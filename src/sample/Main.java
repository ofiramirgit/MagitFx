package sample;

import Logic.Logic;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import node.CommitNode;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Main extends Application {
    private Logic m_LogicManager= new Logic();
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("fxml1.fxml"));
        primaryStage.setTitle("Magit");
        primaryStage.setScene(new Scene(root, 600, 400));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
