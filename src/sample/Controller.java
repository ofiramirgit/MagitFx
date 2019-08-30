package sample;
import Logic.Logic;
import com.sun.org.omg.CORBA.Repository;
import inputValidation.FilesValidation;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import  Logic.XmlException;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;

public class Controller {


    Logic m_LogicManager = new Logic();
    private String FolderPath;
    @FXML
    private Stage newStage = new Stage();
    @FXML
    public Button btn_loadXml;
    @FXML
    public Button btn_switchRepository;
    @FXML
    public Button btn_initRepository;
    @FXML
    public TextField txtField_repositoryPath;
    @FXML
    public TextField txtField_userName;
    @FXML
    public Button btn_setUserName;
    @FXML
    public Tab tab_fileCommit;
    @FXML
    public Tab tab_branches;

    public void initRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Input Repository Name");
            dialog.setHeaderText("Insert Repository Name");
            dialog.setContentText("Please enter repository name: ");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println("Repository Path: " + selectedFolder.getAbsolutePath() + result.get());
                if (m_LogicManager.initRepository(selectedFolder.getAbsolutePath(), result.get())) {
                    txtField_repositoryPath.setText(selectedFolder.getAbsolutePath() +File.separator+ result.get());
                    unDisableRepositorySection();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText("Repository Alreadt Exist!");
                    alert.setContentText("repository alerady exists.");

                    alert.showAndWait();
                }
            }
        }
    }

    public void readXML(javafx.event.ActionEvent actionEvent) { //need to add check
        final FileChooser dc = new FileChooser();
        File selectedXML = dc.showOpenDialog(null);
        Runnable task = () -> {
            try {
                if (selectedXML != null) {
                    m_LogicManager.readXML(selectedXML.getAbsolutePath());
                    txtField_repositoryPath.setText(selectedXML.getAbsolutePath());
                    unDisableRepositorySection();
                }
            } catch (XmlException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public void setUserName(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Input User Name");
        dialog.setHeaderText("Insert User Name");
        dialog.setContentText("Please enter user name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (m_LogicManager.setM_ActiveUser(result.get())) {
                txtField_userName.setText(result.get());
                System.out.println("User Name: " + result.get());
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("UnValid User Name Input");
                alert.setContentText("user name input is unvalid. 1-50 characters");
                alert.showAndWait();
            }
        }
    }
    public void switchRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            if (m_LogicManager.setM_ActiveRepository(selectedFolder.getAbsolutePath())) {
                txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
                txtField_userName.setText("Administrator");
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Repository Not Exist");
                alert.setContentText("the folder you selected isn't repository");
                alert.showAndWait();
            }
        }
    }

    public void unDisableRepositorySection()
    {
        txtField_userName.setText("Administrator");
        tab_fileCommit.setDisable(false);
        tab_branches.setDisable(false);
        btn_switchRepository.setDisable(false);
        btn_setUserName.setDisable(false);
    }
}



