package PVTCylindersGUI;

import PVTCylindersGUI.dataModel.Cylinder;
import PVTCylindersGUI.dataModel.CylinderOperation;
import PVTCylindersGUI.dataModel.Operation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewOperationController {

    @FXML
    private ComboBox<String> operationBox;
    @FXML
    private TextField wellField;
    @FXML
    private ComboBox<String> sampleBox;
    @FXML
    private ComboBox<String> testBox;
    @FXML
    private TextField volumeField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private DatePicker datePicker;

    private Cylinder selectedCylinder;
    private CylinderOperation selectedOperation;
    private boolean editable; // true for "addNewOperation" and "editOperation", false for "showOperation"

    @FXML
    void initialize() {

        datePicker.setValue(LocalDate.now());

        operationBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (editable){
                    switch (newValue){
                        case "Service":
                            wellField.setText("NA");
                            wellField.setDisable(true);
                            sampleBox.getSelectionModel().selectFirst();
                            sampleBox.setDisable(true);
                            testBox.getSelectionModel().selectFirst();
                            testBox.setDisable(true);
                            volumeField.setDisable(true);
                            volumeField.clear();
                            break;
                        case "Test":
                            wellField.setDisable(true);
                            setSampleWellField();
                            sampleBox.setDisable(true);
                            testBox.setDisable(false);
                            volumeField.setDisable(false);
                            volumeField.clear();
                            break;
                        case "Inspection":
                            wellField.setDisable(true);
                            setSampleWellField();
                            sampleBox.setDisable(true);
                            testBox.getSelectionModel().selectFirst();
                            testBox.setDisable(true);
                            volumeField.setText("0");
                            volumeField.setDisable(false);
                            break;
                        case "New Sample":
                            wellField.setDisable(false);
                            wellField.clear();
                            if (selectedCylinder.getCylinderType().equals("G")){
                                sampleBox.getSelectionModel().select(2);
                            } else {
                                sampleBox.getSelectionModel().select(1);
                            }
                            sampleBox.setDisable(false);
                            testBox.getSelectionModel().selectFirst();
                            testBox.setDisable(true);
                            volumeField.setDisable(false);
                            volumeField.clear();
                            break;
                    }
                }
            }
        });
    }

    void initData(Cylinder selectedCylinder, CylinderOperation operation, boolean editable){
        this.editable = editable;
        this.selectedCylinder = selectedCylinder;
        this.selectedOperation = operation;
        operationBox.getSelectionModel().selectFirst();
        if (selectedOperation != null){
            operationBox.setValue(operation.getOperation().toString());
            wellField.setText(operation.getWell());
            sampleBox.setValue(operation.getSample());
            testBox.setValue(operation.getTest());
            volumeField.setText(String.valueOf(operation.getVolumeChange()));
            descriptionArea.setText(operation.getDescription());
            datePicker.setValue(operation.getDate_English());
        }
        if (!editable){
            operationBox.setOnShown(event -> operationBox.hide());
            wellField.setEditable(false);
            sampleBox.setOnShown(event -> sampleBox.hide());
            testBox.setOnShown(event -> testBox.hide());
            volumeField.setEditable(false);
            descriptionArea.setEditable(false);
            datePicker.setOnShown(event -> datePicker.hide());
            datePicker.setEditable(false);
        }
    }

    void setSampleWellField(){
        if (selectedCylinder.getOperations() != null) {
            int operationsSize = selectedCylinder.getOpSize();
            if (operationsSize > 0) {
                CylinderOperation lastOperation = selectedCylinder.getOperations().get(operationsSize - 1);
                wellField.setText(lastOperation.getWell());
                sampleBox.setValue(lastOperation.getSample());
            }
        }
    }

    public CylinderOperation process(){

        if (!editable){
            return null;
        }

        String operationStr = operationBox.getValue();
        Map<String, Operation> operationMapping = new HashMap<>();
        operationMapping.put("Test", Operation.TEST);
        operationMapping.put("New Sample", Operation.NEW_SAMPLE);
        operationMapping.put("Service", Operation.SERVICE);
        operationMapping.put("Inspection", Operation.INSPECTION);
        Operation operation = operationMapping.get(operationStr);

        String well = wellField.getText().trim();
        String sample = sampleBox.getValue();
        String test = testBox.getValue();
        String description = descriptionArea.getText().trim();
        LocalDate date_English = datePicker.getValue();


        double remainingVolume;
        if (selectedOperation == null) {
            // if new dialog
            remainingVolume = selectedCylinder.getRemainingVolume();
        } else {
            // if edit dialog, add the last operation's volume change to the remaining volume
            remainingVolume = selectedCylinder.getRemainingVolume() + Math.abs(selectedOperation.getVolumeChange());
        }

        try {
            double volumeChange = (operation == Operation.SERVICE) ?  // for Service: volume change is defined here
                // if edit dialog, set change volume of a Service operation to 0
                (this.selectedOperation!=null ?  0 : -(selectedCylinder.getRemainingVolume())) :
                Double.parseDouble(volumeField.getText().trim()); // check if volumeChange is a double

            if (selectedCylinder.getCylinderType().equals("G") &&
                (sample.equals("Oil") || sample.equals("Formation Water") || sample.equals("Distilled Water"))){
                showWarning("You cannot fill a gas cylinder with oil or water");
                return null;
            }

            if ((operation == Operation.NEW_SAMPLE) && (volumeChange <= 0)){
                // if new sample, volumeChange must be positive
                showWarning("You must enter a positive number for volume change");
                return null;
            } else if (operation == Operation.TEST){
                // if test, volumeChange must be negative
                if (volumeChange >= 0) {
                    showWarning("You must enter a negative number for volume change");
                    return null;
                }
                // if test, abs(volumeChange) must be smaller than remaining volume
                if (Math.abs(volumeChange) > remainingVolume){
                    showWarning("Volume change must be less than the remaining volume");
                    return null;
                }
                if (testBox.getValue().equals("NA")){
                    showWarning("You must choose a test");
                    return null;
                }
            }

            if ((operation == Operation.NEW_SAMPLE)){
                if (selectedCylinder.getOpSize() > 0) {
                    CylinderOperation lastOperation = selectedCylinder.getOperations().get(selectedCylinder.getOpSize() - 1);
                    if (lastOperation.getOperation() != Operation.SERVICE && this.selectedOperation==null) {
                        showWarning("You must first add a Service Operation");
                        return null;
                    }
                }
                if (selectedCylinder.isNeedingService()){
                    showWarning("You must first add a Service Operation");
                    return null;
                }
            }


            Pattern p2 = Pattern.compile("[a-zA-Z0-9]+[,\\-_/\\\\ ]*[a-zA-Z0-9]*"); // check the correct pattern for well name
            Matcher matcher = p2.matcher(well);
            if (!matcher.matches()){
                showWarning("You must enter a valid well name\nStart and end with a letter or number\n" +
                        "You can use - _ \\ / , in between");
                return null;
            }

//            if ((operation == Operation.TEST)){
//                if (selectedCylinder.getRemainingVolume() == 0){
//                    showWarning("The cylinder is empty");
//                    return null;
//                } if (testBox.getValue().equals("NA")){
//                    showWarning("You must choose a test");
//                    return null;
//                }
//            }

            return new CylinderOperation(well, sample, operation, test, volumeChange, description, date_English);

        } catch (NumberFormatException e){ // if volumeChange is not a double, show the warning and exit
            showWarning("You must enter a number for volume change");
            return null;
        }

    }

    private void showWarning(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.show();
    }
}
