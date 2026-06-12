package controller;

import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import service.BookService;
import service.MemberService;
import service.InvoiceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import model.Book;
import model.Invoice;
import model.Member;

public class admin_controller implements Initializable {

    @FXML
    private Button exitBtn;

    @FXML
    private BorderPane main_form;

    @FXML
    private Button manageBooksBtn;

    @FXML
    private AnchorPane manageBooksForm;

    @FXML
    private TextField mb_author;

    @FXML
    private TextField mb_bookID;

    @FXML
    private Button mb_clearBtn;

    @FXML
    private TableColumn<Book, String> mb_col_author;

    @FXML
    private TableColumn<Book, String> mb_col_bookID;

    @FXML
    private TableColumn<Book, String> mb_col_genre;

    @FXML
    private TableColumn<Book, Double> mb_col_price;

    @FXML
    private TableColumn<Book, Integer> mb_col_quantity;

    @FXML
    private TableColumn<Book, String> mb_col_title;

    @FXML
    private Button mb_deleteBtn;

    @FXML
    private ComboBox<String> mb_genre;

    @FXML
    private ComboBox<String> mb_genreFilter;

    @FXML
    private Button mb_insertBtn;

    @FXML
    private TextField mb_price;

    @FXML
    private TextField mb_quantity;

    @FXML
    private Button mb_resetBtn;

    @FXML
    private TextField mb_search;

    @FXML
    private TableView<Book> mb_tableView;

    @FXML
    private TextField mb_title;

    @FXML
    private Button mb_updateBtn;

    @FXML
    private TextField mem_address;

    @FXML
    private Button mem_clearBtn;

    @FXML
    private TableColumn<Member, String> mem_col_ID;

    @FXML
    private TableColumn<Member, String> mem_col_address;

    @FXML
    private TableColumn<Member, String> mem_col_email;

    @FXML
    private TableColumn<Member, String> mem_col_fullName;

    @FXML
    private TableColumn<Member, String> mem_col_phone;

    @FXML
    private TableColumn<Member, String> mem_col_rank;

    @FXML
    private TableColumn<Member, String> mem_col_status;

    @FXML
    private Button mem_deleteBtn;

    @FXML
    private TextField mem_email;

    @FXML
    private TextField mem_fullName;

    @FXML
    private TextField mem_id;

    @FXML
    private TextField mem_phone;

    @FXML
    private ComboBox<String> mem_rank;

    @FXML
    private Button mem_registerBtn;

    @FXML
    private TextField mem_search;

    @FXML
    private TableView<Member> mem_tableView;

    @FXML
    private Button mem_updateBtn;

    @FXML
    private Button membersBtn;

    @FXML
    private AnchorPane membersForm;

    @FXML
    private Button minimizeBtn;

    @FXML
    private TableColumn<Invoice, String> od_col_ID;

    @FXML
    private TableColumn<Invoice, LocalDate> od_col_dateCreated;

    @FXML
    private TableColumn<Invoice, String> od_col_member;

    @FXML
    private TableColumn<Invoice, String> od_col_employee;

    @FXML
    private TableColumn<Invoice, Double> od_col_totalPrice;

    @FXML
    private TableColumn<Invoice, Integer> od_col_discount;

    @FXML
    private TableColumn<Invoice, Double> od_col_finalPrice;
    
    @FXML
    private TableColumn <Invoice, Void> od_col_viewDetails;

    @FXML
    private Button od_deleteBtn;

    @FXML
    private Button od_newBtn;

    @FXML
    private TextField od_search;

    @FXML
    private TableView<Invoice> od_tableView;

    @FXML
    private AnchorPane ordersForm;

    @FXML
    private Button recordsBtn;

    @FXML
    private Button signOutBtn;

    @FXML
    private Label st_availableBooks;

    @FXML
    private BarChart<String, Number> st_incomeChart;

    @FXML
    private BarChart<String, Number> st_rankBarChart;

