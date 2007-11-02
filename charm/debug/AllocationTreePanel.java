package charm.debug;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.*;
import java.nio.ByteBuffer;

import charm.debug.inspect.Inspector;

public class AllocationTreePanel extends JPanel {
	private JScrollPane scroll;
	private AllocationPoint root;
	private ByteBuffer buf;
	private JTree tree;

	public AllocationTreePanel() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(100,100));
		scroll = new JScrollPane();
		add(scroll);
	}

	public void loadTree(JFrame frame) {
		String input = JOptionPane.showInputDialog("Processor to load (-1 for all)");
		int inputValue;
		try {
			inputValue = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return;
		}
		if (inputValue == -1) frame.setTitle("Combined Allocation Tree");
		else frame.setTitle("Allocation Tree Processor "+input);
		if (inputValue == -1) inputValue = 0; /* Send request to 0 */
		byte[] allocationTree = ParDebug.server.sendCcsRequestBytes("ccs_debug_allocationTree", input, inputValue);
		buf = ByteBuffer.wrap(allocationTree).order(Inspector.getByteOrder());
		root = new AllocationPoint(null);
		root.read(buf);
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		scroll.setViewportView(tree);
	}
	
}
