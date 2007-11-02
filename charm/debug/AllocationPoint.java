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
	}
	
	public void read(ByteBuffer buf) {
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
			child.read(buf);
		}
	}

	public String toString() {
		return (isLeak?"* ":"")+location+": size="+size+", count="+count;
	}

	public Enumeration children() {
	    return children.elements();
    }

	public boolean getAllowsChildren() {
		return true;
    }

	public TreeNode getChildAt(int childIndex) {
	    return (AllocationPoint)children.elementAt(childIndex);
    }

	public int getChildCount() {
	    return children.size();
    }

	public int getIndex(TreeNode node) {
		for (int i=0; i<children.size(); ++i) if (children.elementAt(i)==node) return i;
	    return -1;
    }

	public TreeNode getParent() {
	    return parent;
    }

	public boolean isLeaf() {
	    return children.size()==0;
    }
}
