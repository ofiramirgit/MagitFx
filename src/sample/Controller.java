package sample;
import Logic.Logic;
import Logic.Objects.BranchData;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import Logic.OpenAndConflict;
import Logic.Conflict;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import  Logic.XmlException;
import javafx.stage.Modality;
import javafx.stage.Stage;
import node.CommitNode;

import static Logic.ConstantsEnums.*;
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;

public class Controller {

    Logic m_LogicManager = new Logic();

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
    @FXML
    public ListView listviewConflict;

    //-------------Repository - Start--------------------------
    //create repository
    public void initRepository(javafx.event.ActionEvent actionEvent) throws IOException {

        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            TextInputDialog dialog = TextDialogCreator("Input Repository Name","Insert Repository Name","Please enter repository name: ");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (m_LogicManager.initRepository(selectedFolder.getAbsolutePath(), result.get())) {
                    txtField_repositoryPath.setText(selectedFolder.getAbsolutePath() + File.separator + result.get());
                    unDisableRepositorySection();
                } else {
                    Alert alert = alertCreator(Alert.AlertType.WARNING,"Warning","Repository Alreadt Exist!","repository alerady exists.");
                    alert.showAndWait();
                }
            }
        }
    }
    //Load Repository XML
    public void readXML(javafx.event.ActionEvent actionEvent) { //need to add check
       /* Boolean exist=false;
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
        thread.start();*/
        try {
            m_LogicManager.readXML("C:\\Users\\OL\\Desktop\\Java Course\\ex1-large.xml");
        } catch (XmlException e) {
            e.printStackTrace();
        }
    }
    //Switch Repository
    public void switchRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            if (m_LogicManager.setM_ActiveRepository(selectedFolder.getAbsolutePath())) {
                txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
                txtField_userName.setText("Administrator");
            } else {
                Alert alert = alertCreator(Alert.AlertType.ERROR,"Error","Repository Not Exist","the folder you selected isn't repository");
                alert.showAndWait();
            }
        }
    }
    //Set User Name
    public void setUserName(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Input User Name","Insert User Name","Please enter user name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (m_LogicManager.setM_ActiveUser(result.get())) {
                txtField_userName.setText(result.get());
                System.out.println("User Name: " + result.get());
            } else {
                Alert alert = alertCreator(Alert.AlertType.ERROR,"Error","UnValid User Name Input","user name input is unvalid. 1-50 characters");
                alert.showAndWait();
            }
        }
    }
    //-------------Repository - End--------------------------

    //-------------Files & Commits - Start--------------------------
    public void createCommit(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Commit Message","Insert Commit Message","Please enter Commit Message: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            m_LogicManager.createCommit(result.get());
        }
    }
    public void showCommitList(javafx.event.ActionEvent actionEvent){
        List<CommitNode> commitNodeList = m_LogicManager.getCommitList();
    }

    //-------------Files & Commits - End--------------------------

    //-------------Branches - Start--------------------------
   //Show All Branches
    public void showBranchList(javafx.event.ActionEvent actionEvent) {
        List<BranchData> BranchesList = m_LogicManager.GetAllBranchesDetails();
        String stringToShow = "";
        for (BranchData branch : BranchesList)
            stringToShow += branch.toString();
        textArea.setText(stringToShow);
    }
    //Create New Branch
    public void createNewBranch(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Create New Branch","Insert Branch Name","Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!m_LogicManager.createNewBranch(result.get())) {
                Alert alert = alertCreator(Alert.AlertType.ERROR,"Error","Error! Branch Name Exist!","Error! Branch Name Exist!");
                alert.showAndWait();
            } else {
                Alert alert = alertCreator(Alert.AlertType.CONFIRMATION,"Information","Branch created successfully.","Do you want to check out active branch and load this branch?");

                ButtonType buttonYes = new ButtonType("Yes");
                ButtonType buttonNo = new ButtonType("No");

                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonTypeCancel);

                Optional<ButtonType> resultYesNo = alert.showAndWait();
                if (resultYesNo.get() == buttonYes) {
                    if (m_LogicManager.WcNotChanged()) {
                        m_LogicManager.CheckOutHeadBranch(result.get(), false, EmptyString);
                        Alert success = alertCreator(Alert.AlertType.INFORMATION,"Head branch checked out successfully.","Head branch checked out successfully.","Head branch checked out successfully.");
                        success.showAndWait();
                    } else if (resultYesNo.get() == buttonNo) {
                        System.out.println("There is open changes. check out branch failed.");
                        Alert warning = alertCreator(Alert.AlertType.WARNING,"Check Out Branch Faild","There is open changes. check out branch failed.","There is open changes. check out branch failed.");
                        warning.showAndWait();
                    } else {
                        // ... user chose CANCEL or closed the dialog
                    }
                }
            }
        }
    }
    //Delete Exist Branch
    public void deleteExistBranch(javafx.event.ActionEvent actionEvent){
        TextInputDialog dialog = TextDialogCreator("Create New Branch","Insert Branch Name","Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if(!m_LogicManager.deleteBranch(result.get()))
        {
            Alert alert = alertCreator(Alert.AlertType.ERROR,"Error","Branch is Active!","the branch you selected is Active");
            alert.showAndWait();
        }
        else {
            Alert success = alertCreator(Alert.AlertType.INFORMATION,"Branch deleted successfully.","Branch deleted successfully.","Branch deleted successfully.");
            success.showAndWait();
        }
    }
    //Replace Head Branch
    public void CheckOutHeadBranch(javafx.event.ActionEvent actionEvent){
        TextInputDialog dialog = TextDialogCreator("change head Branch","Insert Branch Name","Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            m_LogicManager.CheckOutHeadBranch(result.get(),false,"");
        }
    }
    //Merge
    public void mergeBranches(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Input Branch Name","Insert Branch Name","Please enter repository name: ");
        Optional<String> result = dialog.showAndWait();
        //todo validation input
        //todo no open changes
        OpenAndConflict openAndConflict = m_LogicManager.MergeBranches(m_LogicManager.getBranchActiveName(),result.get());
        String s="";
         listviewConflict.setVisible(true);
//        ListView<Conflict> listViewConflict = new ListView<>();
        for(Conflict c: openAndConflict.getConflictList()){
            listviewConflict.getItems().add(c);
        }
        listviewConflict.setCellFactory(param -> new ListCell<Conflict>() {
            @Override
            protected void updateItem(Conflict item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getM_our().getM_filePath() == null) {
                    setText(null);
                } else {
                    setText(item.getM_our().getM_filePath().toString());
                }
            }
        });
        listviewConflict.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listviewConflict.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("conflicView1.fxml"));
                    Parent tableViewParent = (Parent)loader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(tableViewParent));
                    ControllerConflict1 controller = loader.getController();
                    controller.initData((Conflict)listviewConflict.getSelectionModel().getSelectedItem(),m_LogicManager.getM_ActiveRepository()+File.separator + m_LogicManager.getRootFolderName(),listviewConflict,listviewConflict.getSelectionModel().getSelectedIndex());
                    stage.showAndWait();
                    if(listviewConflict.getItems().size()==0)
                    {
                        listviewConflict.setVisible(false);
                        System.out.println("NO MORE ITEMS");
                        createCommit(actionEvent);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    //-------------Branches - End--------------------------

    //-------------General - Start--------------------------

    public void unDisableRepositorySection() {
        txtField_userName.setText("Administrator");
        tab_fileCommit.setDisable(false);
        tab_branches.setDisable(false);
        btn_switchRepository.setDisable(false);
        btn_setUserName.setDisable(false);
    }

    private TextInputDialog TextDialogCreator(String i_Title, String i_Header, String i_Content) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(i_Title);
        dialog.setHeaderText(i_Header);
        dialog.setContentText(i_Content);
        return dialog;
    }

    private Alert alertCreator(Alert.AlertType alertType, String i_Title, String i_Header, String i_Content) {
        Alert dialog = new Alert(alertType);
        dialog.setTitle(i_Title);
        dialog.setHeaderText(i_Header);
        dialog.setContentText(i_Content);
        return dialog;
    }
    //-------------General - End--------------------------

}


