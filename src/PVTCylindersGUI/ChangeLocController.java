package PVTCylindersGUI;

import PVTCylindersGUI.dataModel.Cylinder;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ChangeLocController {

    @FXML
    private TextField cylLocField;

    public String process(){
        String cylinderLoc = cylLocField.getText().trim();
        switch (cylinderLoc.toLowerCase()){
            case "pvt":
                cylinderLoc = "PVT";
                break;
            case "eor":
                cylinderLoc = "EOR";
                break;
        }
        if (cylinderLoc.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("You Must Enter a Location");
            alert.show();
            return null;
        } else {
            return cylinderLoc;
        }
    }

    void initData(Cylinder selectedCylinder){
        String location;
        if (selectedCylinder == null) {
            location = null;
        } else {
            location = selectedCylinder.getLocation();
            cylLocField.setText(location);
            cylLocField.setStyle("-fx-font-weight: bold");
            cylLocField.setDisable(true);
        }
    }
}
