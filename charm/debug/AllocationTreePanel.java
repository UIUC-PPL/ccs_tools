package charm.debug;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import charm.debug.inspect.Inspector;

public class AllocationTreePanel extends JPanel {
	private JScrollPane scroll;
	private AllocationPoint root;
	private ByteBuffer buf;
	private JTree tree;

	public AllocationTreePanel() {
		super();
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(500,500));
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
		root.readPuppedBuffer(buf);
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		scroll.setViewportView(tree);
	}
	
	public void loadMemoryLogs(List logs, boolean removeFreed) {
		root = new AllocationPoint(null);
		Hashtable allAllocations = new Hashtable();
		//System.out.println("Loading memory logs "+logs);
		if (logs != null) {
			for (Iterator i = logs.iterator(); i.hasNext(); ) {
				MemoryLog log = (MemoryLog)i.next();
				//System.out.println("Loading single log "+log+" size: "+log.getSize()+" stacksize: "+log.getStack().length);
				if (removeFreed && log.getLocation() == 0) {
					// in this case we simply reset the entire tree
					root = new AllocationPoint(null);
					allAllocations.clear();
					root.addMemoryLog(log, -1);
					root.getChildWithLocation(0).size = log.getSizeAfter();
				}
				else if (log.getSize() > 0) {
					//System.out.println("Adding memory log "+log);
					root.addMemoryLog(log, -1);
					if (removeFreed) allAllocations.put(log, log);
				}
				else if (log.getSize() < 0) {
					if (removeFreed) {
						MemoryLog pair = (MemoryLog)allAllocations.remove(log);
						if (pair != null) root.removeMemoryLog(pair, -1);
						else root.removeMemoryLog(log, -1);
					}
				}
			}
		}
		tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setVisibleRowCount(20);
		scroll.setViewportView(tree);
		for (int i=0; i<10; ++i) tree.expandRow(i);
	}
}
