/* Class containing a single slot of memory, as received from the running program.
   Filippo Gioachin, gioachin@ieee.org, 10/04/2005
*/

package charm.debug.pdata;

import charm.debug.Symbol;
import java.util.Vector;

public class Slot implements Comparable {
	private long location;
	private int size;
	private boolean isLeak;
	private int type;
	private int chareID;
	private Vector backtrace;

	public static final int LEAK_FLAG = 0x8;
	public static final int TYPE_MASK = 0x7;
	public static final int UNKNOWN_TYPE = 0x0;
	public static final int SYSTEM_TYPE = 0x1;
	public static final int USER_TYPE = 0x2;
	public static final int CHARE_TYPE = 0x3;
	public static final int MESSAGE_TYPE = 0x4;

	public Slot(long loc) {
		location = loc;
		isLeak = false;
		type = 0;
		chareID = 0;
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
	
	public void setChareID(int c) {
		chareID = c;
	}

	public void addTrace(Object elem) {
		backtrace.add(elem);
	}

	public long getLocation () {return location;}
	public int getSize () {return size;}
	public boolean isLeak() {return isLeak;}
	public int getType() {return type;}
	public int getChareID() {return chareID;}
	public int getTraceSize() {return backtrace.size();}
	public Object getTrace(int i) {return backtrace.elementAt(i);}

	public String toString() {
		StringBuffer tmp = new StringBuffer();
		if (isLeak()) tmp.append("*** LEAKING ***\n");
		tmp.append("Memory type: ");
		switch (type) {
		case 1:
			tmp.append("system");
			break;
		case 2:
			tmp.append("user");
			break;
		case 3:
			tmp.append("chare object");
			break;
		case 4:
			tmp.append("message");
			break;
		default:
			tmp.append("unknown");
		}
		tmp.append("\nSlot at position 0x" + Long.toHexString(location) + " of size " + size + " bytes.");
		tmp.append("Belonging to chare "+chareID+". Backtrace:\n");
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
