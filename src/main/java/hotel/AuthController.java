package hotel;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {
    @FXML private TextField managerLoginUserField;
    @FXML private PasswordField managerLoginPasswordField;
    @FXML private Label managerLoginStatus;

    @FXML private TextField customerLoginUserField;
    @FXML private PasswordField customerLoginPasswordField;
    @FXML private Label customerLoginStatus;

    @FXML private TextField customerRegisterNameField;
    @FXML private TextField customerRegisterContactField;
    @FXML private TextField customerRegisterEmailField;
    @FXML private TextField customerRegisterUserField;
    @FXML private PasswordField customerRegisterPasswordField;
    @FXML private Label customerRegisterStatus;

    private HotelApp app;
    private HotelService service;

    void initializeApp(HotelApp app, HotelService service) {
        this.app = app;
        this.service = service;
    }

    @FXML
    private void handleManagerLogin() {
        UserAccount account = service.authenticate(
            managerLoginUserField.getText().trim(),
            managerLoginPasswordField.getText(),
            UserAccount.ROLE_MANAGER
        );
        if (account == null) {
            managerLoginStatus.setText("Invalid manager username or password.");
            return;
        }
        openDashboard(account);
    }

    @FXML
    private void handleCustomerLogin() {
        UserAccount account = service.authenticate(
            customerLoginUserField.getText().trim(),
            customerLoginPasswordField.getText(),
            UserAccount.ROLE_CUSTOMER
        );
        if (account == null) {
            customerLoginStatus.setText("Invalid customer username or password.");
            return;
        }
        openDashboard(account);
    }

    @FXML
    private void handleCustomerRegister() {
        if (customerRegisterNameField.getText().isBlank()
            || customerRegisterContactField.getText().isBlank()
            || customerRegisterEmailField.getText().isBlank()
            || customerRegisterUserField.getText().isBlank()
            || customerRegisterPasswordField.getText().isBlank()) {
            customerRegisterStatus.setText("Please fill all customer details.");
            return;
        }

        UserAccount account = service.registerCustomerAccount(
            customerRegisterNameField.getText().trim(),
            customerRegisterContactField.getText().trim(),
            customerRegisterEmailField.getText().trim(),
            customerRegisterUserField.getText().trim(),
            customerRegisterPasswordField.getText()
        );
        if (account == null) {
            customerRegisterStatus.setText("Customer registration failed. Username may already exist.");
            return;
        }

        customerRegisterStatus.setText("Customer account created. Customer ID: " + account.getCustomerId());
        customerRegisterNameField.clear();
        customerRegisterContactField.clear();
        customerRegisterEmailField.clear();
        customerRegisterUserField.clear();
        customerRegisterPasswordField.clear();
    }

    private void openDashboard(UserAccount account) {
        try {
            app.showDashboardScene(account);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to open dashboard.");
            alert.showAndWait();
        }
    }
}
