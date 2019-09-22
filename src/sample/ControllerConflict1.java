package sample;

import Logic.Conflict;
import Logic.ConstantsEnums;
import Logic.Logic;
import Logic.FileStruct;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ControllerConflict1 implements Initializable {
    private Conflict selectedConflict;
    private String repositoryPath;
    private Logic m_Logic = new Logic();
    @FXML
    private TextArea textArea_theirs;
    @FXML
    private TextArea textArea_ours;
    @FXML
    private TextArea textArea_parent;
    @FXML
    private TextArea textArea_new;
    @FXML
    private CheckBox checkbox_deleteFile;
    @FXML
    private Button btn_submit;
    FileChooser fileChooser = new FileChooser();
    public void initData(Conflict conflict, String RepositoryPath, ListView listviewConflict,int index)
    {
        selectedConflict = conflict;
        repositoryPath = RepositoryPath;
        setTextArea(conflict.getM_theirs(),textArea_theirs);
        setTextArea(conflict.getM_our(),textArea_ours);
        setTextArea(conflict.getM_theirs(),textArea_parent);
        checkbox_deleteFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue)
                    textArea_new.setVisible(false);
                else
                    textArea_new.setVisible(true);
            }
        });

        btn_submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(checkbox_deleteFile.isSelected())
                {
                    //delete file
                    if(conflict.getM_our().getM_fileState()!= ConstantsEnums.FileState.DELETED)
                    {
                        File fileToDelete = new File(conflict.getM_our().getM_filePath().toString());
                        fileToDelete.delete();
                        //need to delete from wc
                    }
                }
                else
                {
                    if(conflict.getM_our().getM_fileState()!= ConstantsEnums.FileState.DELETED)
                    {
                        File fileToDelete = new File(conflict.getM_our().getM_filePath().toString());
                        fileToDelete.delete();
                        //need to delete from wc the original
                    }
                    fileChooser.setTitle("save file");
                    fileChooser.setInitialDirectory(new File(repositoryPath));
                    try {
                        File file = fileChooser.showSaveDialog(null);
                        Path newFile = Paths.get(file.toString());
                        try {
                            if (!Files.exists(newFile))
                                Files.createFile(newFile);
                            Files.write(newFile, textArea_new.getText().getBytes());
                        } catch (Exception ex) {//todo

                        }
                    }catch(Exception ex)
                    {

                    }
                }
                listviewConflict.getItems().remove(index);
                listviewConflict.refresh();
                ((Node)(event.getSource())).getScene().getWindow().hide();
//                Stage stage = (Stage) btn_submit.getScene().getWindow();
//                stage.close();
            }
        });

    }

    private void setTextArea(FileStruct i_conflict,TextArea i_textArea) {
        if(i_conflict.getM_fileState()!= ConstantsEnums.FileState.DELETED)
        {
            i_textArea.setText(m_Logic.getContentOfFile(new File(i_conflict.getM_filePath().toString())));
        }else
        {
            checkbox_deleteFile.setVisible(true);
            i_textArea.setText("DELETED");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
