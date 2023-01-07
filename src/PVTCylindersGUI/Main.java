package PVTCylindersGUI;

import PVTCylindersGUI.dataModel.CylinderList;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = loader.load();
        primaryStage.setTitle("PVT Cylinders Archive");
        primaryStage.setScene(new Scene(root, 1000, 575));
//        primaryStage.setScene(new Scene(root, java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,
//                java.awt.Toolkit.getDefaultToolkit().getScreenSize().height));
//        primaryStage.setMaximized(true);
        primaryStage.show();

        MainWindowController controller = loader.getController();
        controller.setTableColumnSize();

        primaryStage.setOnCloseRequest(evt -> {
            if (!controller.closeProgramDialog()) { // ask if the user is sure to close the program
                evt.consume(); // doesn't close
            } else { // closes and saves the list
                CylinderList cylinderList = controller.getCylinderList();
                cylinderList.saveCylinderList();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }

}
