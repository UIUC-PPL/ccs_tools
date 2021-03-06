package charm.debug.inspect;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import charm.debug.ParDebug;
import charm.debug.fmt.*;
import charm.debug.inspect.JTreeVisitor;
import charm.debug.pdata.MsgPList;
import charm.debug.pdata.Slot;

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
	JMenuItem menuItemFollow;
	JMenuItem menuItemCast;
	JMenuItem menuItemCastReset;
	
	public InspectPanel() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(100,100));
		scroll = new JScrollPane();
		add(scroll);
	}
	
	public void clear() {
		scroll.setViewportView(new JLabel(""));
	}
	
	public void load(String s) {
		scroll.setViewportView(new JLabel(s));
	}
	
	public boolean load(int pe, Slot slot, GenericType type) {
		this.pe = pe;
		long location = slot.getLocation();
		System.out.println("location = "+(int)location+", "+(int)(location>>>32));
		if (slot.getType() == Slot.MESSAGE_TYPE) {
			PList list = ParDebug.debugger.server.getPList("converse/message",pe,(int)location,(int)(location>>>32));
			MsgPList msg = ParDebug.debugger.getMessageList();
			msg.load(list);
			msg.elementAt(0).getDetails(this);
			return true;
		}
		PList list = ParDebug.debugger.server.getPList("memory/data",pe,(int)location,(int)(location>>>32));
		if (list==null) System.out.println("list is null!");
		PList cur = (PList)list.elementAt(0);
		int size = ((PNative)cur.elementNamed("size")).getIntValue(0);
		System.out.println("Got memory data size = "+size);
		if (size > 0) {
			ByteBuffer buf = null;
			int startingPoint = 0;
			PAbstract info = cur.elementNamed("value");
			if (info != null) buf = ByteBuffer.wrap(((PString)info).getBytes()).order(Inspector.getByteOrder());
//			// Messages have the envelope at the beginning, need to take it out
//			if (slot.getType() == Slot.MESSAGE_TYPE) {
//				System.out.println("size = "+Inspector.getTypeCreate("envelope").getSize());
//				startingPoint = 8+Inspector.getTypeCreate("envelope").getSize();
//				buf.position(startingPoint);
//			}
			if (type == null) {
			String request = "info symbol 0x";
			if (Inspector.is64bit()) request += Long.toHexString(buf.getLong());
			else request += Integer.toHexString(buf.getInt());
			request += "\n";
			String result = ParDebug.infoCommand(request);
			if (result.startsWith("vtable for")) {
				String strtype = result.substring(10, result.indexOf('+')).trim();
				GenericType gt = Inspector.getTypeCreate(strtype);
				load(new SuperClassElement(gt,0), buf, 0);
				return true;
//			} else if (slot.getType() == Slot.MESSAGE_TYPE){
//				GenericType gt = Inspector.getTypeCreate("envelope");
//				load(new SuperClassElement(gt,0), buf, 8);
//				return true;
			}
			else {
				buf.rewind();
				for (int i=0; i<size; ++i) {
					System.out.print("0x"+Integer.toHexString(buf.get())+" ");
				}
				JOptionPane.showMessageDialog(this, "The selected memory block does not contain enough information to be displayed.", "Unknown data", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			} else {
				load(new SuperClassElement(type,0), buf, startingPoint);
				return true;
			}
		}
		JOptionPane.showMessageDialog(this, "The selected memory block does not contain any data.", "No data", JOptionPane.INFORMATION_MESSAGE);
		return false;
	}
	
	public void load(GenericElement type, ByteBuffer buf, int starting) {
		JTreeVisitor jtv = new JTreeVisitor(buf, starting, type.getName());
		jtv.visit(type);
		tree = (JTree)jtv.getResult();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		scroll.setViewportView(tree);
		//InspectTree it = new InspectTree(new SuperClassElement(gt, 0));
		//scroll.setViewportView(it);
		JPopupMenu popup = new JPopupMenu();
		menuItemFollow = new JMenuItem("Follow pointer");
		menuItemFollow.setActionCommand("dereference");
		menuItemFollow.addActionListener(this);
		popup.add(menuItemFollow);
		menuItemCast = new JMenuItem("Cast");
		menuItemCast.setActionCommand("cast");
		menuItemCast.addActionListener(this);
		popup.add(menuItemCast);
		menuItemCast = new JMenuItem("Reset Cast");
		menuItemCast.setActionCommand("resetcast");
		menuItemCast.addActionListener(this);
		popup.add(menuItemCast);
		MouseListener popupListener = new PopupListener(popup);
		tree.addMouseListener(popupListener);
	}
	
	public void addBeforeTree(MutableTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
		model.insertNodeInto(node, root, 0);
	}
	
	public void actionPerformed(ActionEvent e) {
		DefaultMutableTreeNode obj = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
		InspectedElement el = (InspectedElement)obj.getUserObject();
		if (el.value == null) return;
		long location = Long.parseLong(el.value.substring(el.value.indexOf("0x")+2), 16);
		if (e.getActionCommand().equals("dereference")) {
			if (location > 0) {
				PList list = ParDebug.debugger.server.getPList("memory/data",pe,(int)location,(int)(location>>>32));
				if (list==null) System.out.println("list is null!");
				PList cur = (PList)list.elementAt(0);
				int size = ((PNative)cur.elementNamed("size")).getIntValue(0);
				System.out.println("Got memory data size = "+size);
				if (size > 0) {
					ByteBuffer buf = null;
					PAbstract info = cur.elementNamed("value");
					if (info != null) buf = ByteBuffer.wrap(((PString)info).getBytes()).order(Inspector.getByteOrder());
					JTreeVisitor jtv = new JTreeVisitor(buf, 0, el.e.getType().getName());
					int arraySize = 0;
					if (el.e.getPointer() > 1) {
						arraySize = size / Inspector.getPointerSize();
					} else {
						arraySize = size / el.e.getType().getSize();
					}					
					if (arraySize == 1) arraySize = 0;
					VariableElement vel = new VariableElement(el.e.getType(), null, arraySize, el.e.getPointer()+el.e.getType().getPointer()-1, 0);
					jtv.visit(vel);
					//obj.add((DefaultMutableTreeNode)jtv.top.getFirstChild());
					((DefaultTreeModel)tree.getModel()).insertNodeInto((DefaultMutableTreeNode)jtv.top.getFirstChild(), obj, 0);
					//JTree t = (JTree)jtv.getResult();
				} else {
					JOptionPane.showMessageDialog(this, "No data found", "Missing data", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getActionCommand().equals("cast")) {
			String newType = JOptionPane.showInputDialog("Please enter the new type name");
			if (newType != null) {
				newType = newType.trim();
				int pointer = 0;
				while (newType.charAt(newType.length()-pointer-1) == '*') pointer++;
				newType = newType.substring(0, newType.length()-pointer).trim();
				GenericType nt = Inspector.getTypeCreate(newType);
				if (nt instanceof DataType && ((DataType)nt).desc == null) {
					JOptionPane.showMessageDialog(this, "The specified type does not exist.", "No such type", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (pointer == 0 && nt.getPointer() == 0) {
					JOptionPane.showMessageDialog(this, "The specified type is not a pointer.", "Not a pointer", JOptionPane.ERROR_MESSAGE);
					return;
				}
				el.e = el.e.castNewType(nt, pointer);
			}
		} else if (e.getActionCommand().equals("resetcast")) {
			el.e = el.original;
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
        	//System.out.println("MouseEvent: "+e);
        	TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
        	if (tp != null) {
        		DefaultMutableTreeNode obj = (DefaultMutableTreeNode)tp.getLastPathComponent();
        		if (!(obj.getUserObject() instanceof InspectedElement)) return;
        		InspectedElement el = (InspectedElement)obj.getUserObject();
        		System.out.println(el);
        		if (e.isPopupTrigger() && el.isPointer()) {
        			if (obj.isLeaf()) menuItemFollow.setEnabled(true);
        			else menuItemFollow.setEnabled(false);
        			tree.setSelectionPath(tp);
        			popup.show(e.getComponent(),
        					e.getX(), e.getY());
        		}
        	}
        }
    }
}