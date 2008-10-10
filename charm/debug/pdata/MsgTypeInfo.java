package charm.debug.pdata;

import javax.swing.*;
import charm.debug.inspect.InspectPanel;

// Information regarding a message type
public class MsgTypeInfo extends GenericInfo {
    int index;
    String name;
    int size;
    String description;

    MsgTypeInfo(int i, String n, int s, String d) {
        index = i;
        name = n;
        size = s;
        description = d;
    }

    public String toString() {
        return name+" ("+size+" bytes)";
    }
    public String getDescription() {
        return description;
    }
    public void getDetails(InspectPanel panel) {
        panel.load("<html>Type "+index+": "+name+", size "+size+"<br>"+
            ((description!=null)?("Description:<br><pre>"+description+"</pre>"):"Description not available")+"</html>");
    }
}

