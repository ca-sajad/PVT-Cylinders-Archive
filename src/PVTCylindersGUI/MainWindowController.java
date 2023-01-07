package PVTCylindersGUI;

import PVTCylindersGUI.dataModel.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;


public class MainWindowController {

    // first Tab
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label labelBottom;
    @FXML
    private TextField searchField;
    // first Tab - Left Table
    @FXML
    private TableView<Cylinder> leftTable;
    @FXML
    private TableColumn<Cylinder, String> cylNameLeftCol;
    @FXML
    private TableColumn<Cylinder, String> cylStatusLeftCol;
    @FXML
    private TableColumn<Cylinder, String> sampleLeftCol;
    @FXML
    private TableColumn<Cylinder, String> remVolumeLeftCol;
    @FXML
    private ContextMenu leftContextMenu;
    // first Tab - Right Table
    @FXML
    private TableView<CylinderOperation> rightTable;
    @FXML
    private TableColumn<CylinderOperation, String> WellRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> sampleRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> operationRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> testRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> volumeChangeRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> dateEnglishRightCol;
    @FXML
    private TableColumn<CylinderOperation, String> descriptionRightCol;
    @FXML
    private ContextMenu rightContextMenu;

    // second Tab
    @FXML
    private ComboBox<String> sortedBox;
    @FXML
    private ComboBox<String> statisticsBox;
    @FXML
    private ComboBox<String> wellStatOilBox;
    @FXML
    private ComboBox<String> wellStatGasBox;
    @FXML
    private BarChart<String, Integer> barChart;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private CategoryAxis xAxis;


    private StatisticsTab statisticsTab;
    private CylinderList cylinderList = new CylinderList();
    private ObservableMap<String, Cylinder> cylinders;
    private ObservableList<Cylinder> cylindersValues;
    private FilteredList<Cylinder> filteredList;
    private SortedList<Cylinder> sortedList;

    Comparator<Cylinder> allCylindersComparator = new Comparator<Cylinder>() {
        @Override
        public int compare(Cylinder cylinder1, Cylinder cylinder2) {
            String name1 = cylinder1.getCylinderName();
            String name2 = cylinder2.getCylinderName();

            return compareCylindersNames(name1, name2);
        }
    };

