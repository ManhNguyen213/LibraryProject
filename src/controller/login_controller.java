package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import service.AuthService;

public class login_controller {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button exitButton;
    
    private final AuthService authService = new AuthService();
    
    private void loadScene(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    public void handleLogin(ActionEvent event) {
    	String username = usernameField.getText();
    	String password = passwordField.getText();
    	
    	if(username.isEmpty() || password.isEmpty()) {
    		showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
    		return;
    	}
    	
    	Optional<String> roleOpt = authService.login(username, password);
    	
        if (roleOpt.isPresent()) {
            String role = roleOpt.get();

            if (role.equals("manager")) {
                try {
                    loadScene("/views/fxml/manager.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (role.equals("member")) {
            	showAlert(Alert.AlertType.INFORMATION, "Login", "Logged in as member.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to login. Invalid credentials.");
        }
    }
    
    @FXML
    public void handleExit(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
