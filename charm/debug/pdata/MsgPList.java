package charm.debug.pdata;

import charm.debug.fmt.*;
import java.util.Vector;
import javax.swing.DefaultListModel;

// Extract messages from the converse/localqueue PList
public class MsgPList extends GenericPList {
    EpPList epList;
    MsgTypePList msgList;
    ChareTypePList chareList;

    public MsgInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (MsgInfo)data.elementAt(i);
    }

    public boolean needRefresh() {
        return true;
    }

    public void setLookups(EpPList ep, MsgTypePList msg, ChareTypePList chare) {
        epList = ep;
        msgList = msg;
        chareList = chare;
    }

    public void load(PList list) {
        data.clear();
	if (list==null) System.out.println("list is null!");
	for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) {
            PList lcur=(PList)cur; // because cur is itself an object
            
            PList msg = (PList)lcur.elementNamed("charmMsg");
            if (msg != null) {
                int from = ((PNative)msg.elementNamed("from")).getIntValue(0);
                int prioBits = ((PNative)msg.elementNamed("prioBits")).getIntValue(0);
                int userSize = ((PNative)msg.elementNamed("userSize")).getIntValue(0);
                int msgType = ((PNative)msg.elementNamed("msgType")).getIntValue(0);
                int msgFor = ((PNative)msg.elementNamed("msgFor")).getIntValue(0);
                int ep = ((PNative)msg.elementNamed("ep")).getIntValue(0);
                PList msgData = (PList)msg.elementNamed("data");
                EpInfo epEntry = epList.getEntryFor(ep);

                data.add(new MsgInfo(from, prioBits, userSize, msgList.elementAt(msgType), msgFor, chareList.elementAt(epEntry.getChareType()), epEntry, msgData));
            }
            /*
            PString lstr = (PString)lcur.elementNamed("name");
            type = addRow(lstr.getString());
            PList lst = (PList)lcur.elementNamed("slots");
            for (PAbstract lstcur=lst.elementAt(0);lstcur!=null;lstcur=lstcur.getNext()) {
                PList llcur=(PList)lstcur;
                Slot sl = new Slot(((PNative)llcur.elementNamed("loc")).getIntValue(0));
                sl.setSize(((PNative)llcur.elementNamed("size")).getIntValue(0));
		    int flags = ((PNative)llcur.elementNamed("flags")).getIntValue(0);
		    if ((flags & Slot.LEAK_FLAG) != 0) sl.setLeak(true);
		    PNative st = (PNative)llcur.elementNamed("stack");
		    for (int i=0; i<st.length(); ++i) {
			Symbol s = Symbol.get(st.getIntValue(i));
			if (s == null) {
			    // resolve the symbol in the info gdb
			    String res1 = ParDebug.servthread.infoCommand("info:info symbol "+st.getIntValue(i)+"\n");
			    //System.out.println(res1);
			    int index = res1.indexOf('+');
			    String funcName = index>=0 ? res1.substring(0, index).trim() : "??";
			    String res2 = ParDebug.servthread.infoCommand("info:info line *"+st.getIntValue(i)+"\n");
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
			    Symbol.put(st.getIntValue(i), s);
			}
			sl.addTrace(s);
		    }
		    //if (((Symbol)sl.getTrace(0)).getFunction().indexOf("CkArray::allocate(") != -1) sl.setType(Slot.CHARE_TYPE);
		    sl.setType(((PNative)llcur.elementNamed("flags")).getIntValue(0) & Slot.TYPE_MASK);
		    //if (((Symbol)sl.getTrace(0)).getFunction().equals("CkCreateLocalGroup")) sl.setType(Slot.MESSAGE_TYPE);
		    //if (((Symbol)sl.getTrace(0)).getFunction().equals("CkCreateLocalNodeGroup")) sl.setType(Slot.MESSAGE_TYPE);
		    int el = addElement(type, sl);
		}
            */
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
 
}