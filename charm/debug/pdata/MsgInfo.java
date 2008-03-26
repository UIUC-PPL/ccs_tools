package charm.debug.pdata;

import javax.swing.*;

import charm.debug.fmt.PList;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.InspectPanel;

// Information regarding a message in the queue
public class MsgInfo extends GenericInfo {
    int from;
    int prioBits;
    int userSize;
    MsgTypeInfo type;
    int msgFor;
    ChareTypeInfo chare;
    EpInfo ep;
    PList userData;
    int flags;
    
    public static final int BREAKPOINT = 0x1;

    MsgInfo(int f, int p, int s, MsgTypeInfo t, int mf, ChareTypeInfo c, EpInfo e, PList d, int fl) {
        from = f;
        prioBits = p;
        userSize = s;
        type = t;
        msgFor = mf;
        chare = c;
        ep = e;
        userData = d;
        flags = fl;
    }

    public String toString() {
    	StringBuffer buf = new StringBuffer("<html><body");
    	if ((flags & BREAKPOINT) != 0) buf.append(" bgcolor=\"#FF3333\"");
    	else if (ep.checkBox.isSelected()) buf.append(" bgcolor=\"#EEAA55\"");
    	buf.append(">");
    	buf.append(chare.getType()+"::"+ep.toString());
    	buf.append("</body>");
    	buf.append("</html>");
        return //type.toString()+"\nFrom "+from+" of size "+userSize+"\nTo: "+
            buf.toString();
        //+userData.toString()+"\n";
    }

    public void getDetails(InspectPanel panel) {
        System.out.println(Inspector.getTypeCreate(chare.getType()));
        panel.load("<html>Sender processor: "+from+"<br>"+
            "Destination: "+chare.getType()+"::"+ep.toString()+" (type "+msgFor+")<br>"+
            "Size: "+userSize+"<br>"+
            "User data:\n"+userData.toString()+
            "</html>");
    }
}

