package charm.debug.inspect;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import charm.debug.ParDebug;
import charm.debug.fmt.*;
import charm.debug.inspect.JTreeVisitor;
import java.nio.ByteBuffer;


/**
 * Panel for displaying a JTree-like structure of all the variables and
 * superclasses of a given type. A memory buffer is associated with the type
 * and the variable's values are taken from this buffer,
 * 
 * @author Filippo Gioachin
 *
 */
public class InspectPanel extends JPanel implements ActionListener {
	private JScrollPane scroll;
	private JTree tree;
	int pe;
	
	public InspectPanel() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(100,100));
		scroll = new JScrollPane();
		add(scroll);
	}
	
	public void load(int pe, long location, GenericType type) {
		this.pe = pe;
		System.out.println("location = "+(int)location+", "+(int)(location>>>32));
		PList list = ParDebug.server.getPList("converse/memory/data",pe,(int)location,(int)(location>>>32));
		if (list==null) System.out.println("list is null!");
		PList cur = (PList)list.elementAt(0);
		int size = ((PNative)cur.elementNamed("size")).getIntValue(0);
		System.out.println("Got memory data size = "+size);
		if (size > 0) {
			ByteBuffer buf = null;
			PAbstract info = cur.elementNamed("value");
			if (info != null) buf = ByteBuffer.wrap(((PString)info).getBytes()).order(Inspector.getByteOrder());
			String request = "info symbol 0x";
			if (Inspector.is64bit()) request += Long.toHexString(buf.getLong());
			else request += Integer.toHexString(buf.getInt());
			request += "\n";
			String result = ParDebug.infoCommand(request);
			if (result.startsWith("vtable for")) {
				String strtype = result.substring(10, result.indexOf('+')).trim();
				GenericType gt = Inspector.getTypeCreate(strtype);
				JTreeVisitor jtv = new JTreeVisitor(buf, gt.getName());
				jtv.visit(gt);
				tree = (JTree)jtv.getResult();
				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				scroll.setViewportView(tree);
				//InspectTree it = new InspectTree(new SuperClassElement(gt, 0));
				//scroll.setViewportView(it);
				JPopupMenu popup = new JPopupMenu();
				JMenuItem menuItem;
				menuItem = new JMenuItem("Follow pointer");
				menuItem.setActionCommand("dereference");
				menuItem.addActionListener(this);
				popup.add(menuItem);
				MouseListener popupListener = new PopupListener(popup);
				tree.addMouseListener(popupListener);
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("dereference")) {
			DefaultMutableTreeNode obj = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
			InspectedElement el = (InspectedElement)obj.getUserObject();
			if (el.value == null) return;
			long location = Long.parseLong(el.value.substring(el.value.indexOf("0x")+2), 16);
			if (location > 0) {
				PList list = ParDebug.server.getPList("converse/memory/data",pe,(int)location,(int)(location>>>32));
				if (list==null) System.out.println("list is null!");
				PList cur = (PList)list.elementAt(0);
				int size = ((PNative)cur.elementNamed("size")).getIntValue(0);
				System.out.println("Got memory data size = "+size);
				if (size > 0) {
					ByteBuffer buf = null;
					PAbstract info = cur.elementNamed("value");
					if (info != null) buf = ByteBuffer.wrap(((PString)info).getBytes()).order(Inspector.getByteOrder());
					JTreeVisitor jtv = new JTreeVisitor(buf, el.e.getType().getName());
					if (el.e.getPointer() > 1) {
						VariableElement vel = new VariableElement(el.e.getType(), null, el.e.getType().pointerSize(), el.e.getPointer()+el.e.getType().getPointer()-1, 0);
						obj.add(new DefaultMutableTreeNode(new InspectedElement(vel, "0x0"))); // FIXME: retrieve the correct value
					} else {
						jtv.visit(el.e.getType());
						obj.add(jtv.top);
					}
					//JTree t = (JTree)jtv.getResult();
				}
			}
		}
	}
	
    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
        	System.out.println("MouseEvent: "+e);
        	TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
        	if (tp != null) {
        		DefaultMutableTreeNode obj = (DefaultMutableTreeNode)tp.getLastPathComponent();
        		if (!(obj.getUserObject() instanceof InspectedElement)) return;
        		InspectedElement el = (InspectedElement)obj.getUserObject();
        		System.out.println(el);
        		if (e.isPopupTrigger() && el.isPointer() && obj.isLeaf()) {
        			tree.setSelectionPath(tp);
        			popup.show(e.getComponent(),
        					e.getX(), e.getY());
        		}
        	}
        }
    }
}