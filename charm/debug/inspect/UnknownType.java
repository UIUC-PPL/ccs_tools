package charm.debug.inspect;

import java.nio.ByteBuffer;

/** Class representing a typedef defined type */
public class UnknownType extends GenericType {

    public GenericType build(String n, String d) {
        name = n;
        return this;
    }

    public int getSize() {
        return 0;
    }

    public String getValue(TypeVisitor v) { return "? (unknown)"; }

    public String toString (String indent) {
	return name+" (unknown)";
    }

    public String memoryToString(String indent, ByteBuffer mem, int start) {
        return "? (unknown)";
    }

    public void visit(TypeVisitor v) {
        v.addValue("? (unknown)");
    }
}
