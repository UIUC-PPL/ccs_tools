package charm.debug.inspect;

import java.nio.ByteBuffer;
import javax.swing.tree.*;
import javax.swing.JTree;

public class JTreeVisitor extends TypeVisitor {
    DefaultMutableTreeNode current;
    DefaultMutableTreeNode top;
    DefaultMutableTreeNode last;
    JTree tree;
    
    public JTreeVisitor(ByteBuffer b, int start, String title) {
        super(b, start);
        current = last = top = new DefaultMutableTreeNode(title);
        tree = null;//new JTree(top);
    }
    
    public void addElement(GenericElement e, String v) {
        //System.out.println("addElement");
        last = new DefaultMutableTreeNode(new InspectedElement(e, v));
        current.add(last);
    }

    public void push() {
        //System.out.println("push");
        current = last;
    }

    public void pop() {
        //System.out.println("pop");
        current = (DefaultMutableTreeNode)current.getParent();
    }

    public Object getResult() {
    	if (tree == null) {
    		tree = new JTree(top);
    		tree.setRootVisible(false);
    		tree.expandRow(0);
    	}
    	return tree;
    }
}
