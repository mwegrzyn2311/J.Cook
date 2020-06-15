package jcook.controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import jcook.filters.NameFilter;
import jcook.models.User;
import jcook.providers.UserProvider;

import java.util.LinkedList;

public class RegisterViewController {
    private final int minLength = 5;
    private final int maxLength = 20;

    private final int closeWindowDelay = 8;

    private final String usernameWrong = "Username has to be at least " + minLength + " character and at most " + maxLength + " character long";
    private final String usernameTaken = "This username is already in use";
    private final String wrongPassword = "Password has to be at least " + minLength + " characters and at most " + maxLength + " character long." +
            "It has to contain at least one capital letter, at least one digit and no special characters";
    private final String passwordsDontMatch = "Passwords don't match";
    private final String registerSuccess = "Successfully registered. You can now login to the app. This window will automatically close after " + closeWindowDelay +
    " seconds";


    @FXML
    TextField usernameField;
    @FXML
    PasswordField passwordField;
    @FXML
    PasswordField repeatPasswordField;
    @FXML
    Label registerInfo;
    @FXML
    Button registerButton;

    @FXML
    Pane mainPane;


    public RegisterViewController() { }

    public void initialize() {

        setMaxLengthListeners();

        // Register register handler
        registerButton.addEventHandler(ActionEvent.ACTION, e -> {

            // Check if username is legal
            String username = usernameField.getText();
            if(username.length() < minLength || username.length() > maxLength || containsSpecialCharacters(username)) {
                registerInfo.setText(usernameWrong);
                registerInfo.setTextFill(Color.RED);
                return;
            }
            // Check if there is not user with given username
            if(UserProvider.getInstance().getObjects(new NameFilter(username)).size() > 0) {
                registerInfo.setText(usernameTaken);
                registerInfo.setTextFill(Color.RED);
                return;
            }

            // Check if password is legal
            String password = passwordField.getText();
            if(!passwordCorrect(password)) {
                registerInfo.setText(wrongPassword);
                registerInfo.setTextFill(Color.RED);
                return;
            }
            // Check if passwords match
            String repeatedPassword = repeatPasswordField.getText();
            if(!repeatedPassword.equals(password)) {
                registerInfo.setText(passwordsDontMatch);
                registerInfo.setTextFill(Color.RED);
                return;
            }
            UserProvider.getInstance().addObject(new User(username, new LinkedList<>(), new LinkedList<>(), password));
            registerInfo.setText(registerSuccess);
            registerInfo.setTextFill(Color.GREEN);
            PauseTransition delay = new PauseTransition(Duration.seconds(closeWindowDelay));
            delay.setOnFinished(event -> ((Stage) mainPane.getScene().getWindow()).close());
            delay.play();
        });
    }

    private boolean containsSpecialCharacters(String text) {
        for(char c : text.toCharArray()) {
            if(!Character.isLetter(c) || !Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean passwordCorrect(String password) {
        if(password.length() < minLength || password.length() > maxLength) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasDigit = false;
        for(char c : password.toCharArray()) {
            if(Character.isLetter(c)) {
                if(Character.isUpperCase(c)) {
                    hasCapital = true;
                }
            } else if(Character.isDigit(c)) {
                hasDigit = true;
            } else {
                return false;
            }
        }
        return (hasCapital && hasDigit);
    }

    private void setMaxLengthListeners() {
        setMaxLengthListener(usernameField);
        setMaxLengthListener(passwordField);
        setMaxLengthListener(repeatPasswordField);
    }
    private void setMaxLengthListener(TextField textField) {
        textField.textProperty().addListener((observableValue, s, t1) -> {
            if(textField.getText().length() > maxLength) {
                textField.setText(textField.getText().substring(0, maxLength));
            }
        });
    }
}