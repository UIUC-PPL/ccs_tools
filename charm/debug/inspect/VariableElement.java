package charm.debug.inspect;

import java.nio.ByteBuffer;

/** Representation of a variable inside a class */
public class VariableElement extends GenericElement {
    String name;
    int size;
    int pointer;

    public VariableElement(GenericType t, String n, int s, int p, int o) {
        type = t;
        name = n;
        size = s;
        pointer = p;
        offset = o;
    }

    public String getName() { return name; }
    public int getPointer() { return pointer; }

    public String toString(String indent) {
        StringBuffer buf = new StringBuffer();
        if (pointer > 0) {
            buf.append(type.getName());
            for (int i=0; i<pointer; ++i) buf.append("*");
        } else {
            buf.append(type.toString(indent));
        }
        buf.append(" ").append(name);
        if (size > 0) {
            buf.append("[").append(size).append("]");
        }
        buf.append(" (").append(offset).append(")");
        return buf.toString();
    }

    public String memoryToString(String indent, ByteBuffer mem, int start) {
        StringBuffer buf = new StringBuffer();
        buf.append(type.getName());
        for (int i=0; i<pointer; ++i) buf.append("*");
        buf.append(" ").append(name);
        if (size > 0) {
            buf.append("[").append(size).append("]");
        }
        buf.append(" = ");
        int newStart = (offset>=0 && start>=0) ? offset+start : -1;
        if (pointer > 0) buf.append(GenericType.printPointer(mem, newStart));
        else {
            if (size > 0) buf.append("{ ");
            buf.append(type.memoryToString(indent, mem, newStart));
            for (int i=1; i<size; ++i) buf.append(", ").append(type.memoryToString(indent, mem, newStart>=0?newStart+type.getSize()*i:-1));
            if (size > 0) buf.append(" }");
        }
        return buf.toString();
    }

    public void visit(TypeVisitor v) {
        v.seek(offset);
        v.addType(type.getName());
        v.addName(name);
        type.visit(v);
        v.revertSeek();
        if (pointer>0) v.setPointer(pointer);
    }
}