    @FXML
    private Label st_totalIncomes;

    @FXML
    private Label st_totalMembers;

    @FXML
    private Button staticsBtn;

    @FXML
    private AnchorPane staticsForm;

    
	
	private final BookService bookService = new BookService();
	private final MemberService memberService = new MemberService();
	private final InvoiceService invoiceService = new InvoiceService();
	
	private String originalBookID = null;
	private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
	private FilteredList<Invoice> filteredList;
	private ObservableList<Book> bookList = FXCollections.observableArrayList();
	private ObservableList<Member> memberList = FXCollections.observableArrayList();
	private final ObservableList<String> rankOptions = FXCollections.observableArrayList("Bronze", "Silver", "Gold", "Platinum", "Emerald", "Diamond");
	private String originalAccountID = null;
	
	private ObservableList<Book> mb_getBookListFromDB() {
		return FXCollections.observableArrayList(bookService.getAllBooks());
	}

	private final ObservableList<String> genreOptions = FXCollections.observableArrayList("Self-Help", "Fiction",
			"Non-fiction", "Science", "History", "Fantasy", "Biography", "Romance", "Horror", "Mystery", "Children");

	public void loadBooks() {
	    bookList.clear(); 
	    bookList.addAll(bookService.getAllBooks());
	}

	
	@FXML
	private void mb_insertBook() {
		String id = mb_bookID.getText().trim();
		String title = mb_title.getText().trim();
		String author = mb_author.getText().trim();
		String genreValue = mb_genre.getValue();
	    String genre = (genreValue != null) ? genreValue.trim() : "";
	    
		double price;
		int quantity;
		
		if (id.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
			return;
		}
		
		try {
			price = Double.parseDouble(mb_price.getText());
			quantity = Integer.parseInt(mb_quantity.getText());
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price and quantity must be numbers.");
			return;
		}

		Book newBook = new Book(id, title, author, genre, quantity, price);
		boolean success = bookService.addBook(newBook);
		
		if (success) {
			bookList.add(newBook);
			showAlert(Alert.AlertType.INFORMATION, "Success", "Book inserted successfully.");
			mb_clearBookFields();
		} else {
			showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert book. ID might already exist.");
		}
	}

	private void mb_clearBookFields() {
		mb_bookID.clear();
		mb_title.clear();
		mb_author.clear();
		mb_genre.getSelectionModel().clearSelection();
		mb_quantity.clear();
		mb_price.clear();
	}

	@FXML
	private void mb_handleClearBtn(ActionEvent event) {
		mb_clearBookFields();
	}

	@FXML
	private void mb_selectBook() {
		Book selectedBook = mb_tableView.getSelectionModel().getSelectedItem();
		if (selectedBook != null) {
			mb_bookID.setText(selectedBook.getBookID());
			mb_title.setText(selectedBook.getTitle());
			mb_author.setText(selectedBook.getAuthor());
			mb_genre.setValue(selectedBook.getGenre());
			mb_quantity.setText(String.valueOf(selectedBook.getQuantity()));
			mb_price.setText(String.valueOf(selectedBook.getPrice()));
			originalBookID = selectedBook.getBookID();
		}
	}

	@FXML
	private void mb_updateBook() {
		String newID = mb_bookID.getText().trim();
		String title = mb_title.getText().trim();
		String author = mb_author.getText().trim();
		String genre = mb_genre.getValue();
		double price;
		int quantity;

		try {
			price = Double.parseDouble(mb_price.getText());
			quantity = Integer.parseInt(mb_quantity.getText());
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price and quantity must be numbers.");
			return;
		}

		if (newID.isEmpty() || title.isEmpty() || author.isEmpty() || genre == null) {
			showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
			return;
		}

		Book updatedBook = new Book(newID, title, author, genre, quantity, price);
		boolean success = bookService.updateBook(updatedBook, originalBookID);

		if (success) {
			showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully.");
			bookList.setAll(bookService.getAllBooks());
			mb_tableView.setItems(bookList);
			mb_clearBookFields();
			originalBookID = null;
		} else {
			showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book. Check ID or connection.");
		}
	}

