package charm.debug.pdata;

import charm.debug.inspect.GenericType;
import java.nio.ByteBuffer;

import javax.swing.*;

// Information regarding a message type
public class ReadonlyInfo extends GenericInfo {
    String name;
    GenericType type;
    int size;
    ByteBuffer memory;

    ReadonlyInfo(String n, GenericType t, int s, ByteBuffer m) {
        name = n;
        type = t;
        size = s;
        memory = m;
    }

    public String toString() {
        return type.getName()+" "+name;
    }
    public JComponent getDetails() {
        System.out.print("memory = "+memory+" ");
        for (int i=0; i<memory.limit(); ++i) System.out.print(" "+Integer.toHexString(memory.get(i)));
        //System.out.println("");
        return new JLabel(type.getName()+" "+name+" = "+type.memoryToString(memory));
    }
}

