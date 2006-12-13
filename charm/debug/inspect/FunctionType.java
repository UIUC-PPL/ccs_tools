package charm.debug.inspect;

import java.nio.ByteBuffer;

/** Class representing a function pointer typedef */
public class FunctionType extends GenericType {

    public GenericType build(String n, String d) {
        name = n;
        return this;
    }

    public int getSize() {
        return pointerSize();
    }

    public String getValue(TypeVisitor v) {
        return v.printPointer();
    }

    public String toString(String indent) {
        return "fnprt "+name;
    }

    public String memoryToString(String indent, ByteBuffer mem, int start) {
        return printPointer(mem, start);
    }

    public void visit(TypeVisitor v) {
        v.addValue(v.printPointer());
    }
}
