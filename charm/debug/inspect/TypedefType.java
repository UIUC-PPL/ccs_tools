package charm.debug.inspect;

import java.nio.ByteBuffer;

/** Class representing a typedef defined type */
public class TypedefType extends GenericType {
    GenericType real;
    int pointer;

    public GenericType build(String n, String d) {
        name = n;
        String realName = name;
	String realDesc = d;
	int ptr = 0;
	while (true) {
	    int p = 0;
	    for (int i=realDesc.length()-1; i>=0; --i) {
		if (realDesc.charAt(i) == '*') p++;
		else break;
	    }
	    ptr += p;
	    realName = realDesc.substring(0, realDesc.length()-p).trim();
	    realDesc = getDescription(realName);
	    if (realDesc.indexOf("{") != -1 || realDesc.equals(realName)) break;
	}
        System.out.println("realName: "+realName);
        GenericType realType = Inspector.getType(realName);
        if (realType == null) {
            realType = new DataType();
            Inspector.putType(realName, realType);
            realType.build(realName, realDesc);
        }
        real = realType;
        pointer = ptr;
        return this;
    }

    public GenericType build(String n, DataType t, int p) {
        name = n;
        pointer = p;
        real = t;
        return this;
    }

    public int getChildren() { return real.getChildren(); }
    public GenericElement getChild(int i) { return real.getChild(i); }
    public String getValue(TypeVisitor v) { 
        StringBuffer buf = new StringBuffer(real.getName());
        for (int i=0; i<pointer; ++i) buf.append('*');
        if (real.getChildren() == 0) buf.append(' ').append(real.getValue(v));
        return buf.toString();
    }
    public int getPointer() { return pointer; }

    public boolean isVirtual() {
        if (real instanceof DataType) {
            return ((DataType)real).isVirtual;
        }
        return false;
    }

    public int getSize() {
        if (pointer>0) return pointerSize();
        return real.getSize();
    }
    
    public GenericType getType() {
    	return real.getType();
    }
    
    public boolean equals(GenericType t) {
    	return name.equals(t.name) || real.equals(t);
    }
    
    public boolean equals(TypedefType t) {
    	return name.equals(t.name) || equals(t.real);
    }

    public String toString (String indent) {
        System.out.println("typedef: |"+name+"| to |"+real.getName()+"|");
        StringBuffer buf = new StringBuffer();
        buf.append(name).append(" (");
        buf.append(real.toString(indent)).append(")");
        if (pointer > 0) {
            buf.append(" ");
            for (int i=0; i<pointer; ++i) buf.append("*");
        }
        return buf.toString();
    }

    public String memoryToString(String indent, ByteBuffer mem, int start) {
        if (pointer>0) {
            StringBuffer buf = new StringBuffer();
            buf.append(real.getName());
            for (int i=0; i<pointer; ++i) buf.append("*");
            buf.append(" ").append(printPointer(mem, start));
            return buf.toString();
        }
        return real.getName()+" "+real.memoryToString(indent, mem, start);
    }

    public void visit(TypeVisitor v) {
        if (pointer>0) {
            v.setPointer(pointer);
            v.addValue(v.printPointer());
        } else {
            v.addValue(real.getName());
            v.push();
            real.visit(v);
            v.pop();
        }
    }
}
