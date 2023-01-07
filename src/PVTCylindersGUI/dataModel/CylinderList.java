package PVTCylindersGUI.dataModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CylinderList {
    private ObservableMap<String, Cylinder> cylinders;
    private String fileNameCylinders = "PVT-Cylinders.txt";
    private String fileNameCylOperations = "PVT-Operations.txt";
    private Path pathCylinders = Paths.get(fileNameCylinders);
    private Path pathOperations = Paths.get(fileNameCylOperations);

    public CylinderList() {
        this.cylinders = FXCollections.observableHashMap();
    }

    public ObservableMap<String, Cylinder> getCylinderList() {
        return this.cylinders;
    }

    public ObservableMap<String, Cylinder> getCylinders() {
        return cylinders;
    }

    public void addCylinder(Cylinder cylinder){
        cylinders.put(cylinder.getCylinderName(),cylinder);
    }

    public Path getPathCylinders() {
        return pathCylinders;
    }

    public void setPathCylinders(Path pathCylinders) {
        this.pathCylinders = pathCylinders;
    }

    public Path getPathOperations() {
        return pathOperations;
    }

    public void setPathOperations(Path pathOperations) {
        this.pathOperations = pathOperations;
    }

    public void loadCylinderList(){
        String inputCylinders;
        String inputOperations;
        try (BufferedReader readCylindersbr = Files.newBufferedReader(pathCylinders);
             BufferedReader readOperationsbr = Files.newBufferedReader(pathOperations)) {

            if (!cylinders.isEmpty()){
                cylinders.clear();
            }

            int index1 = 0;
            int index2 = 0;
            while ((inputCylinders = readCylindersbr.readLine()) != null) {
                String[] itemsCylinders = inputCylinders.split("\t");
                String cylinderName = itemsCylinders[0];
                String cylinderType = itemsCylinders[1];
                double remainingVolume = Double.parseDouble(itemsCylinders[2]);
                int opSize = Integer.parseInt(itemsCylinders[3]);
                boolean ready = Boolean.parseBoolean(itemsCylinders[4]);
                boolean needsService = Boolean.parseBoolean(itemsCylinders[5]);
                String location = itemsCylinders[6];
                Cylinder cylinder = new Cylinder(cylinderName, cylinderType, remainingVolume, ready,
                        needsService, location);

                index1 += opSize;
                if (opSize != 0){
                    for (int k = index2; k < index1; k++){
                        inputOperations = readOperationsbr.readLine();
                        String[] itemsOperations = inputOperations.split("\t");
                        cylinder.addOperation(readOperation(itemsOperations));
                    }
                }
                index2 = index1;

                addCylinder(cylinder);
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            openFiles();
            loadCylinderList();
        }
    }

    public CylinderOperation readOperation(String[] itemsOperations){
        String well = itemsOperations[1];
        String sample = itemsOperations[2];
        String strOperation = itemsOperations[3];
        Operation operation = null;
        switch (strOperation) {
            case "Service":
                operation = Operation.SERVICE;
                break;
            case "New Sample":
                operation = Operation.NEW_SAMPLE;
                break;
            case "Test":
                operation = Operation.TEST;
                break;
            case "Inspection":
                operation = Operation.INSPECTION;
                break;
        }
        String test = itemsOperations[4];
        double volumeChange = Double.parseDouble(itemsOperations[5]);
        String description = itemsOperations[6].replace("\\n", "\n");

        LocalDate date_English = LocalDate.parse(itemsOperations[7], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        CylinderOperation tempOperation = new CylinderOperation(well, sample, operation, test, volumeChange,
                description, date_English);
        return tempOperation;
    }

    public void saveCylinderList(){
        try (BufferedWriter writeCylindersbw = Files.newBufferedWriter(pathCylinders);
             BufferedWriter writeOperationsbw = Files.newBufferedWriter(pathOperations)){
            for (ObservableMap.Entry<String,Cylinder> entry : getCylinders().entrySet()){
                String cylinderName = entry.getKey();
                Cylinder cylinder = entry.getValue();
                writeCylindersbw.write(cylinder.toString());
                writeCylindersbw.newLine();

                ObservableList<CylinderOperation> operations = cylinder.getOperations();
                Iterator<CylinderOperation> iterator = operations.iterator();
                while (iterator.hasNext()) {
                    writeOperationsbw.write(cylinderName + '\t');
                    writeOperationsbw.write(iterator.next().toString());
                    writeOperationsbw.newLine();
                }

            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean openFiles(){
        FileChooser chooser = createFileChooser();
        // choosing cylinders file
        chooser.setTitle("Choose Cylinders File");
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            setPathCylinders(Paths.get(file.getPath()));
            // choosing operations file
            chooser.setTitle("Choose Cylinder Operations File");
            chooser.setInitialDirectory(file.getParentFile());
            file = chooser.showOpenDialog(null);
            if (file != null) {
                setPathOperations(Paths.get(file.getPath()));
                return true;
            }
        }
        return false;
    }

    public void saveFiles(){
        FileChooser chooser = createFileChooser();
        // choosing cylinders file
        chooser.setTitle("Choose Cylinders File");
        File fileCylinders = chooser.showSaveDialog(null);
        if (fileCylinders != null) {
            // choosing operations file
            chooser.setTitle("Choose Cylinder Operations File");
            chooser.setInitialDirectory(fileCylinders.getParentFile());
            File fileOperations = chooser.showSaveDialog(null);

            if (fileOperations != null) {
                setPathCylinders(Paths.get(fileCylinders.getPath()));
                setPathOperations(Paths.get(fileOperations.getPath()));

                saveCylinderList();
            }
        }
    }

    public void backupFiles(){
        Path cylDestPath = Paths.get(pathCylinders.toAbsolutePath().getParent().toString(),"backup-Cylinders.txt");
        Path operDestPath = Paths.get(pathOperations.toAbsolutePath().getParent().toString(), "backup-Operations.txt");
        try {
            Files.copy(pathCylinders, cylDestPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(pathOperations, operDestPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private FileChooser createFileChooser(){
        FileChooser chooser = new FileChooser();
        // Extension filter
        FileChooser.ExtensionFilter extensionFilter =
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        chooser.getExtensionFilters().add(extensionFilter);
        //Set to user directory or go to default if cannot access
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        if(!userDirectory.canRead()) {
            userDirectory = new File("c:/");
        }
        chooser.setInitialDirectory(userDirectory);

        return chooser;
    }

}