    @FXML
    public void initialize(){

        cylinderList.loadCylinderList();
        cylinders = cylinderList.getCylinderList();
        cylindersValues = FXCollections.observableArrayList(cylinders.values());

        // LEFT TABLE
        leftTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        filteredList = new FilteredList<>(cylindersValues);
        sortedList = new SortedList<>(filteredList);
        sortedList.setComparator(allCylindersComparator);
        leftTable.setItems(sortedList);

        // ComboBox - Filtering Cylinders in LEFT TABLE
        sortedBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            switch (newValue){
                case "All Cylinders":
                    filteredList.setPredicate(p -> true);
                    break;
                case "Oil Cylinders":
                    filteredList.setPredicate(p -> p.getCylinderType().equals("O"));
                    break;
                case "Gas Cylinders":
                    filteredList.setPredicate(p -> p.getCylinderType().equals("G"));
                    break;
                case "Filled":
                    filteredList.setPredicate(p -> p.getRemainingVolume() > 0);
                    break;
                case "Empty":
                    filteredList.setPredicate(p -> p.getRemainingVolume() == 0);
                    break;
                case "Needing Service":
                    filteredList.setPredicate(Cylinder::isNeedingService);
                    break;
                case "In PVT":
                    filteredList.setPredicate(p -> p.getLocation().equals("PVT"));
                    break;
                case "In PVT - Filled":
                    filteredList.setPredicate(p -> (p.getLocation().equals("PVT") && p.getRemainingVolume() > 0));
                    break;
                case "In PVT - Empty":
                    filteredList.setPredicate(p -> (p.getLocation().equals("PVT") && p.getRemainingVolume() == 0));
                    break;
                case "Outside PVT":
                    filteredList.setPredicate(p -> !p.getLocation().equals("PVT"));
                    break;
            }
        });

        // Search TextBox
        searchField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                searchField.clear();
                searchField.setStyle("-fx-text-fill: black");
            }
        });

        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchString = searchField.getText().trim().toLowerCase();
                int sizeString = searchString.length();
                if (keyEvent.getCode().equals(KeyCode.ENTER) && sizeString>1) {
                    searchForWellCylinder(searchString);
                }
            }
        });


        cylNameLeftCol.setCellValueFactory(
                p -> p.getValue().cylinderNameProperty());
        cylStatusLeftCol.setCellValueFactory(
                p -> ((p.getValue().getOperations().size() == 0) || // if there is no operations
                        (p.getValue().getOperations().get(p.getValue().getOpSize()-1).
                                getOperation() == Operation.SERVICE) ?  // or if the last operation is SERVICE
                        new  SimpleStringProperty(p.getValue().isReadyToService()) : // set status to cylinder ready
                        p.getValue().getOperations().get(
                        p.getValue().getOpSize()-1).wellProperty()));
        sampleLeftCol.setCellValueFactory(
                p -> (p.getValue().getOperations().size() == 0 ?
                        new  SimpleStringProperty("Empty") :
                        p.getValue().getOperations().get(
                                p.getValue().getOpSize()-1).sampleProperty()));
        remVolumeLeftCol.setCellValueFactory(
                p -> new  SimpleStringProperty(String.format("%.0f", p.getValue().getRemainingVolume())));

        // LEFT Table - Coloring rows
        Callback<TableColumn<Cylinder, String>, TableCell<Cylinder, String>> typeCallBack = new Callback<>() {
            public TableCell<Cylinder, String> call(TableColumn param) {
                return new TableCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle(null);
                        } else if (!isEmpty()) {
                            Cylinder cylinder = getTableView().getItems().get(getIndex());
                            if(cylinder.getCylinderType().equals("G")) {
                                this.setTextFill(Color.DARKBLUE); // Blue Font for Gas Cylinders
                            } else {
                                this.setTextFill(Color.BLACK); // Black Font for Oil Cylinders
                            }

                            if (cylinder.getLocation().equals("PVT")) {
                                if (cylinder.getRemainingVolume() == 0 && !cylinder.isNeedingService()) {
                                    this.setStyle("-fx-background-color: LightCyan"); // Empty - Ready
                                } else if (!(cylinder.getRemainingVolume() == 0) && !cylinder.isNeedingService()) {
                                    this.setStyle("-fx-background-color: LightGreen"); // Full
                                }
                                if (!cylinder.isReady() && !cylinder.isNeedingService()) {
                                    this.setStyle("-fx-background-color: DarkGrey"); // Out of Service
                                }
                                if (cylinder.isNeedingService()) {
                                    this.setStyle("-fx-background-color: Yellow"); // Needs Service
                                }
                            } else {
                                this.setStyle("-fx-background-color: Bisque"); // Outside PVT
                            }
                            setText(item);
                        }
                    }
                };
            }
        };

        cylNameLeftCol.setCellFactory(typeCallBack);
        cylStatusLeftCol.setCellFactory(typeCallBack);
        sampleLeftCol.setCellFactory(typeCallBack);
        remVolumeLeftCol.setCellFactory(typeCallBack);

