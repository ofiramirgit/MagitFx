package layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import node.CommitNode;
//import puk.team.course.magit.visual.node.CommitNode;

import java.util.List;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 50;
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
//            if (every3rdNode % 3 == 0) {
            graph.getGraphic(c).relocate(startX + (c.getBranch_number() - 1) * 50, startY);
//            } else {
//            graph.getGraphic(c).relocate(startX, startY);
            startY += 50;
        }
    }
}

