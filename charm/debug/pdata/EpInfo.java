package charm.debug.pdata;

import javax.swing.*;

// Information regarding an entry method
public class EpInfo extends GenericInfo {
    String name;
    int epIdx;
    int msgIdx;
    ChareTypeInfo chare;

    EpInfo(String n, int e, int m, ChareTypeInfo c) {
        name = n;
        epIdx = e;
        msgIdx = m;
        chare = c;
    }

    public String toString() {
        return name;//+" ("+epIdx+","+msgIdx+","+chareIdx+")";
    }
    public int getChareType() {
        return chare.getIndex();
    }

    public JComponent getDetails() {
        return new JLabel("chare type "+chare.getIndex()+": "+chare.getType()+"\n"+
            "entry point "+epIdx+": "+name);//+"\n"+
        //"message type: "+msgIdx;
    }
}