	@FXML
	private void mb_deleteBook() {
		String id = mb_bookID.getText().trim();
		if (id.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a book to delete.");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to delete this book?");
		Optional<ButtonType> option = alert.showAndWait();

		if (option.isPresent() && option.get() == ButtonType.OK) {
			boolean success = bookService.deleteBook(id);
			if (success) {
				bookList.setAll(bookService.getAllBooks());
				mb_tableView.setItems(bookList);
				mb_clearBookFields();
				originalBookID = null;
			} else {
				showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book.");
			}
		}
	}

	@FXML
	private void mb_resetFilter() {
		mb_genreFilter.getSelectionModel().clearSelection();
		mb_search.clear();
		mb_tableView.setItems(bookList);
	}

	private void mb_applyFilters() {
		String selectedGenre = mb_genreFilter.getValue();
		String searchText = mb_search.getText().toLowerCase().trim();

		ObservableList<Book> filteredList = bookList.filtered(book -> {
			boolean matchesGenre = (selectedGenre == null || selectedGenre.isEmpty())
					|| book.getGenre().equalsIgnoreCase(selectedGenre);
			boolean matchesTitle = searchText.isEmpty() || book.getTitle().toLowerCase().contains(searchText);
			return matchesGenre && matchesTitle;
		});

		mb_tableView.setItems(filteredList);
	}
	
	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	// Members
	private ObservableList<Member> mem_getMemberList() {
	    return FXCollections.observableArrayList(memberService.getAllMembers());
	}

	
	@FXML
	private void mem_selectMember(ActionEvent event) {
		mem_selectMember();
	}
	
	private void mem_selectMember() {
	    Member selected = mem_tableView.getSelectionModel().getSelectedItem();
	    if (selected != null) {
	        mem_id.setText(selected.getAccountId());
	        mem_fullName.setText(selected.getFullName());
	        mem_email.setText(selected.getEmail());
	        mem_phone.setText(selected.getPhone());
	        mem_address.setText(selected.getAddress());
	        mem_rank.setValue(selected.getRank());
	        originalAccountID = selected.getAccountId();
	    }
	}
	
	@FXML
	private void mem_clearMemberFields(ActionEvent event) {
	    mem_clearMemberFields();
	}
	
	private void mem_clearMemberFields() {
	    mem_id.clear();
	    mem_fullName.clear();
	    mem_email.clear();
	    mem_phone.clear();
	    mem_address.clear();
	    mem_rank.getSelectionModel().clearSelection();
	}
	
	@FXML
	private void mb_insertMember(ActionEvent event) {
	    String id = mem_id.getText().trim();
	    String fullName = mem_fullName.getText().trim();
	    String email = mem_email.getText().trim();
	    String phone = mem_phone.getText().trim();
	    String address = mem_address.getText().trim();
	    String rank = mem_rank.getValue();

	    if (id.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || rank == null) {
	        showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
	        return;
	    }

	    Member newMember = new Member(id, fullName, phone, email, address, rank);
	    boolean success = memberService.addMember(newMember);

	    if (success) {
	        memberList.add(new Member(id, fullName, email, phone, address, rank, "Active"));
	        showAlert(Alert.AlertType.INFORMATION, "Success", "Member inserted successfully.");
	        mem_clearMemberFields();
	    } else {
	        showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert member. ID might already exist.");
	    }
	}

