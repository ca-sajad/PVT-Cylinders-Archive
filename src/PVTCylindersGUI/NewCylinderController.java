package PVTCylindersGUI;

import PVTCylindersGUI.dataModel.Cylinder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewCylinderController {
    @FXML
    private TextField cylinderNameField;
    @FXML
    private ComboBox<String> typeBox;

    @FXML
    public void initialize(){
        Platform.runLater(()->cylinderNameField.requestFocus());
    }

    public Cylinder process(){
        String cylinderName = cylinderNameField.getText().trim();
        String cylinderType = (typeBox.getValue().equals("Oil")) ? "O" : "G";

        if (cylinderName.isEmpty()){
            showWarning("You Must Enter a Name");
            return null;
        } else {
            // check the correct pattern for cylinder name, preventing Farsi characters
            Pattern pattern = Pattern.compile("[a-zA-Z0-9]+[,\\-_/\\\\ ]*[a-zA-Z0-9]*");
            Matcher matcher = pattern.matcher(cylinderName);
            if (matcher.matches()) {
                return new Cylinder(cylinderName, cylinderType);
            } else {
                showWarning("You must enter a valid name\nStart and end with a letter or number\n" +
                        "You can use - _ \\ / , in between");
                return null;
            }
        }
    }

    private void showWarning(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.show();
    }
}
