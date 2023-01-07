package PVTCylindersGUI.dataModel;

import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatisticsTab {
    private ObservableList<Cylinder> cylinders;
    private BarChart<String, Integer> barChart;
    private NumberAxis yAxis;
    private CategoryAxis xAxis;

    private int totalCylinders;
    private XYChart.Series<String, Integer> series;
    private Map<Integer, String> barChartColors;

    public StatisticsTab(ObservableList<Cylinder> cylinders, BarChart<String, Integer> barChart,
                         NumberAxis yAxis, CategoryAxis xAxis) {
        this.cylinders = cylinders;
        this.barChart = barChart;
        this.yAxis = yAxis;
        this.xAxis = xAxis;
        this.series = new XYChart.Series<>();
        this.barChartColors = barChartDefaultColors();
    }


    // choosing cylinders for specified well
    public void processWellStatBox(String wellName, String cylinderType) {
        barChart.getData().clear();
        this.series = new XYChart.Series<>();

        ArrayList<Cylinder> selectedCylinders = new ArrayList<>();
        double maxVolume = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getCylinderType().equals(cylinderType) && cylinder.getOpSize() != 0) {
                CylinderOperation lastOperation = cylinder.getOperations().get(cylinder.getOpSize() - 1);
                if (lastOperation.getWell().equals(wellName)) {
                    selectedCylinders.add(cylinder);
                    if (cylinder.getRemainingVolume() > maxVolume) { // finding max value of the chart
                        maxVolume = cylinder.getRemainingVolume();
                    }
                }
            }
        }

        int sizeArray = selectedCylinders.size();
        if (sizeArray > 0) {
            for (int i = 0; i < sizeArray; i++) {
                Cylinder cylinder = selectedCylinders.get(i);
                StringBuilder name = new StringBuilder(cylinder.getCylinderName());
                int volume = (int) cylinder.getRemainingVolume();
                StringBuilder volumeStr = new StringBuilder("(" + volume + " ml)");

                // center text
                long nameCount = name.chars().count();
                long volumeStrCount = volumeStr.chars().count();
                long dif = nameCount - volumeStrCount;
                if (dif > 0){
                    for (int j = 1; j < dif; j++){
                        volumeStr.insert(0, " ");
                    }
                } else if (dif < 0){
                    for (int j = 1; j < -dif; j++){
                        name.insert(0, " ");
                    }
                }

                String dataName = name + "\n" + volumeStr;
                XYChart.Data<String, Integer> data = new XYChart.Data<>(dataName, volume);
                series.getData().add(data);
            }
        }

        if (cylinderType.equals("O")) { // oil
            yAxis.setTickUnit(100);
            yAxis.setUpperBound((Math.floor(maxVolume / 100) + 1) * 100);
        } else if (cylinderType.equals("G")) { // gas
            yAxis.setTickUnit(4000);
            yAxis.setUpperBound((Math.floor(maxVolume / 4000) + 1) * 4000);
        }
        yAxis.setLabel("Volume (ml)");

        barChart.getData().add(series);
        setBarColorWidth(series, sizeArray);
    }


    public void processStatisticsBox(String newValue) {
        totalCylinders = cylinders.size();
        this.series = new XYChart.Series<>();

        yAxis.setTickUnit(Math.round(totalCylinders / 5.0));
        yAxis.setUpperBound(yAxis.getTickUnit() * 6);
        yAxis.setLabel("Count");
        barChart.setTitle("     " + newValue);

        switch (newValue) {
            case "":
                barChart.getData().clear();
                break;
            case "All Cylinders":
                showAllStatistics();
                break;
            case "Empty/Filled Cylinders":
                showEmptyFilledStatistics();
                break;
            case "Oil/Gas Cylinders":
                showOilGasStatistics();
                break;
            case "In/Outside PVT":
                showInPVTStatistics();
                break;
            case "In PVT: Empty/Filled Oil Cylinders":
                showInPVTEFOilStatistics();
                break;
            case "In PVT: Empty/Filled Gas Cylinders":
                showInPVTEFGasStatistics();
                break;
            case "In PVT: Needing Service/Ready for Sampling Oil Cylinders":
                showInPVTReadyEmptyOilStatistics();
                break;
            case "In PVT: Needing Service/Ready for Sampling Gas Cylinders":
                showInPVTReadyEmptyGasStatistics();
                break;
        }
    }

    private void showAllStatistics() {
        barChart.getData().clear();
        int numOfBars = 1;

        addDataToSeries("All Cylinders", totalCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }


    private void showOilGasStatistics() {
        barChart.getData().clear();
        int numOfBars = 2;
        int oilCylinders = 0;
        int gasCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getCylinderType().equals("O")) {
                oilCylinders++;
            } else if (cylinder.getCylinderType().equals("G")) {
                gasCylinders++;
            }
        }

        addDataToSeries("Oil", oilCylinders);
        addDataToSeries("Gas", gasCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void showEmptyFilledStatistics() {
        barChart.getData().clear();
        int numOfBars = 2;
        int emptyCylinders = 0;
        int filledCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getRemainingVolume() == 0) {
                emptyCylinders++;
            } else if (cylinder.getRemainingVolume() > 0) {
                filledCylinders++;
            }
        }

        addDataToSeries("Empty", emptyCylinders);
        addDataToSeries("Filled", filledCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void showInPVTStatistics() {
        barChart.getData().clear();
        int numOfBars = 2;
        int insidePVT = 0;
        int outsidePVT = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                insidePVT++;
            } else {
                outsidePVT++;
            }
        }

        addDataToSeries("In PVT", insidePVT);
        addDataToSeries("Outside PVT", outsidePVT);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void showInPVTEFOilStatistics() { // In PVT, empty/full oil
        barChart.getData().clear();
        int numOfBars = 2;
        int inEmptyOilCylinders = 0;
        int inFilledOilCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                if (cylinder.getCylinderType().equals("O")) {
                    if (cylinder.getRemainingVolume() == 0) {
                        inEmptyOilCylinders++;
                    } else {
                        inFilledOilCylinders++;
                    }
                }
            }
        }

        addDataToSeries("Empty", inEmptyOilCylinders);
        addDataToSeries("Filled", inFilledOilCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void showInPVTEFGasStatistics() { // In PVT, empty/full gas
        barChart.getData().clear();
        int numOfBars = 2;
        int inEmptyGasCylinders = 0;
        int inFilledGasCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                if (cylinder.getCylinderType().equals("G")) {
                    if (cylinder.getRemainingVolume() == 0) {
                        inEmptyGasCylinders++;
                    } else {
                        inFilledGasCylinders++;
                    }
                }
            }
        }

        addDataToSeries("Empty", inEmptyGasCylinders);
        addDataToSeries("Filled", inFilledGasCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void showInPVTReadyEmptyOilStatistics() {
        barChart.getData().clear();
        int numOfBars = 2;
        int needServiceOilCylinders = 0;
        int inReadyEmptyOilCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                if (cylinder.getCylinderType().equals("O")) {
                    if (cylinder.getRemainingVolume() == 0) {
                        if (cylinder.isReady()) {
                            inReadyEmptyOilCylinders++;
                        }
                    }
                    if (cylinder.isNeedingService()) {
                        needServiceOilCylinders++;
                    }
                }
            }
        }

        addDataToSeries("Needing Service", needServiceOilCylinders);
        addDataToSeries("Ready", inReadyEmptyOilCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }


    private void showInPVTReadyEmptyGasStatistics() {
        barChart.getData().clear();
        int numOfBars = 2;
        int needServiceGasCylinders = 0;
        int inReadyEmptyGasCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                if (cylinder.getCylinderType().equals("G")) {
                    if (cylinder.getRemainingVolume() == 0) {
                        if (cylinder.isReady()) {
                            inReadyEmptyGasCylinders++;
                        }
                    }
                    if (cylinder.isNeedingService()) {
                        needServiceGasCylinders++;
                    }
                }
            }
        }

        addDataToSeries("Needing Service", needServiceGasCylinders);
        addDataToSeries("Ready", inReadyEmptyGasCylinders);
        barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));

        barChart.getData().add(series);
        setBarColorWidth(series, numOfBars);
    }

    private void addDataToSeries(String dataName, int dataValue) {
        StringBuilder nameStr = new StringBuilder(dataName);
        StringBuilder valueStr = new StringBuilder("(" + dataValue + " cyl)");

        // center text
        long nameCount = nameStr.chars().count();
        long valueStrCount = valueStr.chars().count();
        long dif = nameCount - valueStrCount;
        if (dif > 0){
            for (int j = 1; j < dif; j++){
                valueStr.insert(0, " ");
            }
        } else if (dif < 0){
            for (int j = 1; j < -dif; j++){
                nameStr.insert(0, " ");
            }
        }

        String barName = nameStr + "\n" + valueStr;
        XYChart.Data<String, Integer> data = new XYChart.Data<>(barName, dataValue);
        series.getData().add(data);
    }


    private Map<Integer, String> barChartDefaultColors() {
        barChartColors = new HashMap<>();
        barChartColors.put(0, "-fx-bar-fill: DODGERBLUE;");
        barChartColors.put(1, "-fx-bar-fill: DARKRED;");
        barChartColors.put(2, "-fx-bar-fill: SEAGREEN;");
        barChartColors.put(3, "-fx-bar-fill: GREY;");
        barChartColors.put(4, "-fx-bar-fill: ORANGE;");
        barChartColors.put(5, "-fx-bar-fill: DARKMAGENTA;");
        barChartColors.put(6, "-fx-bar-fill: BURLYWOOD;");
        barChartColors.put(7, "-fx-bar-fill: CHARTREUSE;");
        barChartColors.put(8, "-fx-bar-fill: RED;");
        barChartColors.put(9, "-fx-bar-fill: BLUE;");
        barChartColors.put(10, "-fx-bar-fill: GOLDENROD;");
        barChartColors.put(11, "-fx-bar-fill: BISQUE;");
        barChartColors.put(12, "-fx-bar-fill: LIGHTCORAL;");
        barChartColors.put(13, "-fx-bar-fill: LIMEGREEN;");
        barChartColors.put(14, "-fx-bar-fill: MAROON;");
        barChartColors.put(15, "-fx-bar-fill: MINTCREAM;");
        barChartColors.put(16, "-fx-bar-fill: ROSYBROWN;");
        barChartColors.put(17, "-fx-bar-fill: SILVER;");
        barChartColors.put(18, "-fx-bar-fill: TOMATO;");
        barChartColors.put(19, "-fx-bar-fill: PINK;");
        barChartColors.put(20, "-fx-bar-fill: ORCHID;");

        return barChartColors;
    }

    private void setBarColorWidth(XYChart.Series<String, Integer> series, int dataSize){
        // set bar color
        for (int j = 0; j < series.getData().size(); j++){
            XYChart.Data<String, Integer> data = series.getData().get(j);
            data.getNode().setStyle(barChartColors.get(j));
        }

        // set bar width
        switch (dataSize){
            case 1:
                barChart.setCategoryGap(xAxis.getWidth() / 1.5);
                break;
            case 2:
                barChart.setCategoryGap(xAxis.getWidth() / 3);
                break;
            case 3:
                barChart.setCategoryGap(xAxis.getWidth() / 6);
                break;
            default:
                barChart.setCategoryGap(Math.min((xAxis.getWidth()/(dataSize)),30));
                break;
        }
    }

}
