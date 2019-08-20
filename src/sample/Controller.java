package sample;
import Logic.Logic;
import inputValidation.FilesValidation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import  Logic.XmlException;

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
