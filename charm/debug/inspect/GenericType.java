package charm.debug.inspect;

import charm.debug.ParDebug;
import java.nio.ByteBuffer;

/** Basic abstract class for all classes representing a type definition */
public abstract class GenericType extends VisitableType {
    String name;

    GenericType() {
        name = null;
    }

    public GenericType build(String n) {
        return build(n, null);
    }
    public abstract GenericType build(String n, String d);

    public String getName() {
        return name;
    }

    public int getChildren() { return 0; }
    public GenericElement getChild(int i) { return null; }
    public abstract String getValue(TypeVisitor v);
    public int getPointer() { return 0; }

    public int pointerSize() {
        if (Inspector.is64bit()) return 8;
        else return 4;
    }
    public abstract int getSize();

    public String toString() {
        return toString("");
    }
    public abstract String toString(String s);

    public String memoryToString(ByteBuffer mem) {
        return memoryToString("", mem, 0);
    }
    public abstract String memoryToString(String indent, ByteBuffer mem, int start);

    static String getDescription(String type) {
        String desc = ParDebug.infoCommand("ptype class "+type+"\n");
        System.out.println("info1:ptype "+type+" = "+desc);
        if (desc.length()==0) {
            // trying without "class" keyword
            desc = ParDebug.infoCommand("ptype "+type+"\n");
	    System.out.println("info2:ptype "+type+" = "+desc);
        }
        if (desc.length()==0) {
	    System.out.println("info string is still null");
            return null;
        }
        if (!desc.startsWith("type =")) {
            System.out.println("incorrect description for "+type+": "+desc);
            return null;
        }
        return desc.substring(6).trim();
    }

    static String printPointer(ByteBuffer mem, int start) {
        if (start < 0) return "?";
        if (Inspector.is64bit()) return "0x"+Long.toHexString(mem.getLong(start));
        else return "0x"+Integer.toHexString(mem.getInt(start));
        //Long.toHexString(mem[start]+256*(mem[start+1]+256*(mem[start+2]+256*(long)mem[start+3])));
    }
}
