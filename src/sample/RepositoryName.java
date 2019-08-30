package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;

public class RepositoryName {
//    private RepositoryName insertRepositoryName;
    @FXML
    public Label firstNameLabel;
    public void initData(String RepositoryFolder){
//        insertRepositoryName=repositoryName;
        firstNameLabel.setText(RepositoryFolder.toString());
    }
}
