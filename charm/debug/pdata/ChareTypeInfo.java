package charm.debug.pdata;

import charm.debug.inspect.Inspector;
import charm.debug.inspect.InspectPanel;

// Information regarding a chare type
public class ChareTypeInfo extends GenericInfo {
    int index;
    String name;
    long size;
    String description;
	boolean system;

    ChareTypeInfo(int i, String n, long s, String d, boolean sys) {
        index = i;
        name = n;
        size = s;
        description = d;
		system = sys;
    }

    public String toString() {
        return name+" ("+size+" bytes)";
    }
    public String getType() {
        return name;
    }
    public int getIndex() {
        return index;
    }
    public String getDescription() {
        return description;
    }
	public boolean isSystem() {
		return system;
	}
    public void getDetails(InspectPanel panel) {
        System.out.println(Inspector.getTypeCreate(name)+"\n\n"+description);
        String str = "<html>Type "+index+": "+name+", size "+size+" bytes"+
			(system?" SYSTEM":"")+"<br>"+
            ((description!=null)?("Description:<br><pre>"+/*description*/Inspector.getTypeCreate(name).toString()+"</pre>"):"Description not available")+"</html>";
        panel.load(str);
    }
}

