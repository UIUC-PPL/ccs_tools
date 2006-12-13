package charm.debug.inspect;

import java.nio.ByteBuffer;
import javax.swing.tree.*;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;

public class JTreeVisitor extends TypeVisitor {
    DefaultMutableTreeNode current;
    DefaultMutableTreeNode top;
    DefaultMutableTreeNode last;
    JTree tree;
    
    public JTreeVisitor(ByteBuffer b, String title) {
        super(b);
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
    	if (tree != null) return tree;
    	else return (tree = new JTree(top));
    }
}
