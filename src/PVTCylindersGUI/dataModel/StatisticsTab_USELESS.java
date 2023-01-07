package PVTCylindersGUI.dataModel;

import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class StatisticsTab_USELESS {
    private ObservableList<Cylinder> cylinders;
    private StackPane stackPane;
    private BarChart<String, Integer> barChart;
    private NumberAxis yAxis;
    private CategoryAxis xAxis;

    private int totalCylinders;
    private XYChart.Series<String, Integer> series;

    public StatisticsTab_USELESS(ObservableList<Cylinder> cylinders, StackPane stackPane, BarChart<String, Integer> barChart,
                         NumberAxis yAxis, CategoryAxis xAxis) {
        this.cylinders = cylinders;
        this.stackPane = stackPane;
        this.barChart = barChart;
        this.yAxis = yAxis;
        this.xAxis = xAxis;
        this.series = new XYChart.Series<>();
    }

    public void processWellStatBox(String newValue, String cylinderType){
        barChart.getData().clear();
        this.series = new XYChart.Series<>();
        stackPane.getChildren().removeIf(item -> item instanceof Text);

        ArrayList<Cylinder> selectedCylinders = new ArrayList<>();
        double maxVolume = 0;

        for (Cylinder cylinder : cylinders){
            if (cylinder.getCylinderType().equals(cylinderType) && cylinder.getOpSize() != 0){
                CylinderOperation lastOperation = cylinder.getOperations().get(cylinder.getOpSize() - 1);
                if (lastOperation.getWell().equals(newValue)){
                    selectedCylinders.add(cylinder);
                    if (cylinder.getRemainingVolume() > maxVolume){
                        maxVolume = cylinder.getRemainingVolume();
                    }
                }
            }
        }

        int sizeArray = selectedCylinders.size();
        if (sizeArray > 0){
            for (int i = 0; i < sizeArray; i++){
                Cylinder cylinder = selectedCylinders.get(i);
                addDataToSeries(cylinder.getCylinderName(), (int) cylinder.getRemainingVolume(),
                        sizeArray, i+1);
            }
        }

        if (cylinderType.equals("O")) {
            yAxis.setTickUnit(100);
            yAxis.setUpperBound((Math.floor(maxVolume / 100) + 1) * 100);
        } else if (cylinderType.equals("G")){
            yAxis.setTickUnit(4000);
            yAxis.setUpperBound((Math.floor(maxVolume / 4000) + 1) * 4000);
        }
        yAxis.setLabel("Volume (ml)");

        barChart.getData().add(series);
    }


    public void processStatisticsBox(String newValue) {
        totalCylinders = cylinders.size();
        this.series = new XYChart.Series<>();

        yAxis.setTickUnit(Math.round(totalCylinders / 5.0));
        yAxis.setUpperBound(yAxis.getTickUnit() * 6);
        yAxis.setLabel("Count");

        stackPane.getChildren().removeIf(item -> item instanceof Text);

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
        int barIndex = 1;

        addDataToSeries("All Cylinders", totalCylinders, numOfBars, barIndex);

        barChart.getData().add(series);
    }


    private void showOilGasStatistics() {
        barChart.getData().clear();
        int oilCylinders = 0;
        int gasCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getCylinderType().equals("O")) {
                oilCylinders++;
            } else if (cylinder.getCylinderType().equals("G")) {
                gasCylinders++;
            }
        }

        addDataToSeries("Oil", oilCylinders, 2, 1);
        addDataToSeries("Gas", gasCylinders, 2, 2);

        barChart.getData().add(series);
    }

    private void showEmptyFilledStatistics() {
        barChart.getData().clear();
        int emptyCylinders = 0;
        int filledCylinders = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getRemainingVolume() == 0) {
                emptyCylinders++;
            } else if (cylinder.getRemainingVolume() > 0) {
                filledCylinders++;
            }
        }

        addDataToSeries("Empty", emptyCylinders, 2, 1);
        addDataToSeries("Filled", filledCylinders, 2, 2);

        barChart.getData().add(series);
    }

    private void showInPVTStatistics() {
        barChart.getData().clear();
        int insidePVT = 0;
        int outsidePVT = 0;

        for (Cylinder cylinder : cylinders) {
            if (cylinder.getLocation().equals("PVT")) {
                insidePVT++;
            } else {
                outsidePVT++;
            }
        }

        addDataToSeries("In PVT", insidePVT, 2, 1);
        addDataToSeries("Outside PVT", outsidePVT, 2, 2);

        barChart.getData().add(series);
    }

    private void showInPVTEFOilStatistics() { // In PVT, empty/full oil
        barChart.getData().clear();
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

        addDataToSeries("Empty", inEmptyOilCylinders, 2, 1);
        addDataToSeries("Filled", inFilledOilCylinders, 2, 2);

        barChart.getData().add(series);
    }

    private void showInPVTEFGasStatistics() { // In PVT, empty/full gas
        barChart.getData().clear();
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

        addDataToSeries("Empty", inEmptyGasCylinders, 2, 1);
        addDataToSeries("Filled", inFilledGasCylinders, 2, 2);

        barChart.getData().add(series);
    }

    private void showInPVTReadyEmptyOilStatistics() {
        barChart.getData().clear();
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

        addDataToSeries("Needing Service", needServiceOilCylinders, 2, 1);
        addDataToSeries("Ready", inReadyEmptyOilCylinders, 2, 2);

        barChart.getData().add(series);
    }


    private void showInPVTReadyEmptyGasStatistics() {
        barChart.getData().clear();
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

        addDataToSeries("Needing Service", needServiceGasCylinders, 2, 1);
        addDataToSeries("Ready", inReadyEmptyGasCylinders, 2, 2);

        barChart.getData().add(series);
    }

    private void addDataToSeries(String dataName, int dataValue, int numOfBars, int barIndex) {
        XYChart.Data<String, Integer> data = new XYChart.Data<>(dataName, dataValue);
        displayLabelForData(data, numOfBars, barIndex);
//        data.nodeProperty().addListener(new ChangeListener<Node>() {
//            @Override
//            public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node node) {
//                if (node != null) {
//                    displayLabelForData(data, numOfBars, barIndex);
//                }
//            }
//        });
        series.getData().add(data);
    }

    private void displayLabelForData(XYChart.Data<String, Integer> data, int numOfBars, int barIndex) {
        Text dataText = new Text(data.getYValue() + "");
        dataText.setManaged(false);


//        Bounds bounds = stackPane.getLayoutBounds();
        double yCoeff = 0.5;
        double width;
        double barWidth;


        switch (numOfBars) { // Charts having one bar
            case 1:
                barChart.setCategoryGap(xAxis.getWidth() == 0 ?
                        stackPane.getLayoutBounds().getWidth() / 2 : xAxis.getWidth() / 2);
                barWidth = (xAxis.getWidth() - numOfBars * barChart.getCategoryGap()) / numOfBars;
                width = (barChart.getCategoryGap() + barWidth - barChart.getBarGap()) * (barIndex - 1);
//                width = xAxis.getWidth() / 2 - barChart.getCategoryGap() + yAxis.getWidth() + barChart.getBarGap();
                setDataTextPosition(dataText, width, yCoeff);
                break;

            case 2: // Charts having two bars
            case 3:
                barChart.setCategoryGap(xAxis.getWidth() / (numOfBars + 2));
                barWidth = (xAxis.getWidth() - numOfBars * barChart.getCategoryGap()) / numOfBars;
                width = (barChart.getCategoryGap() + barWidth - barChart.getBarGap()) * (barIndex - 1);
//                width = (xAxis.getWidth() - yAxis.getWidth()) / numOfBars * (barIndex - 1);
                setDataTextPosition(dataText, width, yCoeff);
                break;
        }


        stackPane.getChildren().add(dataText);
    }

    private void setDataTextPosition(Text dataText, double width,double yCoeff){

        double firstBarX = barChart.getCategoryGap() + yAxis.getWidth() - barChart.getBarGap();

        dataText.setLayoutX(
                Math.round(firstBarX + width));
        dataText.setLayoutY(
                Math.round(yAxis.getHeight() * yCoeff));

        System.out.println("barChart.getCategoryGap() = " + barChart.getCategoryGap());
        System.out.println("firstBArx = " + dataText.getLayoutX());
        System.out.println("yAxis.getWidth() = " + yAxis.getWidth());

//        stackPane.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
//            @Override
//            public void changed(ObservableValue<? extends Bounds> observableValue, Bounds b1, Bounds b2) {
//                dataText.setLayoutX(
//                        Math.round(firstBarX + width));
//                dataText.setLayoutY(
//                        Math.round(yAxis.getHeight() * yCoeff));
//            }
//        });
    }

}
