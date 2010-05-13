package charm.debug.pdata;

import java.util.SortedSet;
import java.util.TreeSet;

import charm.debug.inspect.InspectPanel;
import charm.debug.EpCheckBox;

// Information regarding an entry method
public class EpInfo extends GenericInfo implements Cloneable {
    String name;
    int epIdx;
    int msgIdx;
    ChareTypeInfo chare;
    EpCheckBox checkBox;
    SortedSet breakpoints;

    EpInfo(String n, int e, int m, ChareTypeInfo c) {
        name = n;
        epIdx = e;
        msgIdx = m;
        chare = c;
        checkBox = null;
        breakpoints = new TreeSet();
    }

    public String toString() {
        return name;//+" ("+epIdx+","+msgIdx+","+chareIdx+")";
    }
    public int getChareType() {
        return chare.getIndex();
    }
    public String getChareName() {
    	return chare.name;
    }
    public int getEpIndex() {
    	return epIdx;
    }
    public SortedSet getBPSet() {
    	return breakpoints;
    }
    public void setCheckBox(EpCheckBox cb) {
    	checkBox = cb;
    }
    public EpCheckBox getCheckBox() {
    	return checkBox;
    }
    public void addBP(SortedSet bp) {
    	breakpoints.addAll(bp);
    }
    public void removeBP(SortedSet bp) {
    	breakpoints.removeAll(bp);
    }

    public void getDetails(InspectPanel panel) {
        panel.load("<html>chare type "+chare.getIndex()+": "+chare.getType()+"<br>"+
            "entry point "+epIdx+": "+name+"</html>");//+"\n"+
        //"message type: "+msgIdx;
    }
    
    public Object clone() {
    	return new EpInfo(name, epIdx, msgIdx, chare);
    }
    
    public boolean equals(String chareName, String epName) {
    	//System.out.println("EpInfo.equal: "+chare.getType()+"=="+chareName+" and "+name+"=="+epName);
    	return (chare.getType().equals(chareName) && name.startsWith(epName));
    }
}

