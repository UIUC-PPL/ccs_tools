package charm.debug.pdata;

import charm.debug.inspect.*;
import java.nio.ByteBuffer;

// Information regarding a message type
public class ReadonlyInfo extends GenericInfo {
    String name;
    GenericType type;
    long size;
    ByteBuffer memory;

    ReadonlyInfo(String n, GenericType t, long s, ByteBuffer m) {
        name = n;
        type = t;
        size = s;
        memory = m;
    }

    public String toString() {
        return type.getName()+" "+name;
    }
    public void getDetails(InspectPanel panel) {
        System.out.print("memory = "+memory+" ");
        for (int i=0; i<memory.limit(); ++i) System.out.print(" "+Integer.toHexString(memory.get(i)));
        //System.out.println("");
        if (type != null) {
        	VariableElement el = new VariableElement(type, name, 0, 0, 0);
        	/*StringVisitor st = new StringVisitor(memory);
            st.visit(el);
            System.out.println(st.getResult());
            JTreeVisitor jtv = new JTreeVisitor(memory, null);
            jtv.visit(el);
            return (JTree)jtv.getResult();*/
        	panel.load(el, memory, 0);
        }
        /*else if (id != null) {
        	int nItems = ParDebug.server.getListLength("charm/object",ParDebug.currentListedPE);
        	PList list = ParDebug.server.getPList("charm/object",ParDebug.currentListedPE,0,nItems,id);
        	if (list==null) return null;
        	PList cur = (PList)list.elementAt(0);
        	type = Inspector.getTypeCreate(((PString)cur.elementNamed("type")).getString());
        	memory = ByteBuffer.wrap(((PString)cur.elementNamed("value")).getBytes()).order(Inspector.getByteOrder());

        	System.out.println("Loaded type "+name);
        	StringVisitor st = new StringVisitor(memory);
        	st.visit(type);
        	System.out.println(st.getResult());
        	JTreeVisitor jtv = new JTreeVisitor(memory, type.getName());
        	jtv.visit(type);
        	//return type.memoryToString(memory);
        	//JScrollPane scr = new JScrollPane((JTree)jtv.getResult());
        	return (JTree)jtv.getResult();
        }*/
        else panel.load(type.getName()+" "+name+" = "+type.memoryToString(memory));
        //return new JLabel(type.getName()+" "+name+" = "+type.memoryToString(memory));
    }
}