	@FXML
	private void mb_updateMember(ActionEvent event) {
	    String newID = mem_id.getText().trim();
	    String fullName = mem_fullName.getText().trim();
	    String email = mem_email.getText().trim();
	    String phone = mem_phone.getText().trim();
	    String address = mem_address.getText().trim();
	    String rank = mem_rank.getValue();

	    if (newID.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || rank == null) {
	        showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
	        return;
	    }

	    Member updatedMember = new Member(newID, fullName, phone, email, address, rank);
	    boolean success = memberService.updateMember(updatedMember, originalAccountID);

	    if (success) {
	        showAlert(Alert.AlertType.INFORMATION, "Success", "Member updated successfully.");
	        memberList.setAll(memberService.getAllMembers());
	        mem_tableView.setItems(memberList);
	        mem_clearMemberFields();
	        originalAccountID = null;
	    } else {
	        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update member. Check ID or connection.");
	    }
	}

	@FXML
	private void mem_deleteMember(ActionEvent event) {
	    String id = mem_id.getText().trim();
	    if (id.isEmpty()) {
	        showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a member.");
	        return;
	    }

	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this member?", ButtonType.YES, ButtonType.NO);
	    Optional<ButtonType> result = alert.showAndWait();

	    if (result.isPresent() && result.get() == ButtonType.YES) {
	        boolean success = memberService.deleteMember(id);
	        if (success) {
	            memberList.setAll(memberService.getAllMembers());
	            mem_tableView.setItems(memberList);
	            mem_clearMemberFields();
	            originalAccountID = null;
	        } else {
	            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete member.");
	        }
	    }
	}

	private void searchMembers(String name) {
	    memberList.clear();
	    memberList.addAll(memberService.searchMembersByName(name));
	}

	// Orders
	private void addButtonToTable() {
	    Callback<TableColumn<Invoice, Void>, TableCell<Invoice, Void>> cellFactory = new Callback<>() {
	        @Override
	        public TableCell<Invoice, Void> call(final TableColumn<Invoice, Void> param) {
	            return new TableCell<>() {
	                private final Button btn = new Button("View Details");

	                {
	                    btn.setStyle("-fx-background-color: transparent;" +
	                                 "-fx-text-fill: #0066cc;" +
	                                 "-fx-underline: true;");
	                    btn.setCursor(Cursor.HAND);

	                    btn.setOnAction((ActionEvent event) -> {
	                        Invoice invoice = getTableView().getItems().get(getIndex());
	                        showInvoiceDetails(invoice);
	                    });
	                }

	                @Override
	                public void updateItem(Void item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (empty) {
	                        setGraphic(null);
	                    } else {
	                        setGraphic(btn);
	                    }
	                }
	            };
	        }
	    };

	    od_col_viewDetails.setCellFactory(cellFactory);
	}
	
	public ObservableList<Invoice> loadInvoices() {
	    return FXCollections.observableArrayList(invoiceService.getAllInvoices());
	}
	
	private void showInvoiceDetails(Invoice invoice) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/make_receipt.fxml"));
	        Parent root = loader.load();

	        make_order_controller controller = loader.getController();
	        controller.loadReceipt(
	            invoice.getInvoiceId(),
	            invoice.getMemberId()
	        );

