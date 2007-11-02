package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.inspect.Inspector;
import charm.debug.Symbol;
import charm.debug.ParDebug;

import java.util.Vector;
import java.util.Collections;

// Extract memory information from the converse/memory PList
public class MemoryPList {

    // contains Vector of Slot
    private Vector slots;
    // contains String
    private Vector names;

    public int addRow(String name) {
	names.add(name);
	slots.add(new Vector());
	return slots.size()-1;
    }

    public int addElement(int type, Slot s) {
	Vector t = (Vector)slots.elementAt(type);
	t.add(s);
	return t.size()-1;
    }

    public int size() {
	return slots.size();
    }

    public int size(int type) {
	return ((Vector)slots.elementAt(type)).size();
    }

    public String getName(int type) {
	return (String)names.elementAt(type);
    }

    public Slot elementAt(int type, int index) {
	return (Slot)((Vector)slots.elementAt(type)).elementAt(index);
    }

    public void sort() {
	for (int type=0; type<names.size(); ++type) {
	    Collections.sort((Vector)slots.elementAt(type));
	}
    }

    public class Hole {
	long position;
	long size;

	public Hole() {
	    position=0;
	    size=0;
	}
	public Hole(long p, long s) {
	    position=p;
	    size=s;
	}
        public long getPosition() {
            return position;
        }
        public long getSize() {
            return size;
        }
    }

    public class MemoryIterator {
	private Vector heads;
	private MemoryPList source;

	private class Holder {
	    int type;
	    int position;
	    Slot slot;
	    Holder(int t, int p, Slot s) {
		type=t;
		position=p;
		slot=s;
	    }
	}

	public MemoryIterator(MemoryPList list) {
	    source = list;
	    heads = new Vector(list.size());
	    for (int i=0; i<list.size(); ++i) {
		if (list.size(i) == 0) continue;
		// insert a single element into the list
		add(new Holder(i, 0, list.elementAt(i, 0)));
	    }
	}

	private void add(Holder h) {
	    int j;
	    for (j=0; j<heads.size(); ++j) {
		if (h.slot.getLocation() < ((Holder)heads.elementAt(j)).slot.getLocation()) break;
	    }
	    heads.add(j, h);
	}

	public Slot getNext() {
	    if (heads.size() == 0) return null;
	    Holder result = (Holder)heads.elementAt(0);
	    heads.remove(0);
	    int pos = result.position+1;
	    if (pos < source.size(result.type)) {
		add(new Holder(result.type, pos, source.elementAt(result.type, pos)));
	    }
	    return result.slot;
	}
    }

    public static final int HOLE_SIZE = 50*1024*1024;

    /* returns an array of holes in the memory. The first position is always the
     * total number and total size of holes. WARNING: it assumes the function
     * "sort" has already been called. */
    public Hole[] findHoles() {
	Vector list = new Vector();
	Hole bookkeeper = new Hole(0, 0);
	list.add(bookkeeper);
	MemoryIterator iter = new MemoryIterator(this);
	Slot cur = iter.getNext();
	Slot next = iter.getNext();
	while (next != null) {
	    long diff = next.getLocation() - (cur.getLocation()+cur.getSize());
	    if (diff > HOLE_SIZE) {
		// found a hole, record it
		list.add(new Hole(cur.getLocation()+cur.getSize(), diff));
		bookkeeper.position++;
		bookkeeper.size += diff;
	    }
	    cur = next;
	    next = iter.getNext();
	}
	Object result[] = new Hole[(int)bookkeeper.position+1];
	result = list.toArray(result);
	return (Hole[])result;

	/*
	Hole result[] = new Hole[2];
	result[0] = new Hole();
	result[0].position=1;
	result[0].size=939636600;
	result[1] = new Hole();
	result[1].position = 136352288+100000;
	result[1].size = 939636600;
	return result;
	*/
    }
 
    public boolean needRefresh() {
        return true;
    }

    public MemoryPList() {
	slots = new Vector();
	names = new Vector();
    }

