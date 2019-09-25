package node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;

public class CommitNode extends AbstractCell {

    private String timestamp;
    private String committer;
    private String message;
    private Integer branch_number;
    private Integer prevBranch;
    private String Sha1;
    private Boolean alreadyAdded=false;
    private CommitNodeController commitNodeController;

    public CommitNode(String timestamp, String committer, String message,Integer branch_number,Integer prevBranch,String Sha1) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
        this.branch_number = branch_number;
        this.prevBranch = prevBranch;
        this.Sha1 = Sha1;
    }


    public void setAlreadyAdded(Boolean alreadyAdded) {
        this.alreadyAdded = alreadyAdded;
    }

    public Boolean getAlreadyAdded() {
        return alreadyAdded;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSha1() {
        return Sha1;
    }

    public Integer getBranch_number() {
        return branch_number;
    }

    public Integer getPrevBranch() {
        return prevBranch;
    }

    public void setPrevBranch(Integer prevBranch) {
        this.prevBranch = prevBranch;
    }

    @Override
    public Region getGraphic(Graph graph) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
