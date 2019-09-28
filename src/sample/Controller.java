package sample;
import Logic.Logic;
import Logic.Objects.BranchData;
import Logic.Objects.WorkingCopyStatus;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import inputValidation.FilesValidation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import Logic.OpenAndConflict;
import Logic.Conflict;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import  Logic.XmlException;
import javafx.stage.Modality;
import javafx.stage.Stage;
import layout.CommitTreeLayout;
import node.CommitNode;

import static Logic.ConstantsEnums.*;
import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;

public class Controller {

    private Logic m_LogicManager = new Logic();
    private FilesValidation m_FilesValidation = new FilesValidation();

    @FXML
    public TextField txtField_repositoryPath;
    @FXML
    public TextField txtField_userName;
    @FXML
    public TextArea textArea;
    @FXML
    public ListView listviewConflict;
    @FXML
    public Menu menu_repository,menu_file_commits,menu_branches,menu_collaborate;
    @FXML
    public MenuItem FetchMenuItem;
    @FXML
    public MenuItem PushMenuItem;
    @FXML
    public MenuItem PullMenuItem;

    //-------------Repository - Start--------------------------
    //create repository
    public void initRepository(javafx.event.ActionEvent actionEvent) throws IOException {

        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            TextInputDialog dialog = TextDialogCreator("Input Repository Name", "Insert Repository Name", "Please enter repository name: ");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (m_LogicManager.initRepository(selectedFolder.getAbsolutePath(), result.get())) {
                    txtField_repositoryPath.setText(selectedFolder.getAbsolutePath() + File.separator + result.get());
                    unDisableRepositorySection();
                } else {
                    Alert alert = alertCreator(Alert.AlertType.WARNING, "Warning", "Repository Alreadt Exist!", "repository alerady exists.");
                    alert.showAndWait();
                }
            }
        }
    }

    //Load Repository XML
    public void readXML(javafx.event.ActionEvent actionEvent) {
        final FileChooser dc = new FileChooser();
        File selectedXML = dc.showOpenDialog(null);
        Runnable runnable = () -> {

            try {
                if (selectedXML != null) {
                    m_LogicManager.readXML(selectedXML.getAbsolutePath());
                    txtField_repositoryPath.setText(selectedXML.getAbsolutePath());
                    unDisableRepositorySection();
                }
            } catch (XmlException e) {
                Alert alert = alertCreator(Alert.AlertType.WARNING,"XML loading aborted",e.getMessage(),"");
                alert.showAndWait();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    //Switch Repository
    public void switchRepository(javafx.event.ActionEvent actionEvent) throws IOException {
        final DirectoryChooser dc = new DirectoryChooser();
        File selectedFolder = dc.showDialog(null);
        if (selectedFolder != null) {
            if (m_LogicManager.setM_ActiveRepository(selectedFolder.getAbsolutePath())) {
                txtField_repositoryPath.setText(selectedFolder.getAbsolutePath());
                txtField_userName.setText("Administrator");
                unDisableRepositorySection();
            } else {
                Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "Repository Not Exist", "the folder you selected isn't repository");
                alert.showAndWait();
            }
        }
    }

    //Set User Name
    public void setUserName(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Input User Name", "Insert User Name", "Please enter user name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (m_LogicManager.setM_ActiveUser(result.get())) {
                txtField_userName.setText(result.get());
                System.out.println("User Name: " + result.get());
            } else {
                Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "UnValid User Name Input", "user name input is unvalid. 1-50 characters");
                alert.showAndWait();
            }
        }
    }
    //-------------Repository - End--------------------------

    //-------------Files & Commits - Start--------------------------
    public void showWcStatus(javafx.event.ActionEvent actionEvent) {
        WorkingCopyStatus wcStatus = m_LogicManager.ShowWorkingCopyStatus(m_LogicManager.getPathFolder(".magit") + File.separator + "CommitStatus.txt");
        String stringToShow=EmptyString;
        if(wcStatus.isNotChanged()){
            stringToShow += "There is no changes." + System.lineSeparator();

        }
        else {
            if (!wcStatus.getM_NewFilesList().isEmpty()) {
                stringToShow += "New Files:" +System.lineSeparator();
                for (String fileFullName : wcStatus.getM_NewFilesList())
                    stringToShow += ("    - " + fileFullName)+System.lineSeparator();
                stringToShow += System.lineSeparator();
            }
            if (!wcStatus.getM_ChangedFilesList().isEmpty()) {
                stringToShow += "Modified Files:" + System.lineSeparator();
                for (String fileFullName : wcStatus.getM_ChangedFilesList())
                    stringToShow += ("    - " + fileFullName) + System.lineSeparator();
                stringToShow += System.lineSeparator();
            }
            if (!wcStatus.getM_DeletedFilesList().isEmpty()) {
                stringToShow += "Deleted Files:" + System.lineSeparator();
                for (String fileFullName : wcStatus.getM_DeletedFilesList())
                    stringToShow += ("    - " + fileFullName) + System.lineSeparator();
                stringToShow += System.lineSeparator();
            }
        }
        textArea.setText(stringToShow);
    }


    public void createCommit(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Commit Message", "Insert Commit Message", "Please enter Commit Message: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            m_LogicManager.createCommit(result.get());
        }
    }

    public void showCommitList(javafx.event.ActionEvent actionEvent) throws IOException {
        List<CommitNode> commitNodeList = m_LogicManager.getCommitList();
        Graph tree = new Graph();
        createCommits(tree, commitNodeList);

        Stage primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        GridPane root = fxmlLoader.load(getClass().getResource("main.fxml"));

        final Scene scene = new Scene(root, 700, 400);

        ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpaneContainer");
        PannableCanvas canvas = tree.getCanvas();
        scrollPane.setContent(canvas);

        Button button = (Button) scene.lookup("#pannableButton");

        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> {
            tree.getUseViewportGestures().set(false);
            tree.getUseNodeGestures().set(false);
        });

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
        TextInputDialog dialog = TextDialogCreator("Create New Branch", "Insert Branch Name", "Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (!m_LogicManager.createNewBranch(result.get())) {
                Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "Error! Branch Name Exist!", "Error! Branch Name Exist!");
                alert.showAndWait();
            } else {
                Alert alert = alertCreator(Alert.AlertType.CONFIRMATION, "Information", "Branch created successfully.", "Do you want to check out active branch and load this branch?");

                ButtonType buttonYes = new ButtonType("Yes");
                ButtonType buttonNo = new ButtonType("No");

                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonTypeCancel);

                Optional<ButtonType> resultYesNo = alert.showAndWait();
                if (resultYesNo.get() == buttonYes) {
                    if (m_LogicManager.WcNotChanged()) {
                        m_LogicManager.CheckOutHeadBranch(result.get(), false, EmptyString);
                        Alert success = alertCreator(Alert.AlertType.INFORMATION, "Head branch checked out successfully.", "Head branch checked out successfully.", "Head branch checked out successfully.");
                        success.showAndWait();
                    } else if (resultYesNo.get() == buttonNo) {
                        System.out.println("There is open changes. check out branch failed.");
                        Alert warning = alertCreator(Alert.AlertType.WARNING, "Check Out Branch Faild", "There is open changes. check out branch failed.", "There is open changes. check out branch failed.");
                        warning.showAndWait();
                    } else {
                        // ... user chose CANCEL or closed the dialog
                    }
                }
            }
        }
    }

    //Delete Exist Branch
    public void deleteExistBranch(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Create New Branch", "Insert Branch Name", "Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (!m_LogicManager.deleteBranch(result.get())) {
            Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "Branch is Active!", "the branch you selected is Active");
            alert.showAndWait();
        } else {
            Alert success = alertCreator(Alert.AlertType.INFORMATION, "Branch deleted successfully.", "Branch deleted successfully.", "Branch deleted successfully.");
            success.showAndWait();
        }
    }

    //Replace Head Branch
    public void CheckOutHeadBranch(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("change head Branch", "Insert Branch Name", "Please enter branch name: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            m_LogicManager.CheckOutHeadBranch(result.get(), false, "");
        }
    }

    //Reset Branch
    public void ResetBranch(javafx.event.ActionEvent actionEvent){
        TextInputDialog dialog = TextDialogCreator("Reset Branch", "Insert Sha1 To Reset Branch", "Please enter Sha1: ");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent())
            return;
        if(!m_LogicManager.isSha1Exist(result.get()))
            return;
        m_LogicManager.zeroingBranch(result.get());
    }

    //Merge
    public void mergeBranches(javafx.event.ActionEvent actionEvent) {
        TextInputDialog dialog = TextDialogCreator("Input Branch Name", "Insert Branch Name", "Please enter repository name: ");
        Optional<String> result = dialog.showAndWait();
        //todo validation input
        //todo no open changes
        if (!result.isPresent())
            return;
        if(!m_LogicManager.isBranchExist(result.get())){
            Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "Branch Not Exist", "the branch name you entered is not exist.");
            alert.showAndWait();
            return;
        }

        OpenAndConflict openAndConflict = m_LogicManager.MergeBranches(m_LogicManager.getBranchActiveName(), result.get());
        listviewConflict.setVisible(true);
        for (Conflict c : openAndConflict.getConflictList()) {
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
                    Parent tableViewParent = (Parent) loader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(tableViewParent));
                    ControllerConflict1 controller = loader.getController();
                    controller.initData((Conflict) listviewConflict.getSelectionModel().getSelectedItem(), m_LogicManager.getM_ActiveRepository() + File.separator + m_LogicManager.getRootFolderName(), listviewConflict, listviewConflict.getSelectionModel().getSelectedIndex());
                    stage.showAndWait();
                    if (listviewConflict.getItems().size() == 0) {
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

    //-------------Collaboration - Start--------------------------
    public void Clone(javafx.event.ActionEvent actionEvent) throws Exception {
        File selectedDestination = null;
        final DirectoryChooser dc = new DirectoryChooser();
        TextInputDialog td = new TextInputDialog("");

        dc.setTitle("Select Repository to Clone");
        File selectedRepo = dc.showDialog(null);


        if (selectedRepo != null) {
            if(m_FilesValidation.isRepository(selectedRepo.getAbsolutePath())) {
                dc.setTitle("Select Destination Folder to clone into");
                selectedDestination = dc.showDialog(null);
                if (selectedDestination != null) {
                    td.setTitle("");
                    td.setHeaderText("Enter Repository Name");
                    Optional<String> result = td.showAndWait();
                    m_LogicManager.Clone(selectedRepo.getAbsolutePath(), selectedDestination.getAbsolutePath(), result.get());
                    txtField_repositoryPath.setText(m_LogicManager.getM_ActiveRepository());
                    FetchMenuItem.setDisable(false);
                    PullMenuItem.setDisable(false);
                    PushMenuItem.setDisable(false);
                }
                else {
                    Alert alert = alertCreator(Alert.AlertType.ERROR, "Error", "Repository Not Exist", "the folder you selected isn't repository");
                    alert.showAndWait();
                        }
            }
            else{
                Alert alert = alertCreator(Alert.AlertType.WARNING,"Selected file is not a Repository.","Please select a repository","");
                alert.showAndWait();
            }
        }
        else {
            Alert alert = alertCreator(Alert.AlertType.WARNING,"Problem with the selected file..","Please select new file","");
            alert.showAndWait();
        }
    }

    public void Fetch(javafx.event.ActionEvent actionEvent) throws Exception{
        m_LogicManager.Fetch();
    }

    public void Pull(javafx.event.ActionEvent actionEvent) throws Exception{
        m_LogicManager.Pull();
    }

    public void Push(javafx.event.ActionEvent actionEvent) throws Exception{
        m_LogicManager.Push();
    }
    //-------------Collaboration - End--------------------------

    //-------------General - Start--------------------------

    public void unDisableRepositorySection() {
        txtField_userName.setText("Administrator");
        menu_branches.setDisable(false);
        menu_file_commits.setDisable(false);
        menu_collaborate.setDisable(false);
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
    public void createCommits(Graph graph, List<CommitNode> commitNodeList) {
        final Model model = graph.getModel();
        graph.beginUpdate();

//        List <ICell> icells = new ArrayList<>();
        for (CommitNode commitNode : commitNodeList) {
            model.addCell(commitNode);
        }
        for(int i=commitNodeList.size()-1;i>=0;i--)
        {
            findRainbow(model,commitNodeList,i);
        }
//        for (CommitNode commitNode : commitNodeList) {
//            commitNode.setAlreadyAdded(true);
//            for (CommitNode commitNode2 : commitNodeList) {
//                if (!commitNode2.getAlreadyAdded()) {
//                    if (commitNode2.getBranch_number() == commitNode.getBranch_number())//same branch
//                        model.addEdge(commitNode, commitNode2);
//                    else if (commitNode.getBranch_number() == commitNode2.getPrevBranch())//new branch
//                        model.addEdge(commitNode, commitNode2);
//                }
//            }
//        }
        graph.endUpdate();
        graph.layout(new CommitTreeLayout());

    }

    private void findRainbow(Model model, List<CommitNode> commitNodeList, int size) {
        for(int i=size-1;i>=0;i--)
        {
            if(commitNodeList.get(i).getBranch_number()==commitNodeList.get(size).getBranch_number())
            {
                model.addEdge(commitNodeList.get(i), commitNodeList.get(size));
                return;
            }
            else if(commitNodeList.get(size).getPrevBranch()==commitNodeList.get(i).getBranch_number())
            {
                model.addEdge(commitNodeList.get(i), commitNodeList.get(size));
                return;
            }
        }
    }
}


