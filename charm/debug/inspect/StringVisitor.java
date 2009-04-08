package charm.debug.inspect;

import java.nio.ByteBuffer;

public class StringVisitor extends TypeVisitor {
    int spacing;
    StringBuffer result;

    public StringVisitor(ByteBuffer b, int start) {
        super(b, start);
        spacing = 0;
        result = new StringBuffer();
    }

    public void addElement(GenericElement e, String v) {
        //System.out.println("addElement");
    	GenericType gt = e.getType();
    	String t = gt.getName();
        String n = e.getName();
        int pointer = e.getPointer() + gt.getPointer();
        result.append('\n');
        for (int i=0; i<spacing; ++i) result.append("  ");
        if (t != null) result.append(t);
        for (int i=0; i<pointer; ++i) result.append('*');
        if (n != null) result.append(' ').append(n);
        result.append(" = ");
        if (v != null) result.append(v);
    }

    public void push() {
        //System.out.println("push");
        spacing++;
        result.append('{');
    }

    public void pop() {
        //System.out.println("pop");
        spacing--;
        result.append('\n');
        for (int i=0; i<spacing; ++i) result.append("  ");
        result.append('}');
    }

    public Object getResult() { return result; }
}
