package PVTCylindersGUI.dataModel;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Iterator;

public class Cylinder {
    private SimpleStringProperty cylinderName;
    private final SimpleStringProperty cylinderType;              // O:oil - G:gas
    private SimpleDoubleProperty remainingVolume;
    private ObservableList<CylinderOperation> operations;
    private boolean ready;
    private boolean needsService;
    private SimpleStringProperty location;
    private int opSize;

    public Cylinder(String cylinderName, String cylinderType) {
        this.cylinderName = new SimpleStringProperty(cylinderName);
        this.cylinderType = new SimpleStringProperty(cylinderType);
        this.remainingVolume = new SimpleDoubleProperty(0);
        this.operations = FXCollections.observableArrayList();
        this.ready = true;
        this.needsService = false;
        this.location = new SimpleStringProperty("PVT");
        this.opSize = 0;
    }

    public Cylinder(String cylinderName, String cylinderType, double remainingVolume,
                    boolean ready, boolean needsService, String location) {
        this.cylinderName = new SimpleStringProperty(cylinderName);
        this.cylinderType = new SimpleStringProperty(cylinderType);
        this.remainingVolume = new SimpleDoubleProperty(remainingVolume);
        this.operations = FXCollections.observableArrayList();
        this.ready = ready;
        this.needsService = needsService;
        this.location = new SimpleStringProperty(location);
        this.opSize = 0;
    }

    public String getCylinderName() {
        return cylinderName.get();
    }

    public SimpleStringProperty cylinderNameProperty() {
        return cylinderName;
    }


    public String getCylinderType() {
        return cylinderType.get();
    }

    public SimpleStringProperty cylinderTypeProperty() {
        return cylinderType;
    }

    public double getRemainingVolume() {
        return remainingVolume.get();
    }

    public SimpleDoubleProperty remainingVolumeProperty() {
        return remainingVolume;
    }

    public ObservableList<CylinderOperation> getOperations() {
        return operations;
    }

    public String isReadyToService() {
        if (ready){
            return "Ready to Service";
        } else {
            return "Out of Service";
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void setCylinderName(String cylinderName) {
        this.cylinderName.set(cylinderName);
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isNeedingService() {
        return needsService;
    }

    public void setNeedsService(boolean needsService) {
        this.needsService = needsService;
    }

    public int getOpSize() {
        return opSize;
    }

    public String getLocation() {
        return location.get();
    }

    public SimpleStringProperty locationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public void addOperation(CylinderOperation operation) {
        this.opSize++;
        if (operation.getOperation() == Operation.NEW_SAMPLE) {
            remainingVolume = new SimpleDoubleProperty(operation.getVolumeChange());
        } else if (operation.getOperation() == Operation.SERVICE) {
            operation.setVolumeChange((remainingVolume.get() == 0) ? 0 : -remainingVolume.get());
            remainingVolume = new SimpleDoubleProperty(0);
            needsService = false;
            operation.setWell("NA");
            operation.setSample("Empty");
        } else {
            remainingVolume = new SimpleDoubleProperty(remainingVolume.get() + operation.getVolumeChange());
        }

        operations.add(operation);
    }

    public void editOperation(int operationIndex, CylinderOperation selectedOperation,
                              CylinderOperation editedOperation){
        remainingVolume = new SimpleDoubleProperty(remainingVolume.get() +
                Math.abs(selectedOperation.getVolumeChange()) + editedOperation.getVolumeChange());
        getOperations().set(operationIndex, editedOperation);
    }

    public void removeOperation(CylinderOperation removedOperation){
        this.opSize--;
        operations.remove(removedOperation);

        remainingVolume = new SimpleDoubleProperty(remainingVolume.get() +
                Math.abs(removedOperation.getVolumeChange())) ;
    }

    public void printOperations() {
        Iterator<CylinderOperation> iterator = operations.iterator();
        System.out.println("*****List of operations performed on " + cylinderName.get() + "*****");
        int i = 1;
        while (iterator.hasNext()) {
            System.out.println("operation " + i++ + ": " + iterator.next());
        }
        System.out.println("Remaining Volume = " + remainingVolume.get());
    }

    @Override
    public String toString() {
        String str = String.format("%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t",
                getCylinderName(),
                getCylinderType(),
                getRemainingVolume(),
                getOpSize(),
                isReady(),
                isNeedingService(),
                getLocation());

        return str;
    }
}
