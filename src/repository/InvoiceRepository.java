package repository;

import config.DatabaseConfig;
import model.Book;
import model.Invoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceRepository {

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT invoice_id, date_created, member_id, employee_id, total_price, discount_applied FROM Invoices";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Invoice(
                        rs.getString("invoice_id"),
                        rs.getDate("date_created").toLocalDate(),
                        rs.getString("member_id"),
                        rs.getString("employee_id"),
                        rs.getDouble("total_price"),
                        rs.getInt("discount_applied")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Book> getBooksFromInvoice(String invoiceId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.book_id, b.title, b.author, b.genre, idt.quantity, idt.price_each as price " +
                     "FROM Books b JOIN Invoice_Details idt ON b.book_id = idt.book_id " +
                     "WHERE idt.invoice_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    );
                    book.setSelectedQuantity(rs.getInt("quantity")); // specifically selected for this invoice
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public double getTotalIncomeAfterDiscount() {
        String query = "SELECT SUM(total_price * (100 - discount_applied) / 100) AS total_income FROM Invoices";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total_income");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Double> getIncomeByMonthYear() {
        Map<String, Double> data = new HashMap<>();
        String query = "SELECT YEAR(date_created) AS year, MONTH(date_created) AS month, SUM(total_price) AS total_income " +
                       "FROM Invoices GROUP BY YEAR(date_created), MONTH(date_created) ORDER BY YEAR(date_created), MONTH(date_created)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                double income = rs.getDouble("total_income");
                String label = year + "-" + String.format("%02d", month);
                data.put(label, income);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}
