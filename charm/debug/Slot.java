/* Class containing a single slot of memory, as received from the running program.
   Filippo Gioachin, gioachin@ieee.org, 10/04/2005
*/

package charm.debug;

import java.util.Vector;

public class Slot implements Comparable {
    private int location;
    private int size;
    private Vector backtrace;

    public Slot(int loc) {
	location = loc;
	backtrace = new Vector();
    }

    public void setSize(int sz) {
	size = sz;
    }

    public void addTrace(Object elem) {
	backtrace.add(elem);
    }

    public int getLocation () {return location;}
    public int getSize () {return size;}
    public int getTraceSize() {return backtrace.size();}
    public Object getTrace(int i) {return backtrace.elementAt(i);}

    public String toString() {
	StringBuffer tmp = new StringBuffer("Slot at position 0x" + Integer.toHexString(location) + " of size " + size + "bytes. Backtrace:\n");
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
