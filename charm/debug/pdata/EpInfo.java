package charm.debug.pdata;

import javax.swing.*;
import charm.debug.inspect.InspectPanel;
import charm.debug.EpCheckBox;

// Information regarding an entry method
public class EpInfo extends GenericInfo {
    String name;
    int epIdx;
    int msgIdx;
    ChareTypeInfo chare;
    EpCheckBox checkBox;

    EpInfo(String n, int e, int m, ChareTypeInfo c) {
        name = n;
        epIdx = e;
        msgIdx = m;
        chare = c;
        checkBox = null;
    }

    public String toString() {
        return name;//+" ("+epIdx+","+msgIdx+","+chareIdx+")";
    }
    public int getChareType() {
        return chare.getIndex();
    }
    public int getEpIndex() {
    	return epIdx;
    }
    public void setCheckBox(EpCheckBox cb) {
    	checkBox = cb;
    }

    public void getDetails(InspectPanel panel) {
        panel.load("<html>chare type "+chare.getIndex()+": "+chare.getType()+"\n"+
            "entry point "+epIdx+": "+name+"</html>");//+"\n"+
        //"message type: "+msgIdx;
    }
}

