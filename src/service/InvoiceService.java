package service;

import config.DatabaseConfig;
import model.Book;
import model.Invoice;
import repository.InvoiceRepository;
import repository.MemberRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;

    public InvoiceService() {
        this.invoiceRepository = new InvoiceRepository();
        this.memberRepository = new MemberRepository();
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Book> getBooksFromInvoice(String invoiceId) {
        return invoiceRepository.getBooksFromInvoice(invoiceId);
    }

    public double getTotalIncomeAfterDiscount() {
        return invoiceRepository.getTotalIncomeAfterDiscount();
    }

    public Map<String, Double> getIncomeByMonthYear() {
        return invoiceRepository.getIncomeByMonthYear();
    }

    /**
     * Creates an invoice and handles transaction: inserts invoice, invoice details, and updates book quantities.
     */
    public boolean createInvoice(String memberId, String employeeId, List<Book> selectedBooks) {
        int discountPercent = memberRepository.getMemberDiscountPercent(memberId);
        
        double totalPriceBeforeDiscount = 0;
        for (Book b : selectedBooks) {
            totalPriceBeforeDiscount += b.getPrice() * b.getSelectedQuantity();
        }
        double totalPriceAfterDiscount = totalPriceBeforeDiscount * (1 - discountPercent / 100.0);

        String sqlGetMaxInvoiceId = "SELECT TOP 1 invoice_id FROM Invoices ORDER BY invoice_id DESC";
        String sqlInsertInvoice = "INSERT INTO Invoices(invoice_id, member_id, employee_id, date_created, total_price, discount_applied) VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?)";
        String sqlInsertDetail = "INSERT INTO Invoice_Details(invoice_id, book_id, quantity, price_each) VALUES (?, ?, ?, ?)";
        String sqlUpdateBookQty = "UPDATE Books SET quantity = quantity - ? WHERE book_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            String newInvoiceId = "IV01";
            try (PreparedStatement psGetMax = conn.prepareStatement(sqlGetMaxInvoiceId);
                 ResultSet rsMax = psGetMax.executeQuery()) {
                if (rsMax.next()) {
                    String lastId = rsMax.getString("invoice_id");
                    int num = Integer.parseInt(lastId.substring(2)) + 1;
                    newInvoiceId = String.format("IV%02d", num);
                }
            }

            try (PreparedStatement psInvoice = conn.prepareStatement(sqlInsertInvoice)) {
                psInvoice.setString(1, newInvoiceId);
                if (memberId != null && !memberId.isEmpty()) {
                    psInvoice.setString(2, memberId);
                } else {
                    psInvoice.setNull(2, java.sql.Types.VARCHAR);
                }
                psInvoice.setString(3, employeeId);
                psInvoice.setDouble(4, totalPriceAfterDiscount);
                psInvoice.setInt(5, discountPercent);
                psInvoice.executeUpdate();
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlInsertDetail);
                 PreparedStatement psBookUpdate = conn.prepareStatement(sqlUpdateBookQty)) {
                
                for (Book b : selectedBooks) {
                    if (b.getSelectedQuantity() > 0) {
                        psDetail.setString(1, newInvoiceId);
                        psDetail.setString(2, b.getBookID());
                        psDetail.setInt(3, b.getSelectedQuantity());
                        psDetail.setDouble(4, b.getPrice());
                        psDetail.executeUpdate();

                        psBookUpdate.setInt(1, b.getSelectedQuantity());
                        psBookUpdate.setString(2, b.getBookID());
                        psBookUpdate.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInvoiceAndReturnBooks(Invoice selectedInvoice) {
        String invoiceId = selectedInvoice.getInvoiceId();
        String getDetailsQuery = "SELECT book_id, quantity FROM Invoice_Details WHERE invoice_id = ?";
        String updateBookQuery = "UPDATE Books SET quantity = quantity + ? WHERE book_id = ?";
        String deleteInvoiceQuery = "DELETE FROM Invoices WHERE invoice_id = ?"; // cascade deletes details
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psGet = conn.prepareStatement(getDetailsQuery)) {
                psGet.setString(1, invoiceId);
                try (ResultSet rs = psGet.executeQuery()) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(updateBookQuery)) {
                        while (rs.next()) {
                            psUpdate.setInt(1, rs.getInt("quantity"));
                            psUpdate.setString(2, rs.getString("book_id"));
                            psUpdate.addBatch();
                        }
                        psUpdate.executeBatch();
                    }
                }
            }

            int rowsDeleted = 0;
            try (PreparedStatement psDelete = conn.prepareStatement(deleteInvoiceQuery)) {
                psDelete.setString(1, invoiceId);
                rowsDeleted = psDelete.executeUpdate();
            }

            if (rowsDeleted > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
