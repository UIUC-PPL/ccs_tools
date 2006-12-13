package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.ParDebug;
import charm.debug.inspect.*;
import java.nio.ByteBuffer;
import javax.swing.*;

// Information regarding a message in the queue
public class ChareInfo extends GenericInfo {
    String name;
    byte[] id;
    GenericType type;
    ByteBuffer memory;

    ChareInfo(String n, byte[] i, GenericType t, ByteBuffer m) {
        name = n;
        id = i;
        type = t;
        memory = m;
    }

    public String toString() {
        return name;//type.toString()+"\nFrom "+from+" of size "+userSize+"\nTo: "+
            //type.getName();//+"::"+ep.toString();
        //+userData.toString()+"\n";
    }

    public JComponent getDetails() {
        if (type != null) {
            StringVisitor st = new StringVisitor(memory);
            st.visit(type);
            System.out.println(st.getResult());
            JTreeVisitor jtv = new JTreeVisitor(memory, type.getName());
            jtv.visit(type);
/*            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);*/
//            JPanel panel = new JPanel()
            //JScrollPane scr = new JScrollPane((JTree)jtv.getResult());
            //panel.add(scr);
            //frame.pack();
            //frame.setVisible(true);
            //return type.memoryToString(memory);
            return (JTree)jtv.getResult();
        }
        else if (id != null) {
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
            /*
            StringBuffer buf = new StringBuffer();
            ByteBuffer bid = ByteBuffer.wrap(id).order(Inspector.getByteOrder());
            byte numType = bid.get(0);
            buf.append("type: ").append(numType).append("\n");
            switch (numType) {
            case 1:
                buf.append("group ").append(bid.getInt(1));
                break;
            case 2:
                buf.append("array ").append(bid.getInt(1));
                buf.append(Inspector.getTypeCreate("CkArrayIndexMax").memoryToString(" ", bid, 5));
            }
            return buf.toString();
            */
        }
        return null;
        /*System.out.println(Inspector.getTypeCreate(chare.getType()));
        return "Sender processor: "+from+"\n"+
            "Destination: "+chare.getType()+"::"+ep.toString()+" (type "+msgFor+")\n"+
            "Size: "+userSize+"\n"+
            "User data:\n"+userData.toString();*/
    }
}

