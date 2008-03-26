package charm.debug;

import javax.swing.tree.DefaultMutableTreeNode;

public class EpTreeCheckBox extends DefaultMutableTreeNode {
	EpCheckBox chkbox;
	
	EpTreeCheckBox(EpCheckBox cb) {
		super(cb.getText());
		chkbox = cb;
	}
}