//        Callback<TableColumn<Cylinder, String>, TableCell<Cylinder, String>> typeCallBack_2 = new Callback<>() {
//            public TableCell<Cylinder, String> call(TableColumn param) {
//                return new TableCell<>() {
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item == null || empty) {
//                            setText(null);
//                            setStyle(null);
//                        } else if (!isEmpty()) {
//                            Cylinder cylinder = getTableView().getItems().get(getIndex());
//                            if(cylinder.getCylinderType().equals("G")) {
//                                this.setTextFill(Color.DARKBLUE); // Blue Font for Gas Cylinders
//                            } else {
//                                this.setTextFill(Color.BLACK); // Black Font for Oil Cylinders
//                            }
//                            if (cylinder.getCylinderName().equals(
//                                    leftTable.getSelectionModel().getSelectedItem().getCylinderName())) {
//                                this.setStyle("-fx-background-color: red");
//                            }
//                            setText(item);
//                        }
//                    }
//                };
//            }
//        };

        // LEFT TABLE - Context Menu
        leftContextMenu = new ContextMenu();
        MenuItem addOpMenuItem = new MenuItem("Add New Operation");
        addOpMenuItem.setOnAction((ActionEvent event) -> addNewOperation());
        MenuItem changeLocMenuItem = new MenuItem("Change Cylinder's Location");
        changeLocMenuItem.setOnAction((ActionEvent event) -> changeLocation());
        MenuItem showLocMenuItem = new MenuItem("Show Cylinder's Location");
        showLocMenuItem.setOnAction((ActionEvent event) -> showLocation());
        MenuItem needsServiceMenuItem = new MenuItem("Needs Service");
        needsServiceMenuItem.setOnAction((ActionEvent event) -> needsService());
        MenuItem outServiceMenuItem = new MenuItem("In Service / Out of Service");
        outServiceMenuItem.setOnAction((ActionEvent event) -> changeServiceStatus());
        leftContextMenu.getItems().addAll(addOpMenuItem, needsServiceMenuItem, showLocMenuItem,
                changeLocMenuItem, outServiceMenuItem);

        leftTable.setRowFactory(tableView -> {
            TableRow<Cylinder> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(leftContextMenu);
                }
            });
            return row;
        });

        // RIGHT TABLE
        rightTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // RIGHT TABLE - LEFT TABLE interaction
        leftTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Cylinder>() {
            @Override
            public void changed(ObservableValue<? extends Cylinder> observableValue,
                                Cylinder oldCylinder, Cylinder newCylinder) {
                if (newCylinder != null) {
                    labelBottom.setText(newCylinder.getCylinderName());
                    updateRightTable(newCylinder);
                }
            }
        });


        //  RIGHT TABLE - Context Menu
        rightContextMenu = new ContextMenu();
        MenuItem showOpMenuItem = new MenuItem("Show Details");
        showOpMenuItem.setOnAction((ActionEvent event) -> showOperation());
        MenuItem editOpMenuItem = new MenuItem("Edit Operation");
        editOpMenuItem.setOnAction((ActionEvent event) -> editOperation());
        MenuItem deleteOpMenuItem = new MenuItem("Delete Operation");
        deleteOpMenuItem.setOnAction((ActionEvent event) -> deleteOperation());
        rightContextMenu.getItems().addAll(showOpMenuItem, editOpMenuItem, deleteOpMenuItem);

        // RIGHT TABLE - Context Menu & DoubleClick
        rightTable.setRowFactory(tableView -> {
            TableRow<CylinderOperation> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(rightContextMenu);
                }
            });
            row.setOnMouseClicked(click ->{
                if ((click.getClickCount() == 2) && (!row.isEmpty())){
                    showOperation();
                }
            });
            return row;
        });

        // ComboBoxes - Statistics Tab
        updateStatisticsTab();

        statisticsBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.equals("")) {
                wellStatOilBox.setValue("");
                wellStatGasBox.setValue("");
            } else {
                statisticsBox.setValue("");
            }
            statisticsTab.processStatisticsBox(newValue);
            barChart.setTitle(newValue);
        });

        wellStatOilBox.getSelectionModel().selectedItemProperty().addListener((obs, oldWell, newWell) -> {
            if (!newWell.equals("")) {
                statisticsBox.setValue("");
                wellStatGasBox.setValue("");
                statisticsTab.processWellStatBox(newWell, "O");
                barChart.setTitle(newWell);
            }
        });

        wellStatGasBox.getSelectionModel().selectedItemProperty().addListener((obs, oldWell, newWell) -> {
            if (!newWell.equals("")) {
                statisticsBox.setValue("");
                wellStatOilBox.setValue("");
                statisticsTab.processWellStatBox(newWell, "G");
                barChart.setTitle(newWell);
            }
        });

    }

    public CylinderList getCylinderList(){
        return cylinderList;
    }

    public void setTableColumnSize(){
        leftTable.prefWidthProperty().bind(mainBorderPane.widthProperty().multiply(0.32));
        rightTable.prefWidthProperty().bind(mainBorderPane.widthProperty().multiply(0.68));

        cylNameLeftCol.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.2));
        cylStatusLeftCol.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.3));
        sampleLeftCol.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.25));
        remVolumeLeftCol.prefWidthProperty().bind(leftTable.widthProperty().multiply(0.25));

