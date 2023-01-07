package PVTCylindersGUI.dataModel;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CylinderOperation  {
    private SimpleStringProperty well;
    private SimpleStringProperty sample; // oil, gas, N2, water, ...
    private SimpleStringProperty test;
    private SimpleDoubleProperty volumeChange;
    private SimpleStringProperty description;
    private LocalDate date_English;
    private Operation operation;

    public CylinderOperation(String well, String sample, Operation operation, double volumeChange) {
        this.well = new SimpleStringProperty(well);
        this.sample = new SimpleStringProperty(sample);
        this.operation = operation;
        this.test = new SimpleStringProperty();
        this.volumeChange = new SimpleDoubleProperty(volumeChange);
        this.description = new SimpleStringProperty();
        this.date_English = LocalDate.now();
    }

    public CylinderOperation(String well, String sample, Operation operation, String test,
                             double volumeChange, String description, LocalDate date_English) {
        this.well = new SimpleStringProperty(well);
        this.sample = new SimpleStringProperty(sample);
        this.operation = operation;
        this.test = new SimpleStringProperty(test);
        this.volumeChange = new SimpleDoubleProperty(volumeChange);
        this.description = new SimpleStringProperty(description);
        this.date_English = date_English;
    }

    public String getWell() {
        return well.get();
    }

    public SimpleStringProperty wellProperty() {
        return well;
    }

    public String getSample() {
        return sample.get();
    }

    public SimpleStringProperty sampleProperty() {
        return sample;
    }

    public String getTest() {
        return test.get();
    }

    public SimpleStringProperty testProperty() {
        return test;
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public double getVolumeChange() {
        return volumeChange.get();
    }

    public SimpleDoubleProperty volumeChangeProperty() {
        return volumeChange;
    }

    public LocalDate getDate_English() {
        return date_English;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setWell(String well) {
        this.well = new SimpleStringProperty(well);
    }

    public void setSample(String sample) {
        this.sample.set(sample);
    }

    public void setTest(String test) {
        this.test = new SimpleStringProperty(test);
    }

    public void setVolumeChange(double volumeChange) {
        this.volumeChange = new SimpleDoubleProperty(volumeChange);
    }

    public void setDescription(String description) {
        this.description = new SimpleStringProperty(description);
    }

    public void setDate_English(String dateStr) {
        date_English = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String toString() {
        String descriptionEdited = description.get().replace("\n", "\\n");

        return  well.get() + '\t' + sample.get() + '\t' +
                operation + '\t' +
                test.get() + '\t' + volumeChange.get() + '\t' +
                descriptionEdited + '\t' + date_English;
    }

}

