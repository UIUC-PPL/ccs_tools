package charm.debug.inspect;

import java.nio.ByteBuffer;

public class PrimitiveType extends GenericType {
    public PrimitiveType(String n) {
        name = n;
    }

    public GenericType build(String n, String d) {return this;}

    public int getSize() {
        if (name.equals("bool")) return Inspector.getBoolSize();
	if (name.equals("long long") || name.equals("unsigned long long")) return Inspector.getLongLongSize();
        if (name.equals("long") || name.equals("unsigned long")) return Inspector.getLongSize();
	if (name.equals("int") || name.equals("unsigned int")) return Inspector.getIntSize();
        if (name.equals("short") || name.equals("unsigned short")) return 2;
        if (name.equals("char") || name.equals("unsigned char")) return 1;
        if (name.equals("float")) return 4;
        if (name.equals("double")) return 8;
        return 0;
    }

    public String toString(String indent) {
        return name;
    }

    public String memoryToString(String indent, ByteBuffer mem, int start) {
        if (start < 0) return "?";
        if (name.equals("float")) return Float.toString(mem.getFloat(start));
        if (name.equals("double")) return Double.toString(mem.getDouble(start));
        int size = getSize();
        byte valueB;
        short valueS;
        int valueI;
        long value;
        String str;
        switch (size) {
        case 1:
            valueB = mem.get(start);
            value = valueB;
            str = Byte.toString(valueB);
            break;
        case 2:
            valueS = mem.getShort(start);
            value = valueS;
            str = Short.toString(valueS);
            break;
        case 4:
            valueI = mem.getInt(start);
            value = valueI;
            str = Integer.toString(valueI);
            break;
        case 8:
            value = mem.getLong(start);
            str = Long.toString(value);
            break;
        default:
            return "???";
        }
        if (name.equals("bool")) return value!=0?"true":"false";
        else return str;
    }

    public String getValue(TypeVisitor v) {
        if (name.equals("float")) return Float.toString(v.getFloat());
        if (name.equals("double")) return Double.toString(v.getDouble());
        int size = getSize();
        String str;
        long value;
        switch (size) {
        case 1:
            value = v.getByte();
            str = Byte.toString(v.getByte());
            break;
        case 2:
            value = v.getShort();
            str = Short.toString(v.getShort());
            break;
        case 4:
            value = v.getInteger();
            str = Integer.toString(v.getInteger());
            break;
        case 8:
            value = v.getLong();
            str = Long.toString(v.getLong());
            break;
        default:
            return "???";
        }
        if (name.equals("bool")) return value!=0?"true":"false";
        else return str;
    
    }

    public void visit(TypeVisitor v) {
        if (v.isValid()) {
            String str = null;
            if (name.equals("float")) str = Float.toString(v.getFloat());
            if (name.equals("double")) str = Double.toString(v.getDouble());
            int size = getSize();
            long value=0;
            switch (size) {
            case 1:
                value = v.getByte();
                str = Byte.toString(v.getByte());
                break;
            case 2:
                value = v.getShort();
                str = Short.toString(v.getShort());
                break;
            case 4:
                value = v.getInteger();
                str = Integer.toString(v.getInteger());
                break;
            case 8:
                value = v.getLong();
                str = Long.toString(v.getLong());
                break;
            default:
                str = "???";
            }
            if (name.equals("bool")) str = value!=0?"true":"false";
            v.addValue(str);
        } else v.addValue("?");
    }
}
