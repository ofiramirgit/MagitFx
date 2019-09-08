package sample;
import Logic.Logic;
import Logic.Objects.BranchData;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.org.omg.CORBA.Repository;
import inputValidation.FilesValidation;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import  Logic.XmlException;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static Logic.ConstantsEnums.EmptyString;
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;
import Logic.XmlException;

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
    public Button btn_mergeBranches;
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
    @FXML
    public TextArea textArea;

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
                    txtField_repositoryPath.setText(selectedFolder.getAbsolutePath() + File.separator + result.get());
                    unDisableRepositorySection();
                } else {
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
        Boolean exist=false;
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
                System.out.println("Repository Already Exist!");
            }
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    public void switchRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            if (m_LogicManager.setM_ActiveRepository(selectedFolder.getAbsolutePath())) {
                txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
                txtField_userName.setText("Administrator");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Repository Not Exist");
                alert.setContentText("the folder you selected isn't repository");
                alert.showAndWait();
            }
        }
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
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("UnValid User Name Input");
                alert.setContentText("user name input is unvalid. 1-50 characters");
                alert.showAndWait();
            }
        }
    }

    public void unDisableRepositorySection() {
        txtField_userName.setText("Administrator");
        tab_fileCommit.setDisable(false);
        tab_branches.setDisable(false);
        btn_switchRepository.setDisable(false);
        btn_setUserName.setDisable(false);
    }

    public void createCommit(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Commit Message");
        dialog.setHeaderText("Insert Commit Message");
        dialog.setContentText("Please enter Commit Message: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            m_LogicManager.createCommit(result.get());
        }
    }
    public void showBranchList(javafx.event.ActionEvent actionEvent) {
        List<BranchData> BranchesList = m_LogicManager.GetAllBranchesDetails();
        String stringToShow = "";
        for (BranchData branch : BranchesList)
            stringToShow += branch.toString();
        textArea.setText(stringToShow);
    }

    public void createNewBranch(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Create New Branch");
        dialog.setHeaderText("Insert Branch Name");
        dialog.setContentText("Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!m_LogicManager.createNewBranch(result.get())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error! Branch Name Exist!");
                alert.setContentText("Error! Branch Name Exist!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Information");
                alert.setHeaderText("Branch created successfully.");
                alert.setContentText("Do you want to check out active branch and load this branch?");

                ButtonType buttonYes = new ButtonType("Yes");
                ButtonType buttonNo = new ButtonType("No");

                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonTypeCancel);

                Optional<ButtonType> resultYesNo = alert.showAndWait();
                if (resultYesNo.get() == buttonYes) {
                    if (m_LogicManager.WcNotChanged()) {
                        m_LogicManager.CheckOutHeadBranch(result.get(), false, EmptyString);
                        Alert succes = new Alert(Alert.AlertType.INFORMATION);
                        succes.setTitle("Head branch checked out successfully.");
                        succes.setHeaderText("Head branch checked out successfully.");
                        succes.setContentText("Head branch checked out successfully.");
                        alert.showAndWait();
                    } else if (resultYesNo.get() == buttonNo) {
                        System.out.println("There is open changes. check out branch failed.");
                        Alert warrning = new Alert(Alert.AlertType.WARNING);
                        warrning.setTitle("Check Out Branch Faild");
                        warrning.setHeaderText("There is open changes. check out branch failed.");
                        warrning.setContentText("There is open changes. check out branch failed.");

                        warrning.showAndWait();
                    } else {
                        // ... user chose CANCEL or closed the dialog
                    }
                }
            }
        }
    }

    public void mergeBranches(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Input Branch Name");
        dialog.setHeaderText("Insert Branch Name");
        dialog.setContentText("Please enter repository name: ");
        Optional<String> result = dialog.showAndWait();
        //todo validation input
        //todo no open changes
        m_LogicManager.MergeBranches(m_LogicManager.getBranchActiveName(),result.get());
    }

    public void deleteExistBranch(javafx.event.ActionEvent actionEvent){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Create New Branch");
        dialog.setHeaderText("Insert Branch Name");
        dialog.setContentText("Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if(!m_LogicManager.deleteBranch(result.get()))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Branch is Active!");
            alert.setContentText("the branch you selected is Active");
            alert.showAndWait();
        }
        else {
            Alert succes = new Alert(Alert.AlertType.INFORMATION);
            succes.setTitle("Branch deleted successfully.");
            succes.setHeaderText("Branch deleted successfully.");
            succes.setContentText("Branch deleted successfully.");
            succes.showAndWait();
        }
    }
}