	        Stage stage = new Stage();
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (IOException | SQLException e) {
	        e.printStackTrace();
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Failed to load receipt details");
	        alert.setContentText("An error occurred while opening the receipt view.");
	        alert.showAndWait();
	    }
	}

	

	@FXML
	private void mr_handleNewBtn(ActionEvent event) {
	    try {
	        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/fxml/make_receipt.fxml"));
	        Parent root = fxmlLoader.load();
	        
	        make_order_controller receiptController = fxmlLoader.getController();
	        receiptController.setAdminController(this);
	        
	        Stage stage = new Stage();
	        stage.initStyle(StageStyle.UNDECORATED);
	        stage.setScene(new Scene(root));
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.showAndWait();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	public void refreshInvoiceTable() {
        invoiceList.setAll(invoiceService.getAllInvoices());
    }
	
	@FXML
	private void handleDeleteInvoice() {
	    Invoice selectedInvoice = od_tableView.getSelectionModel().getSelectedItem();

	    if (selectedInvoice == null) {
	        Alert alert = new Alert(Alert.AlertType.WARNING);
	        alert.setTitle("No Selection");
	        alert.setHeaderText(null);
	        alert.setContentText("Please select an invoice to delete.");
	        alert.showAndWait();
	        return;
	    }

	    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
	    confirm.setTitle("Confirm Deletion");
	    confirm.setHeaderText("Are you sure you want to delete this invoice?");

	    Optional<ButtonType> result = confirm.showAndWait();
	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        boolean success = invoiceService.deleteInvoiceAndReturnBooks(selectedInvoice);
	        if (success) {
	            invoiceList.remove(selectedInvoice);
	        } else {
	            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete invoice.", ButtonType.OK);
	            error.showAndWait();
	        }
	    }
	}


	// Statistics
	public void st_updateAvailableBooks() {
		int total = bookService.getTotalAvailableQuantity();
		st_availableBooks.setText(String.valueOf(total));
	}
	
	public void st_updateTotalMembers() {
	    int total = memberService.getTotalMembersCount();
	    st_totalMembers.setText(String.valueOf(total));
	}
	
	public void st_updateTotalIncomes() {
	    double total = invoiceService.getTotalIncomeAfterDiscount();
	    st_totalIncomes.setText("$" + String.format("%.2f", total));
	}

	
	public void loadIncomeBarChart() {
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    Map<String, Double> data = invoiceService.getIncomeByMonthYear();
	    
	    for (Map.Entry<String, Double> entry : data.entrySet()) {
	        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
	    }
	    
	    st_incomeChart.getData().clear();
	    st_incomeChart.getData().add(series);
	}

	private String formatYearMonthLabel(int year, int month) {
	    return year + "-" + String.format("%02d", month);
	}
	
	public void loadMemberRankBarChart() {
	    st_rankBarChart.getData().clear();
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    series.setName("Thành viên theo hạng");

	    Map<String, Integer> data = memberService.getMembersRankDistribution();
	    for (Map.Entry<String, Integer> entry : data.entrySet()) {
	        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
	    }

	    st_rankBarChart.getData().add(series);
	}

	
	@FXML
	public void signout() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Message");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to sign out?");
		Optional<ButtonType> option = alert.showAndWait();
		if (option.isPresent() && option.get().equals(ButtonType.OK)) {
			try {
				Parent root = FXMLLoader.load(getClass().getResource("/views/fxml/login.fxml"));
				Stage primaryStage = new Stage();
				Scene scene = new Scene(root);

				primaryStage.initStyle(StageStyle.TRANSPARENT);
				primaryStage.setScene(scene);
				primaryStage.show();

				Stage currentStage = (Stage) signOutBtn.getScene().getWindow();
				currentStage.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	public void switchForm(ActionEvent event) {

		if (event.getSource() == manageBooksBtn) {
			manageBooksForm.setVisible(true);
			ordersForm.setVisible(false);
			membersForm.setVisible(false);
			staticsForm.setVisible(false);
			
			loadBooks();
		} else if (event.getSource() == recordsBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(true);
			membersForm.setVisible(false);
			staticsForm.setVisible(false);
			
			refreshInvoiceTable();
			
		} else if (event.getSource() == membersBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(false);
			membersForm.setVisible(true);
			staticsForm.setVisible(false);

		} else if (event.getSource() == staticsBtn) {
			manageBooksForm.setVisible(false);
			ordersForm.setVisible(false);
			membersForm.setVisible(false);
			staticsForm.setVisible(true);
			
			st_updateAvailableBooks();
			st_updateTotalMembers();
			st_updateTotalIncomes();
			loadIncomeBarChart();
			loadMemberRankBarChart();
		}

	}

	@FXML
	public void close() {
		Stage stage = (Stage) exitBtn.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void minimize(ActionEvent event) {
		Stage stage = (Stage) minimizeBtn.getScene().getWindow();
		stage.setIconified(true);
	}

	private double x = 0;
	private double y = 0;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		main_form.setOnMousePressed(event -> {
			x = event.getSceneX();
			y = event.getSceneY();
		});

		main_form.setOnMouseDragged(event -> {
			Stage stage = (Stage) main_form.getScene().getWindow();
			stage.setX(event.getScreenX() - x);
			stage.setY(event.getScreenY() - y);
		});
		
		// Manage_Books
		mb_col_bookID.setCellValueFactory(new PropertyValueFactory<>("bookID"));
		mb_col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
		mb_col_author.setCellValueFactory(new PropertyValueFactory<>("author"));
		mb_col_genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
		mb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		mb_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));

		mb_genre.setItems(genreOptions);
		mb_genreFilter.setItems(genreOptions);

		bookList = mb_getBookListFromDB();
		mb_tableView.setItems(bookList);

		mb_genreFilter.setOnAction(e -> mb_applyFilters());
		mb_search.textProperty().addListener((observable, oldValue, newValue) -> mb_applyFilters());

		mb_resetBtn.setOnAction(e -> {
			mb_genreFilter.getSelectionModel().clearSelection();
			mb_search.clear();
			mb_tableView.setItems(bookList);
		});

		mb_tableView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 1) {
				mb_selectBook();
			}
		});
	
		// Members
		mem_col_ID.setCellValueFactory(new PropertyValueFactory<>("accountId"));
	    mem_col_fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
	    mem_col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
	    mem_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
	    mem_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));
	    mem_col_rank.setCellValueFactory(new PropertyValueFactory<>("rank"));
	    mem_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

	    mem_rank.setItems(rankOptions);
	    memberList = mem_getMemberList();
	    mem_tableView.setItems(memberList);

	    mem_tableView.setOnMouseClicked(event -> {
	        if (event.getClickCount() == 1) {
	            mem_selectMember();
	        }
	    });
	    
	    mem_search.setOnKeyReleased(event -> {
	        String searchText = mem_search.getText().trim();
	        searchMembers(searchText);
	    });
	    
	    // Orders
	    od_col_ID.setCellValueFactory(cellData -> cellData.getValue().invoiceIdProperty());
	    od_col_dateCreated.setCellValueFactory(cellData -> cellData.getValue().dateCreatedProperty());
	    od_col_member.setCellValueFactory(cellData -> cellData.getValue().memberIdProperty());
	    od_col_employee.setCellValueFactory(cellData -> cellData.getValue().employeeIdProperty());
	    od_col_totalPrice.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());
	    od_col_discount.setCellValueFactory(cellData -> cellData.getValue().discountAppliedProperty().asObject());
	    od_col_finalPrice.setCellValueFactory(cellData -> cellData.getValue().finalPriceProperty().asObject());
	    
	    od_col_finalPrice.setCellFactory(tc -> new TableCell<Invoice, Double>() {
	        @Override
	        protected void updateItem(Double price, boolean empty) {
	            super.updateItem(price, empty);
	            if (empty || price == null) {
	                setText(null);
	            } else {
	                setText(String.format("%.2f", price));
	            }
	        }
	    });
	    
	    invoiceList = loadInvoices();
	    
	    FilteredList<Invoice> filteredList = new FilteredList<>(invoiceList, p -> true);
	    od_tableView.setItems(filteredList);
	    
	    od_search.textProperty().addListener((observable, oldValue, newValue) -> {
	        filteredList.setPredicate(invoice -> {
	            if (newValue == null || newValue.isEmpty()) {
	                return true;
	            }

	            String lowerCaseFilter = newValue.toLowerCase();

	            return invoice.getInvoiceId().toLowerCase().contains(lowerCaseFilter)
	                || invoice.getMemberId().toLowerCase().contains(lowerCaseFilter)
	                || invoice.getEmployeeId().toLowerCase().contains(lowerCaseFilter);
	        });
	    });
	    
	    addButtonToTable();
	}
	
}
