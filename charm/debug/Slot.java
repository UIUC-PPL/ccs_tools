/* Class containing a single slot of memory, as received from the running program.
   Filippo Gioachin, gioachin@ieee.org, 10/04/2005
*/

package charm.debug;

import java.util.Vector;

public class Slot implements Comparable {
    private int location;
    private int size;
    private boolean isLeak;
    private int type;
    private Vector backtrace;

    public static final int LEAK_FLAG = 0x8;
    public static final int UNKNOWN_TYPE = 0x0;
    public static final int SYSTEM_TYPE = 0x1;
    public static final int USER_TYPE = 0x2;
    public static final int CHARE_TYPE = 0x3;
    public static final int MESSAGE_TYPE = 0x4;

    public Slot(int loc) {
	location = loc;
	isLeak = false;
	type = 0;
	backtrace = new Vector();
    }

    public void setSize(int sz) {
	size = sz;
    }

    public void setLeak(boolean l) {
	isLeak = l;
    }

    public void setType(int t) {
	type = t;
    }

    public void addTrace(Object elem) {
	backtrace.add(elem);
    }

    public int getLocation () {return location;}
    public int getSize () {return size;}
    public boolean isLeak() {return isLeak;}
    public int getType() {return type;}
    public int getTraceSize() {return backtrace.size();}
    public Object getTrace(int i) {return backtrace.elementAt(i);}

    public String toString() {
	StringBuffer tmp = new StringBuffer();
	if (isLeak()) tmp.append("*** LEAKING ***\n");
	tmp.append("Slot at position 0x" + Integer.toHexString(location) + " of size " + size + " bytes. Backtrace:\n");
	for (int i=0; i<backtrace.size(); ++i) {
	    tmp.append("\tfunction ").append((Symbol)backtrace.elementAt(i)+"\n");
	}
	return tmp.toString();
    }

    public int compareTo(Object o) {
	Slot os = (Slot)o;
	return location==os.location ? 0 : (location>os.location ? 1 : -1);
    }
}
