package charm.debug;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class EpTreeRenderer extends DefaultTreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
		//String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
		if (value instanceof EpTreeCheckBox) return ((EpTreeCheckBox)value).chkbox;
		return super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
    }

}
