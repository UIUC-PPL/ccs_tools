package charm.debug.pdata;

import javax.swing.*;

import charm.debug.fmt.PList;
import charm.debug.inspect.Inspector;

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

    MsgInfo(int f, int p, int s, MsgTypeInfo t, int mf, ChareTypeInfo c, EpInfo e, PList d) {
        from = f;
        prioBits = p;
        userSize = s;
        type = t;
        msgFor = mf;
        chare = c;
        ep = e;
        userData = d;
    }

    public String toString() {
        return //type.toString()+"\nFrom "+from+" of size "+userSize+"\nTo: "+
            chare.getType()+"::"+ep.toString();
        //+userData.toString()+"\n";
    }

    public JComponent getDetails() {
        System.out.println(Inspector.getTypeCreate(chare.getType()));
        return new JLabel("Sender processor: "+from+"\n"+
            "Destination: "+chare.getType()+"::"+ep.toString()+" (type "+msgFor+")\n"+
            "Size: "+userSize+"\n"+
            "User data:\n"+userData.toString());
    }
}

