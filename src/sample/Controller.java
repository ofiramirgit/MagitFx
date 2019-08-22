package sample;
import Logic.Logic;
import inputValidation.FilesValidation;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import  Logic.XmlException;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Controller {

    Logic m_LogicManager = new Logic();
    private String FolderPath;
    @FXML
    private Stage newStage = new  Stage();
    @FXML
    public Button btn_loadXml;
    @FXML
    public Button btn_switchRepository;
    @FXML
    public Button btn_initRepository;
    @FXML
    public TextField txtField_repositoryPath;
    @FXML
    public TextField input_user_name;
    @FXML
    public Button btn_setUserName;

    public void initRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            FolderPath = selectedFolder.getAbsolutePath();
            Parent root = FXMLLoader.load(getClass().getResource("window.fxml"));
            newStage.setScene(new Scene(root, 300, 200));
            newStage.show();

        }
    }

    public void selectRepositoryName(javafx.event.ActionEvent actionEvent) throws IOException {
        String RepositoryName = input_user_name.getText();
        if (m_LogicManager.initRepository(FolderPath,RepositoryName)) {
            txtField_repositoryPath.setText(FolderPath+File.separator+RepositoryName);
        }
    }

    public void readXML(javafx.event.ActionEvent actionEvent) { //need to add check
        final FileChooser dc = new FileChooser();
        File selectedXML = dc.showOpenDialog(null);
        try {
            if (selectedXML != null){
                m_LogicManager.readXML(selectedXML.getAbsolutePath());
                txtField_repositoryPath.setText(selectedXML.getAbsolutePath());
            }
        } catch (XmlException e) {
            e.printStackTrace();
        }

    }


//    public void loadRepository(javafx.event.ActionEvent actionEvent) {
//        final DirectoryChooser dc = new DirectoryChooser();
//        File selectedFolder = dc.showDialog(null);
//        if (selectedFolder != null && m_FilesValidation.isRepository(selectedFolder)) {
//            txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
//            btn_loadRepository.setDisable(true);
//        }
//    }

}
