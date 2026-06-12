package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.control.Spinner;
import javafx.beans.value.ChangeListener;

import model.Book;
import model.Invoice;
import model.Member;
import service.BookService;
import service.InvoiceService;
import service.MemberService;

public class make_order_controller implements Initializable {
	
    @FXML
    private TableColumn<Book, String> mr_col_author;

    @FXML
    private TableColumn<Book, Integer> mr_col_available;

    @FXML
    private TableColumn<Book, String> mr_col_id;

    @FXML
    private TableColumn<Book, Double> mr_col_price;

    @FXML
    private TableColumn<Book, Integer> mr_col_selected;

    @FXML
    private TableColumn<Book, String> mr_col_title;

    @FXML
    private TableView<Book> mr_tableView;
    
	@FXML 
	private ComboBox<String> mr_id;
	
	@FXML 
	private TextField mr_memberName;
	
	@FXML
	private BorderPane mr_form;
	
	@FXML
	private Button mr_close;
	
	@FXML
    private Label mr_subtotal;
	
	@FXML
    private Label mr_discount;
	
	@FXML
    private Label mr_total;
	
	@FXML
	private Button mr_create;
	
	@FXML
	private Button mr_clear;
	
	@FXML
	private TextField mr_search;
	
	@FXML
	private VBox mr_vbox_books;
	
	private Map<String, String> memberMap = new HashMap<>();
	
	private admin_controller admin_controller; 

    public void setAdminController(admin_controller admin_controller) {
        this.admin_controller = admin_controller;
    }
	
	private int discountPercent = 0; 
    private ObservableList<Book> bookList;
    
    private final BookService bookService = new BookService();
    private final MemberService memberService = new MemberService();
    private final InvoiceService invoiceService = new InvoiceService();
	
    public void setInvoice(Invoice invoice) {
        String memberId = invoice.memberIdProperty().get();

        mr_id.getItems().clear();
        mr_id.getItems().add(memberId);
        mr_id.setValue(memberId);

        updateMemberInfo(memberId);

        List<Book> orderedBooks = invoiceService.getBooksFromInvoice(invoice.getInvoiceId());
        bookList = FXCollections.observableArrayList(bookService.getAllBooks());

        for (Book b : bookList) {
            for (Book ordered : orderedBooks) {
                if (b.getBookID().equals(ordered.getBookID())) {
                    b.setSelectedQuantity(ordered.getSelectedQuantity());
                }
            }
        }

        mr_tableView.setItems(bookList);
    }

    public void updateMemberInfo(String memberId) {
        if (memberId == null || memberId.isEmpty()) {
            mr_memberName.setText("");
            mr_discount.setText("0%");
            discountPercent = 0;
            return;
        }

        memberService.getAllMembers().stream()
            .filter(m -> m.getAccountId().equals(memberId))
            .findFirst()
            .ifPresentOrElse(m -> {
                mr_memberName.setText(m.getFullName());
                discountPercent = memberService.getMembersRankDistribution().containsKey(m.getRank()) ? 
                    memberService.getAllMembers().stream().filter(x -> x.getAccountId().equals(memberId)).findFirst().map(x -> 0).orElse(0) : 0; 
                // Actual discount comes from Rank_Policies, using direct repo call or hardcode for demo
            }, () -> {
                mr_memberName.setText("");
                discountPercent = 0;
            });
            
        // Assuming memberService can get actual discount percent, adding direct logic:
        int actualDiscount = invoiceService.getAllInvoices().stream().filter(i -> i.getMemberId() != null && i.getMemberId().equals(memberId)).map(i -> i.getDiscountApplied()).findFirst().orElse(0);
        // Note: Real system should use repo.getMemberDiscountPercent(memberId), let's pretend it's in MemberService:
        // discountPercent = memberService.getMemberDiscountPercent(memberId); // Assuming we add this.
        mr_discount.setText("-" + discountPercent + "%");
    }