//        WellRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.13));
//        sampleRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.1));
//        operationRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.1));
//        testRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.15));
//        volumeChangeRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.12));
//        dateEnglishRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.1));
//        descriptionRightCol.prefWidthProperty().bind(rightTable.widthProperty().multiply(0.3));
    }

    private void updateRightTable(Cylinder selectedCylinder){
        if (selectedCylinder != null){
            leftTable.setStyle("-fx-selection-bar: red");
            ObservableList<CylinderOperation> operations = selectedCylinder.getOperations();
            rightTable.setItems(operations);
            WellRightCol.setCellValueFactory(cellData -> cellData.getValue().wellProperty());
            sampleRightCol.setCellValueFactory(cellData -> cellData.getValue().sampleProperty());
            operationRightCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getOperation().toString()));
            testRightCol.setCellValueFactory(cellData -> cellData.getValue().testProperty());
            volumeChangeRightCol.setCellValueFactory(cellData ->
                    new  SimpleStringProperty(String.format("%.0f", cellData.getValue().getVolumeChange())));
            dateEnglishRightCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDate_English().toString()));
            descriptionRightCol.setCellValueFactory(cellData ->
                    cellData.getValue().descriptionProperty());

            dateEnglishRightCol.setSortType(TableColumn.SortType.ASCENDING);
            rightTable.getSortOrder().add(dateEnglishRightCol);
            rightTable.sort();
        }
    }

    private void updateStatisticsTab(){
        statisticsTab = new StatisticsTab(sortedList, barChart, yAxis, xAxis);

        ObservableList<String> wellListOil = FXCollections.observableArrayList();
        ObservableList<String> wellListGas = FXCollections.observableArrayList();
        for (Cylinder cylinder : cylindersValues){
            if (cylinder.getCylinderType().equals("O") && cylinder.getOpSize() != 0){
                CylinderOperation lastOperation = cylinder.getOperations().get(cylinder.getOpSize() - 1);
                if (lastOperation.getOperation() != Operation.SERVICE){
                    if (!wellListOil.contains(lastOperation.getWell())){
                        wellListOil.add(lastOperation.getWell());
                    }
                }
            } else if (cylinder.getCylinderType().equals("G") && cylinder.getOpSize() != 0){
                CylinderOperation lastOperation = cylinder.getOperations().get(cylinder.getOpSize() - 1);
                if (lastOperation.getOperation() != Operation.SERVICE){
                    if (!wellListGas.contains(lastOperation.getWell())){
                        wellListGas.add(lastOperation.getWell());
                    }
                }
            }
        }
        wellStatOilBox.setItems(wellListOil);
        wellStatGasBox.setItems(wellListGas);
    }

    // Comparing Names of Cylinders for Sorting
    private int compareCylindersNames(String name1, String name2){
        char char1;
        char char2;
        int compared = 0;
        int index = 0;
        while (compared == 0 && index < name1.length()){
            char1 = name1.charAt(index);
            try {
                char2 = name2.charAt(index);
                if (!Character.isDigit(char1) && !Character.isDigit(char2)){
                    // comparing two non-numbers
                    compared = Character.compare(char1, char2);
                }
                else if (!Character.isDigit(char1) && Character.isDigit(char2)){
                    // letters precede numbers
                    compared = -1;
                }
                else if (Character.isDigit(char1) && !Character.isDigit(char2)){
                    // letters precede numbers
                    compared = 1;
                }
                else if (Character.isDigit(char1) && Character.isDigit(char2)){
                    // comparing two blocks of numbers
                    // find block of numbers in the first name
                    int tempIndex = index;
                    while (tempIndex < name1.length() && Character.isDigit(name1.charAt(tempIndex))){
                        tempIndex++;
                    }
                    int char1Int = Integer.parseInt(name1.substring(index, tempIndex));

                    // find block of numbers in the second name
                    tempIndex = index;
                    while (tempIndex < name2.length() && Character.isDigit(name2.charAt(tempIndex))){
                        tempIndex++;
                    }
                    int char2Int = Integer.parseInt(name2.substring(index, tempIndex));

                    compared = Integer.compare(char1Int, char2Int);
                }
            } catch (IndexOutOfBoundsException e){
                compared = 1;
            }
            index++;
        }
        if (name1.length() < name2.length() && compared == 0){
            compared = 1;
        }

        return compared;
    }

    public void searchForWellCylinder(String searchString){
        // if the cylinder has no operations, only check if its name matches
        // if the cylinder has any operations, check its last operation
        filteredList.setPredicate(p -> p.getOpSize() == 0 ?
                        p.getCylinderName().toLowerCase().contains(searchString) :
                        p.getCylinderName().toLowerCase().contains(searchString) ||
                        p.getOperations().get(p.getOpSize()-1).getWell().toLowerCase().contains(searchString));
        
        leftTable.getSelectionModel().selectFirst();
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        updateRightTable(selectedCylinder);
    }


    @FXML
    private void addNewCylinder(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Cylinder");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("NewCylinderDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)){
            NewCylinderController controller = fxmlLoader.getController();
            Cylinder newCylinder = controller.process();

            if (newCylinder != null){ // A valid name has been entered
                if (!cylinders.containsKey(newCylinder.getCylinderName())) { // The name doesn't exist in the list
                    cylinders.put(newCylinder.getCylinderName(), newCylinder);
                    cylindersValues.add(newCylinder);
                    updateStatisticsTab();
                } else {
                    showWarning("A cylinder with this name exists");
                }
            }
        }
    }

    @FXML
    private void editCylinder(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit Cylinder " + selectedCylinder.getCylinderName());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("NewCylinderDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)){
            NewCylinderController controller = fxmlLoader.getController();
            Cylinder editedCylinder = controller.process();
            if (editedCylinder != null){ // A valid name has been entered
                if (!cylinders.containsKey(editedCylinder.getCylinderName())) { // The name doesn't exist in the list
                    selectedCylinder.setCylinderName(editedCylinder.getCylinderName());
                    // update item in the list (without this line, the list is updated only after restart)
                    cylindersValues.set(cylindersValues.indexOf(selectedCylinder), selectedCylinder);
                    updateStatisticsTab();
                } else {
                    showWarning("A cylinder with this name exists");
                }
            }
        }
    }

    @FXML
    private void deleteCylinder(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Cylinder");
        alert.setHeaderText("Delete cylinder: " + selectedCylinder.getCylinderName());
        alert.setContentText("Are you sure?");

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDefaultButton(false);
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)){
            cylinders.remove(selectedCylinder.getCylinderName());
            cylindersValues.remove(selectedCylinder);
            updateStatisticsTab();
        }
    }

    @FXML
    private void addNewOperation(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        if (selectedCylinder != null) {
            if (!selectedCylinder.isReady()){ // if cylinder is out of service: show warning and don't show the dialog
                showWarning("Cylinder " + selectedCylinder.getCylinderName() + " is out of service");
                return;
            }

            dialog.setTitle("Add New Operation to " + selectedCylinder.getCylinderName());
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("NewOperationDialog.fxml"));

            try {
                dialog.getDialogPane().setContent(fxmlLoader.load());
            } catch (IOException e){
                System.out.println("Couldn't load the dialog");
                e.printStackTrace();
                return;
            }

            NewOperationController newOperationController = fxmlLoader.getController();
            newOperationController.initData(selectedCylinder, null, true);

            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && (result.get() == ButtonType.OK)){
                CylinderOperation newOperation = newOperationController.process();
                if (newOperation != null) {
                    selectedCylinder.addOperation(newOperation);
                    leftTable.refresh();
                    updateStatisticsTab();

                    updateRightTable(selectedCylinder);
                }
            }
        } else {
            showWarning("Choose a Cylinder");
        }
    }

    @FXML
    private void editOperation(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        if (selectedCylinder != null) {
            CylinderOperation selectedOperation = rightTable.getSelectionModel().getSelectedItem();
            if (selectedOperation != null) {
                int operationIndex = selectedCylinder.getOperations().indexOf(selectedOperation);
                dialog.setTitle("Edit Operation of Cylinder " + selectedCylinder.getCylinderName());
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("NewOperationDialog.fxml"));

                try {
                    dialog.getDialogPane().setContent(fxmlLoader.load());
                } catch (IOException e){
                    System.out.println("Couldn't load the dialog");
                    e.printStackTrace();
                    return;
                }

                NewOperationController editOperationController = fxmlLoader.getController();
                editOperationController.initData(selectedCylinder, selectedOperation, true);

                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && (result.get() == ButtonType.OK)){
                    CylinderOperation editedOperation = editOperationController.process();
                    if (editedOperation != null) {
                        selectedCylinder.editOperation(operationIndex, selectedOperation, editedOperation);
                        leftTable.refresh();
                        updateStatisticsTab();
                    }
                }
            } else {
                showWarning("Choose an Operation");
            }
        } else {
            showWarning("Choose a Cylinder");
        }
    }

    @FXML
    private void deleteOperation() {
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        CylinderOperation selectedOperation = rightTable.getSelectionModel().getSelectedItem();
        if (selectedOperation != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Operation");
            alert.setHeaderText("Well: " + selectedOperation.getWell() + ", Operation: " + selectedOperation.getOperation().name()
                    + "\nSample: " + selectedOperation.getSample() + ", Test: " + selectedOperation.getTest()
                    + "\nVolume Change: " + selectedOperation.getVolumeChange() + ", Date: " + selectedOperation.getDate_English());
            alert.setContentText("Are you sure?");
            
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDefaultButton(false);
            Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setDefaultButton(true);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && (result.get() == ButtonType.OK)) {
                selectedCylinder.removeOperation(selectedOperation);
                leftTable.refresh();
                updateStatisticsTab();
            }
        } else {
            showWarning("Choose an Operation");
        }
    }

    @FXML
    private void changeServiceStatus(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();

        String title = "Change Cylinder's Service Status";
        String header = "Is cylinder " + selectedCylinder.getCylinderName() +
                (selectedCylinder.isReady() ? " out of service" : " in service") + "?";
        Optional<ButtonType> result = showAlertWindow(title, header);

        if (result.isPresent() && (result.get() == ButtonType.YES)){
            boolean noOpOrService = (selectedCylinder.getOperations().size() == 0) || // if there is no operations
                    (selectedCylinder.getOperations().get(selectedCylinder.getOpSize()-1).
                            getOperation() == Operation.SERVICE); // or if the last operation is SERVICE
            if (noOpOrService){
                selectedCylinder.setReady(!selectedCylinder.isReady());
                leftTable.refresh();
                updateStatisticsTab();
            } else {
                showWarning("You must first add a service operation");
            }
        }
    }

    @FXML
    private void changeLocation() {
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Change Cylinder's Location");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("changeLocDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)){
            ChangeLocController controller = fxmlLoader.getController();
            String cylinderLoc = controller.process();
            if ((cylinderLoc != null)){
                selectedCylinder.setLocation(cylinderLoc);
                leftTable.refresh();
                updateStatisticsTab();
            }
        }
    }

    @FXML
    private void showLocation() {
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Cylinder's Location");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("changeLocDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        ChangeLocController changeLocController = fxmlLoader.getController();
        changeLocController.initData(selectedCylinder);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
    }


    @FXML
    private void needsService() {
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();

        String title = "Servicing Cylinder";
        String header = "Does cylinder " + selectedCylinder.getCylinderName() + " need to be serviced?";
        Optional<ButtonType> result = showAlertWindow(title, header);

        if (result.isPresent() && (result.get() == ButtonType.YES)){
            if ((selectedCylinder.getOperations().size() != 0)) {// if there is one or more operations
                boolean isServiced = (selectedCylinder.getOperations().get(selectedCylinder.getOpSize() - 1).
                        getOperation() == Operation.SERVICE); // if the last operation is SERVICE
                if (isServiced) {
                    String title2 = "Servicing Cylinder";
                    String header2 = selectedCylinder.getCylinderName() + " is already serviced\n" +
                            "Do you want to service it again?";
                    Optional<ButtonType> result2 = showAlertWindow(title2, header2);
                    if (result2.isPresent() && (result2.get() == ButtonType.YES)) {
                        selectedCylinder.setNeedsService(true); // if user clicks YES on the warning dialog
                    } else {
                        selectedCylinder.setNeedsService(false); // if user clicks NO on the warning dialog
                    }
                } else {
                    selectedCylinder.setNeedsService(true); // if the last operation is not SERVICE
                }
            } else {
                selectedCylinder.setNeedsService(true); // if there's no operations
            }
        }
        leftTable.refresh();
        updateStatisticsTab();
    }

    @FXML
    private void showOperation(){
        Cylinder selectedCylinder = leftTable.getSelectionModel().getSelectedItem();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        if (selectedCylinder != null) {
            CylinderOperation selectedOperation = rightTable.getSelectionModel().getSelectedItem();
            if (selectedOperation != null) {
                dialog.setTitle("Operation Details of Cylinder " + selectedCylinder.getCylinderName());
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("NewOperationDialog.fxml"));

                try {
                    dialog.getDialogPane().setContent(fxmlLoader.load());
                } catch (IOException e){
                    System.out.println("Couldn't load the dialog");
                    e.printStackTrace();
                    return;
                }

                NewOperationController newOperationController = fxmlLoader.getController();
                newOperationController.initData(selectedCylinder, selectedOperation, false);

                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                dialog.showAndWait();
            } else {
                showWarning("Choose an Operation");
            }
        } else {
            showWarning("Choose a Cylinder");
        }
    }

    @FXML
    private void showLegend(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Color Legend");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("colorLegend.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    public void openFile(){
        if(cylinderList.openFiles()) { // if both file paths selected successfully
            cylinderList.loadCylinderList();
            cylinders = cylinderList.getCylinderList();
            cylindersValues = FXCollections.observableArrayList(cylinders.values());

            // LEFT TABLE
            filteredList = new FilteredList<>(cylindersValues);
            sortedList = new SortedList<>(filteredList);
            sortedList.setComparator(allCylindersComparator);
            leftTable.setItems(sortedList);
        }
    }

    @FXML
    public void saveData(){
        cylinderList.saveCylinderList();
    }

    @FXML
    public void saveDataToNewFile(){
        cylinderList.saveFiles();
    }

    @FXML
    public void createBackup(){
        cylinderList.backupFiles();
    }

    @FXML
    public void saveExitProgram(){
        if (closeProgramDialog()){
            cylinderList.saveCylinderList();
            Platform.exit();
        }
    }

    @FXML
    public void exitProgram(){
        if (closeProgramDialog()){
            Platform.exit();
        }
    }

    public boolean closeProgramDialog(){
        Optional<ButtonType> result = showAlertWindow("Closing The Program", "Are you sure?");
        if (result.isPresent() && result.get() == ButtonType.YES) {
            return true;
        }
        return false;
    }


    private void showWarning(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.show();
    }

    private Optional<ButtonType> showAlertWindow(String title, String header){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        //Deactivate Default behavior for yes-Button:
        Button yesButton = (Button) alert.getDialogPane().lookupButton( ButtonType.YES );
        yesButton.setDefaultButton(false);
        //Activate Default behavior for no-Button:
        Button noButton = (Button) alert.getDialogPane().lookupButton( ButtonType.NO );
        noButton.setDefaultButton(true);

        return alert.showAndWait();
    }



}