    public void load(PList list) {
        slots.clear();
        names.clear();
	int type;
	if (list==null) System.out.println("list is null!");
	for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) 
	    {
		PList lcur=(PList)cur; // because cur is itself an object
		PString lstr = (PString)lcur.elementNamed("name");
		type = addRow(lstr.getString());
		PList lst = (PList)lcur.elementNamed("slots");
		for (PAbstract lstcur=lst.elementAt(0);lstcur!=null;lstcur=lstcur.getNext()) {
		    PList llcur=(PList)lstcur;
		    Slot sl;
		    if (Inspector.is64bit()) sl = new Slot(((PNative)llcur.elementNamed("loc")).getLongValue(0));
		    else sl = new Slot(((PNative)llcur.elementNamed("loc")).getIntValue(0));
		    sl.setSize(((PNative)llcur.elementNamed("size")).getIntValue(0));
		    int flags = ((PNative)llcur.elementNamed("flags")).getIntValue(0);
		    System.out.println("Found "+sl);
		    if ((flags & Slot.LEAK_FLAG) != 0) sl.setLeak(true);
		    PNative st = (PNative)llcur.elementNamed("stack");
		    for (int i=0; i<st.length(); ++i) {
		    	long location;
		    	if (Inspector.is64bit()) location = st.getLongValue(i); 
		    	else location = st.getIntValue(i);
		    	Symbol s = Symbol.get(location);
		    	/*
		    	if (s == null) {
		    		// resolve the symbol in the info gdb
		    		String res1 = ParDebug.infoCommand("info:info symbol "+location+"\n");
		    		//System.out.println(res1);
		    		int index = res1.indexOf('+');
		    		String funcName = index>=0 ? res1.substring(0, index).trim() : "??";
		    		String res2 = ParDebug.infoCommand("info:info line *"+location+"\n");
		    		index = res2.indexOf("Line");
		    		String fileName;
		    		int line;
		    		if (index == -1) {
		    			line = 0;
		    			fileName = "??";
		    		} else {
		    			int index2 = res2.indexOf(' ', index+5);
		    			//System.out.println(res2+" "+index+" "+index2);
		    			line = Integer.parseInt(res2.substring(index+5,index2));
		    			index = res2.indexOf('"');
		    			index2 = res2.indexOf('"', index+1);
		    			fileName = res2.substring(index+1,index2).trim();
		    		}
		    		s = new Symbol(funcName, line, fileName);
		    		Symbol.put(location, s);
		    	} MOVED TO Symbol.get        */
		    	sl.addTrace(s);
		    }
		    //if (((Symbol)sl.getTrace(0)).getFunction().indexOf("CkArray::allocate(") != -1) sl.setType(Slot.CHARE_TYPE);
		    sl.setType(((PNative)llcur.elementNamed("flags")).getIntValue(0) & Slot.TYPE_MASK);
		    //if (((Symbol)sl.getTrace(0)).getFunction().equals("CkCreateLocalGroup")) sl.setType(Slot.MESSAGE_TYPE);
		    //if (((Symbol)sl.getTrace(0)).getFunction().equals("CkCreateLocalNodeGroup")) sl.setType(Slot.MESSAGE_TYPE);
		    int el = addElement(type, sl);
		}
		//System.out.println("name: "+lcur.getName());
		/*PString name=(PString)(lcur.elementNamed("name"));
		PNative inCharm=(PNative)(lcur.elementNamed("inCharm"));
		if (inCharm.getIntValue(0)==1) // intrinsic
		    systemEps.add(name.getString());
		else 
		    userEps.add(name.getString());
		count++;
		*/
	    }
    }
    
    /// Return this list as a string
    public String toString() {
	StringBuffer ret=new StringBuffer("Lists:\n");
	for (int i=0; i<names.size(); ++i) {
	    ret.append(getName(i)+" {\n");
	    for (int j=0; j<size(i); ++j) {
		ret.append("\tloc=0x"+Long.toHexString(elementAt(i,j).getLocation())+", size="+elementAt(i,j).getSize()+", trace: {\n\t\t");
		for (int k=0; k<elementAt(i,j).getTraceSize(); ++k) {
		    if (k>0) ret.append(",\n\t\t");
		    ret.append((Symbol)elementAt(i,j).getTrace(k));
		}
		ret.append("\t}\n");
	    }
	    ret.append("}\n");
	}
	return ret.toString();
    }
    
    /// Draw this object to this screen.
    public boolean draw(PDisplayStyle p,int drawStyle) {
	/*	if (!hasFew()) {
	    super.draw(p,drawStyle); // draw our name
	    p.drawString("{");
	}
	if (drawStyle==PDisplayStyle.drawStyle_multirow) 
	    { // indents and newlines
		p.addIndent(1); 
		for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
		    cur.draw(p);
		    if (cur.getNext()!=null) 
			p.newRow();
		}
		p.addIndent(-1);
	    }
	    else { // all on one line
	    for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
		if (!cur.draw(p))
		    return false;
	    }
	    }
	    if (!hasFew()) p.drawString("}");*/
	for (int i=0; i<names.size(); ++i) System.out.println("List "+getName(i));
	return true;
    }
};


