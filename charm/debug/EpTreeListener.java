package charm.debug;

import java.awt.event.MouseAdapter;
import javax.swing.JTree;
import javax.swing.tree.*;
import java.awt.event.MouseEvent;

public class EpTreeListener extends MouseAdapter {
	JTree tree;

	EpTreeListener(JTree tree) {
		this.tree = tree;
	}

	public void mouseClicked(MouseEvent e) {
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		//TreePath  path = tree.getSelectionPath();
		if (path != null) {
			DefaultMutableTreeNode defnode = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (defnode instanceof EpTreeCheckBox) {
				EpTreeCheckBox node = (EpTreeCheckBox)defnode;
				node.chkbox.doClick();
			//if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
			//	if ( isSelected ) {
			//		tree.expandPath(path);
			//	} else {
			//		tree.collapsePath(path);
			//	}
			//}
			//}
			((DefaultTreeModel)tree.getModel()).nodeChanged(node);
			// I need revalidate if node is root.  but why?
			//		if (row == 0) {
			//			tree.revalidate();
			//			tree.repaint();
			}
		}
	}
}
