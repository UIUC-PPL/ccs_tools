package charm.debug.inspect;

import java.nio.ByteBuffer;

class SuperClassElement extends GenericElement {

    SuperClassElement(GenericType t, int o) {
        type = t;
        offset = o;
    }

    public String getName() { return null; }

    public String toString(String indent) {
        return type.toString(indent)+" ("+offset+")";
    }
    
    public String toString() {
    	return type.getName()+" ("+offset+")";
    }
    
    public String memoryToString(String indent, ByteBuffer mem, int start) {
        return type.getName()+" = "+type.memoryToString(indent, mem, (offset>=0&&start>=0)?start+offset:-1);
    }

    public void visit(TypeVisitor v) {
        v.seek(offset);
        v.addType(type.getName());
        type.visit(v);
        v.revertSeek();
        v.setStatus(TypeVisitor.SUPERCLASS);
    }
}