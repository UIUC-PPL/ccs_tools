package charm.debug.inspect;

import java.nio.ByteBuffer;
import java.util.*;

public abstract class TypeVisitor {
    ByteBuffer buf;
    int offset;
    int status;
    Stack seeks;
    boolean valid;

    public TypeVisitor(ByteBuffer b) {
        buf = b;
        offset = 0;
        status = 0;
        valid = true;
        seeks = new Stack();
    }

    public void visit(GenericType gt) {
        //System.out.println("start visit");
        int children = gt.getChildren();
        if (children > 0) {
            push();
            for (int i=0; i<children; ++i) {
                GenericElement e = gt.getChild(i);
                visit(e);
            }
            pop();
        }
    }
    
    public void visit(GenericElement e) {
        seek(e.getOffset());
        GenericType t = e.getType();
        //String type = t.getName();
        //String name = e.getName();
        String value = null;
        int pointer = e.getPointer() + t.getPointer();
        int size = 1;
        if (e instanceof VariableElement && ((VariableElement)e).getArray() > 0) size = ((VariableElement)e).getArray();
        if (size > 1) {
        	addElement(e, "[]");
        	push();
        }
        for (int i=0; i<size; ++i) {
        	if (i>0) reseek(e.getOffset()+i*t.getSize());
            if (valid) value = t.getValue(this);
            else value = null;
        	if (pointer > 0 && valid) {
        		if (value != null) value += " " + GenericType.printPointer(buf, offset);
        		else value = GenericType.printPointer(buf, offset);
        	}
        	addElement(e, value);
        	if (pointer == 0) visit(t);
        }
        if (size > 1) pop();
        revertSeek();
    }

    public abstract Object getResult();

    public abstract void addElement(GenericElement e, String v);

    /** Deprecated */
    public void addType(String str) {}
    /** Deprecated */
    public void addName(String str) {}
    /** Deprecated */
    public void addValue(String str) {}

    public abstract void push();
    public abstract void pop();

    /** Deprecated */
    public boolean isValid() {return valid;}
    /** Deprecated */
    public void setPointer(int p) {}

    public static final int SUPERCLASS = 2;
    public void setStatus(int s) { status |= s; }

    public void seek(int size) {
        if (valid) {
            seeks.push(new Integer(size));
            offset += size;
            if (size < 0) valid = false;
        } else {
            seeks.push(new Integer(0));
        }
    }
    
    public void reseek(int size) {
    	revertSeek();
    	seek(size);
    }

    public void revertSeek() {
        int change = ((Integer)seeks.pop()).intValue();
        if (change < 0) valid = true;
        offset -= change;
    }

    public byte getByte() { return buf.get(offset); }
    public short getShort() { return buf.getShort(offset); }
    public int getInteger() { return buf.getInt(offset); }
    public long getLong() { return buf.getLong(offset); }
    public float getFloat() { return buf.getFloat(offset); }
    public double getDouble() { return buf.getDouble(offset); }

    public String printPointer() { return GenericType.printPointer(buf, offset); }
}
