package sample;
import Logic.Logic;
import inputValidation.FilesValidation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {
    Logic m_LogicManager = new Logic();
    @FXML
    public Button btn_loadXml;
    @FXML
    public Button btn_switchRepository;
    @FXML
    public Button btn_initRepository;
    @FXML
    public TextField txtField_repositoryPath;

    public void initRepository(javafx.event.ActionEvent actionEvent){
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null && m_LogicManager.initRepository(selectedFolder)) {
            txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
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
