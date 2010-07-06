package charm.debug;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.*;

public class PeSet {
	private String name;
	SortedSet list;
	
	int numRunning;
	int numDead;
	int numConditional;
	
	public PeSet(String n, Processor l[]) {
		numRunning = 0;
		numDead = 0;
		name = n;
		list = new TreeSet();
		for (int i=0; i<l.length; ++i) {
			list.add(l[i]);
			l[i].addToSet(this);
			if (l[i].isDead()) numDead++;
			else if (! l[i].isFrozen()) numRunning++;
		}
	}
	
	public PeSet(String n, SortedSet l) {
		numRunning = 0;
		numDead = 0;
		name = n;
		list = l;
		Iterator iter = l.iterator();
		while (iter.hasNext()) {
			Processor p = (Processor)iter.next();
			p.addToSet(this);
			if (p.isDead()) numDead++;
			else if (! p.isFrozen()) numRunning++;
		}
	}

	/*
	public PeSubset(String n, int start, int end) {
		name = n;
		list = new TreeSet();
		for (int i=0; i<=end-start; ++i) {
			list.add(new Integer(i+start));
		}
		status = FROZEN;
	}
	*/
	
	public String toString() {
		//String color = "";
		//if (status == RUNNING) color="white";
		//if (status == FROZEN) color="red";
		//if (status == RUN_FROZEN) color="orange";
		//return "<html><body bgcolor=\""+color+"\">"+name+"</body></html>";
		return name;
	}
	public String getDetail() {
		StringBuffer buf = new StringBuffer();
		Iterator iter = getList().iterator();
		while (iter.hasNext()) buf.append(" "+iter.next());
		return buf.toString();
	}
	public String getName() {return name;}
	public SortedSet getList() {return list;}

	void setRunning() {numRunning++;}
	void setFrozen() {numRunning--;}
	void setDead() {numDead++;}
	void setConditional() {numConditional++;}
	void unsetConditional() {numConditional--;}
	
	public boolean isAllRunning() {return numRunning == list.size();}
	public boolean isAllFrozen() {return numRunning == 0;}
	public boolean isSomeDead() {return numDead > 0;}
	public boolean isSomeConditional() {return numConditional > 0;}

	public Iterator iterator() {return list.iterator();}
	public PeSetIterator runningIterator() {return new Running();}
	public PeSetIterator frozenIterator() {return new Frozen();}
	
	public int[] toIDsArray() {
		int []result = new int[list.size()];
		Iterator iter = iterator();
		for (int i=0; iter.hasNext(); ++i) result[i] = ((Processor)iter.next()).getId(); 
		return result;
	}
	
	public abstract class PeSetIterator implements Iterator {
		Iterator iter;
		public int[] toIDs() {
			int size = list.size();
			int []result = new int[size];
			int count=0;
			Processor p;
			while ((p=(Processor)next()) != null) result[count++] = p.getId();
			if (count != size) {
				result = Arrays.copyOf(result, count);
			}
			return result;
		}
	}

	public class Running extends PeSetIterator {
		Object next;
		
		public Running() {
			iter = list.iterator();
			next();
		}
		
		public boolean hasNext() {
			return next != null;
		}

		public Object next() {
			Object result = next;
			// find the next one
			next = null;
			Object tmp;
			while (iter.hasNext()) {
				tmp = iter.next();
				if (((Processor)tmp).isRunning()) {
					next = tmp;
					break;
				}
			}
			return result;
        }

		public void remove() {
			iter.remove();
        }
	}

	public class Frozen extends PeSetIterator {
		Object next;
		
		public Frozen() {
			iter = list.iterator();
			next();
		}
		
		public boolean hasNext() {
			return next != null;
		}

		public Object next() {
			Object result = next;
			// find the next one
			next = null;
			Object tmp;
			while (iter.hasNext()) {
				tmp = iter.next();
				if (((Processor)tmp).isFrozen()) {
					next = tmp;
					break;
				}
			}
			return result;
        }

		public void remove() {
			iter.remove();
        }
	}

	public static class PopupListener extends MouseAdapter {
	    JPopupMenu popupShort;
	    JPopupMenu popupLong;
	    JList list;

	    PopupListener(ActionListener listener, JList l) {
	    	list = l;
	    	popupShort = new JPopupMenu();
	        popupLong = new JPopupMenu();
	        JMenuItem menuItem;
	        menuItem = new JMenuItem("Create new...");
	        menuItem.setActionCommand("newPeSet");
	        menuItem.addActionListener(listener);
	        popupShort.add(menuItem);
	        menuItem = new JMenuItem("Create new...");
	        menuItem.setActionCommand("newPeSet");
	        menuItem.addActionListener(listener);
	        popupLong.add(menuItem);
	        menuItem = new JMenuItem("View details");
	        menuItem.setActionCommand("peSetDetails");
	        menuItem.addActionListener(listener);
	        popupLong.add(menuItem);
	        menuItem = new JMenuItem("Delete");
	        menuItem.setActionCommand("deletePeSet");
	        menuItem.addActionListener(listener);
	        popupLong.add(menuItem);
	    }

	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	    	//System.out.println("MouseEvent: "+e);
	    	if (e.isPopupTrigger()) {
	    		int idx = list.locationToIndex(e.getPoint());
	    		if (idx >= 0) {
	    			Rectangle range = list.getCellBounds(idx, idx);
	    			if (range.contains(e.getPoint())) {
	    				list.setSelectedIndex(idx);
	    				popupLong.show(e.getComponent(), e.getX(), e.getY());
	    			} else {
	    				popupShort.show(e.getComponent(), e.getX(), e.getY());
	    			}
	    		}
	    	}
	    	/*
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
	    	*/
	    }
	}

	public static class CellRenderer extends JLabel implements ListCellRenderer {
		static ImageIcon runningIcon;// = new ImageIcon((new PeSetCellRenderer()).getClass().getResource("long.gif"));
		static ImageIcon frozenIcon;// = new ImageIcon((new PeSetCellRenderer()).getClass().getResource("short.gif"));
		static ImageIcon run_frozenIcon;// = new ImageIcon("short.gif");
		static ImageIcon deadIcon;
		
		public static void initIcons() {
			runningIcon = new ImageIcon(CellRenderer.class.getResource("running.gif"));
			frozenIcon = new ImageIcon(CellRenderer.class.getResource("frozen.gif"));
			run_frozenIcon = new ImageIcon(CellRenderer.class.getResource("run-frozen.gif"));
			deadIcon = new ImageIcon(CellRenderer.class.getResource("dead.gif"));
		}

		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.

		public Component getListCellRendererComponent(
	       JList list,
	       Object value,            // value to display
	       int index,               // cell index
	       boolean isSelected,      // is the cell selected
	       boolean cellHasFocus)    // the list and the cell have the focus
		{
			String s = value.toString();
			setText(s);
			PeSet set = (PeSet)value;
			if (set.isSomeDead()) setIcon(deadIcon);
			else if (set.isAllRunning()) setIcon(runningIcon);
			else if (set.isAllFrozen()) setIcon(frozenIcon);
			else setIcon(run_frozenIcon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

}
