package charm.debug;

import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;

import charm.debug.inspect.Inspector;

public class AllocationPoint implements TreeNode {
	Symbol location;
	int size;
	int count;
	boolean isLeak;
	Vector children;
	AllocationPoint parent;

	public AllocationPoint(AllocationPoint p) {
		parent = p;
		size = count = 0;
		isLeak = false;
		location = null;
		children = null;
	}
	
	public void readPuppedBuffer(ByteBuffer buf) {
		long key;
		if (Inspector.is64bit()) key = buf.getLong();
		else key = buf.getInt();
		location = Symbol.get(key);
		size = buf.getInt();
		count = buf.getInt();
		isLeak = buf.get()>0;
		int numChildren = buf.getInt();
		children = new Vector(numChildren);
		for (int i=0; i<numChildren; ++i) {
			AllocationPoint child =new AllocationPoint(this); 
			children.addElement(child);
			child.readPuppedBuffer(buf);
		}
	}

	public void addMemoryLog(MemoryLog log, int depth) {
		if (depth >= 0) {
			if (location == null) location = Symbol.get(log.getStack(depth));
			size += log.getSize();
			count ++;
		}
		
		if (log.getStack().length > depth+1) {
			AllocationPoint child = getChildWithLocation(log.getStack(depth+1));
			if (child == null) {
				child = new AllocationPoint(this);
				if (children == null) children = new Vector();
				children.add(child);
			}
			child.addMemoryLog(log, depth+1);
		}
	}
	
	public void removeMemoryLog(MemoryLog log, int depth) {
		int sizePlus = log.getSize();
		if (sizePlus < 0) sizePlus = - sizePlus;
		if (depth >= 0) {
			if (location == null) location = Symbol.get(log.getStack(depth));
			size -= sizePlus;
			count --;
		}
		
		if (log.getStack().length > depth+1) {
			AllocationPoint child = getChildWithLocation(log.getStack(depth+1));
			if (child != null) {
				child.removeMemoryLog(log, depth+1);
				if (child.size == 0) children.remove(child);
			}
		}
	}
	
	AllocationPoint getChildWithLocation(long loc) {
		if (children == null) return null;
		for (int i=0; i<children.size(); ++i) {
			AllocationPoint child = (AllocationPoint)children.elementAt(i);
			if (child.location.equals(loc)) return child;
		}
		return null;
	}
	
	public String toString() {
		return (isLeak?"* ":"")+location+": size="+size+", count="+count;
	}

	public Enumeration children() {
		if (children == null) return null;
	    return children.elements();
    }

	public boolean getAllowsChildren() {
		return true;
    }

	public TreeNode getChildAt(int childIndex) {
	    return (AllocationPoint)children.elementAt(childIndex);
    }

	public int getChildCount() {
		if (children == null) return 0;
	    return children.size();
    }

	public int getIndex(TreeNode node) {
		if (children == null) return -1;
		for (int i=0; i<children.size(); ++i) if (children.elementAt(i)==node) return i;
	    return -1;
    }

	public TreeNode getParent() {
	    return parent;
    }

	public boolean isLeaf() {
	    return children==null || children.size()==0;
    }
}
