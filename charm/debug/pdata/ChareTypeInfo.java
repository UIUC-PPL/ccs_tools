package charm.debug.pdata;

import javax.swing.JLabel;
import javax.swing.JComponent;
import charm.debug.inspect.Inspector;

// Information regarding a chare type
public class ChareTypeInfo extends GenericInfo {
    int index;
    String name;
    int size;
    String description;

    ChareTypeInfo(int i, String n, int s, String d) {
        index = i;
        name = n;
        size = s;
        description = d;
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
    public JComponent getDetails() {
        System.out.println(Inspector.getTypeCreate(name));
        String str = "Type "+index+": "+name+", size "+size+" bytes\n"+
            ((description!=null)?("Description:\n"+description):"Description not available");
        return new JLabel(str);
    }
}