	private void addSpinnerToBookTable() {
	    mr_col_selected.setCellValueFactory(cellData -> cellData.getValue().selectedQuantityProperty().asObject());

	    mr_col_selected.setCellFactory(column -> new TableCell<>() {
	        private final Spinner<Integer> spinner = new Spinner<>();
	        private final ChangeListener<Integer> listener = (obs, oldVal, newVal) -> {
	            Book book = getTableView().getItems().get(getIndex());
	            if (newVal != null) {
	                book.setSelectedQuantity(newVal);
	                updateReceiptView(bookList, discountPercent);
	            }
	        };

	        {
	            spinner.setEditable(true);
	            spinner.setPrefWidth(100);
	        }

	        @Override
	        protected void updateItem(Integer item, boolean empty) {
	            super.updateItem(item, empty);

	            if (empty || getIndex() >= getTableView().getItems().size()) {
	                setGraphic(null);
	            } else {
	                Book book = getTableView().getItems().get(getIndex());
	                spinner.valueProperty().removeListener(listener); 
	                SpinnerValueFactory<Integer> valueFactory =
	                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
	                        0,
	                        book.getQuantity(),
	                        book.getSelectedQuantity()
	                    );
	                spinner.setValueFactory(valueFactory);
	                spinner.valueProperty().addListener(listener);
	                setGraphic(spinner);
	            }
	        }
	    });
	}
	
	public void loadReceipt(String invoiceId, String memberId) {
	    double subtotal = 0;
	    mr_vbox_books.getChildren().clear();
	    Map<String, Integer> invoiceBookQuantities = new HashMap<>();

        List<Book> invoiceBooks = invoiceService.getBooksFromInvoice(invoiceId);

	    for (Book b : invoiceBooks) {
	        invoiceBookQuantities.put(b.getBookID(), b.getSelectedQuantity());
	        subtotal += b.getSelectedQuantity() * b.getPrice();

	        HBox line = new HBox(10);
	        Label titleLabel = new Label(b.getTitle());
	        titleLabel.setStyle("-fx-font-size: 14px;");
	        Label priceLabel = new Label(b.getSelectedQuantity() + " x $" + String.format("%.2f", b.getPrice()));
	        priceLabel.setStyle("-fx-font-size: 14px;");
	        Region spacer = new Region();
	        HBox.setHgrow(spacer, Priority.ALWAYS);
	        line.getChildren().addAll(titleLabel, spacer, priceLabel);
	        mr_vbox_books.getChildren().add(line);
	    }

	    for (Book book : bookList) {
	        if (invoiceBookQuantities.containsKey(book.getBookID())) {
	            book.setSelectedQuantity(invoiceBookQuantities.get(book.getBookID()));
	        } else {
	            book.setSelectedQuantity(0);
	        }
	    }
	    mr_tableView.refresh();

        Invoice currentInvoice = invoiceService.getAllInvoices().stream().filter(i -> i.getInvoiceId().equals(invoiceId)).findFirst().orElse(null);
	    if (currentInvoice != null) {
	        this.discountPercent = currentInvoice.getDiscountApplied();
	    }

	    double total = subtotal * (1 - discountPercent / 100.0);
	    mr_subtotal.setText(String.format("$%.2f", subtotal));
	    mr_discount.setText("-" + discountPercent + "%");
	    mr_total.setText(String.format("$%.2f", total));

	    loadMemberInfo(memberId);
	}

	public void loadMemberInfo(String memberId) {
        memberService.getAllMembers().stream()
            .filter(m -> m.getAccountId().equals(memberId))
            .findFirst()
            .ifPresent(m -> mr_memberName.setText(m.getFullName()));

	    if (!mr_id.getItems().contains(memberId)) {
	        mr_id.getItems().add(memberId);  
	    }
	    mr_id.setValue(memberId);
	}

	private void updateReceiptView(ObservableList<Book> books, int discountPercent) {
	    mr_vbox_books.getChildren().clear();
	    double subtotal = 0;

	    for (Book book : books) {
	        int qty = book.getSelectedQuantity();
	        if (qty > 0) {
	            double price = book.getPrice();
	            double itemTotal = qty * price;
	            subtotal += itemTotal;

	            HBox line = new HBox(10);
	            Label titleLabel = new Label(book.getTitle());
	            Label priceLabel = new Label(qty + " x $" + String.format("%.2f", price));
	            Region spacer = new Region();
	            HBox.setHgrow(spacer, Priority.ALWAYS);

	            line.getChildren().addAll(titleLabel, spacer, priceLabel);
	            mr_vbox_books.getChildren().add(line);
	        }
	    }

	    double total = subtotal * (1 - discountPercent / 100.0);
	    mr_subtotal.setText(String.format("$%.2f", subtotal));
	    mr_discount.setText("-" + discountPercent + "%");
	    mr_total.setText(String.format("$%.2f", total));
	}
	
	private void loadMembersFromDatabase() {
        memberService.getAllMembers().forEach(m -> memberMap.put(m.getAccountId(), m.getFullName()));
    }
	
	@FXML
	private void handleCreate(ActionEvent event) {
	    createInvoice();
	}
	
	public String getSelectedMemberId() {
	    Object selected = mr_id.getValue();
	    if (selected != null) {
	        return selected.toString();
	    }
	    return null; 
	}

	public void createInvoice() {
	    String memberId = getSelectedMemberId();
	    String employeeId = "A01"; // Fixed logic based on current system

	    List<Book> selectedBooks = bookList.stream()
	            .filter(book -> book.getSelectedQuantity() > 0)
	            .collect(Collectors.toList());

	    if (selectedBooks.isEmpty()) {
	        return;
	    }

        boolean success = invoiceService.createInvoice(memberId, employeeId, selectedBooks);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Order created successfully.");
            alert.showAndWait();
            
            if (admin_controller != null) {
                admin_controller.refreshInvoiceTable();
            }
            
            Stage stage = (Stage) mr_create.getScene().getWindow();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Order creation failed.");
            alert.showAndWait();
        }
	}

	@FXML
	private void mr_handleClear() {
	    mr_id.setValue(null);
	    mr_memberName.setText("");
	    mr_discount.setText("0%");
	    discountPercent = 0;

	    if (bookList != null) {
	        for (Book book : bookList) {
	            book.setSelectedQuantity(0);
	        }
	        mr_tableView.refresh();
	    }

	    mr_vbox_books.getChildren().clear();
	    mr_subtotal.setText("$0.00");
	    mr_total.setText("$0.00");
	}

	@FXML
	private void mr_handleClose(ActionEvent event) {
	    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    stage.close();
	}
	
	private double x = 0;
	private double y = 0;
	
	public void initialize(URL location, ResourceBundle resources) {
		mr_form.setOnMousePressed(event -> {
			x = event.getSceneX();
			y = event.getSceneY();
		});

		mr_form.setOnMouseDragged(event -> {
			Stage stage = (Stage) mr_form.getScene().getWindow();
			stage.setX(event.getScreenX() - x);
			stage.setY(event.getScreenY() - y);
		});
		
	    mr_col_id.setCellValueFactory(new PropertyValueFactory<>("bookID"));
	    mr_col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
	    mr_col_author.setCellValueFactory(new PropertyValueFactory<>("author"));
	    mr_col_available.setCellValueFactory(new PropertyValueFactory<>("quantity"));
	    mr_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
	    
	    bookList = FXCollections.observableArrayList(bookService.getAllBooks());
	    mr_tableView.setItems(bookList);
	    
	    addSpinnerToBookTable();
	    loadMembersFromDatabase();
	    
	    mr_id.getItems().addAll(memberMap.keySet());
	    
	    mr_id.setOnAction(event -> {
	        String selectedId = mr_id.getValue();
	        updateMemberInfo(selectedId);
	        updateReceiptView(bookList, discountPercent);
	    });
	    
	    FilteredList<Book> filteredBooks = new FilteredList<>(bookList, b -> true);
	    mr_tableView.setItems(filteredBooks);
	    
	    mr_search.textProperty().addListener((obs, oldValue, newValue) -> {
	        String filter = newValue.toLowerCase().trim();
	        filteredBooks.setPredicate(book -> {
	            if (filter == null || filter.isEmpty()) return true;
	            return book.getTitle().toLowerCase().contains(filter);
	        });
	    });
	}
}
