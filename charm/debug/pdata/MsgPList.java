package charm.debug.pdata;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import charm.debug.ParDebug;
import charm.debug.fmt.*;

// Extract messages from the converse/localqueue PList
public class MsgPList extends GenericPList implements ActionListener {
    EpPList epList;
    MsgTypePList msgList;
    ChareTypePList chareList;
    PopupListener popup;
    JList list;
    JPopupMenu jp;

    public MsgInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (MsgInfo)data.elementAt(i);
    }

    public MsgPList() {
    	createPopupMenu();
    }
    
    public MsgPList(MsgPList msg) {
    	epList = msg.epList;
    	msgList = msg.msgList;
    	chareList = msg.chareList;
    	createPopupMenu();
    }
    
    private void createPopupMenu() {
        jp = new JPopupMenu();
		JMenuItem empty= new JMenuItem("Deliver now");
		empty.setActionCommand("deliver");
		empty.addActionListener(this);
		jp.add(empty);
		//MouseListener popupListener = new PopupListener(popup);
		popup = new PopupListener(jp);
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
                int envType = ((PNative)msg.elementNamed("envType")).getIntValue(0);
                int ep = ((PNative)msg.elementNamed("ep")).getIntValue(0);
                PList msgData = (PList)msg.elementNamed("data");
                EpInfo epEntry = epList.getEntryFor(ep);
                int flags = 0;
                if (((PString)lcur.elementNamed("name")).getString().equals("Breakpoint")) flags |= MsgInfo.BREAKPOINT;
                MsgInfo info = new MsgInfo(from, prioBits, userSize, msgList.elementAt(msgType), envType, chareList.elementAt(epEntry.getChareType()), epEntry, msgData, flags);
                data.add(info);
                if (msg.elementNamed("arrID") != null) {
                	int arrayID = ((PNative)msg.elementNamed("arrID")).getIntValue(0);
                	int nInts = ((PNative)msg.elementNamed("nInts")).getIntValue(0);
                	int dimension = ((PNative)msg.elementNamed("dimension")).getIntValue(0);
                	int[] index = null;
                	if (dimension > 0 && dimension <= 6) {
                		index = new int[dimension];
                		for (int i=0; i<dimension; ++i) index[i] = ((PNative)msg.elementNamed("index")).getIntValue(i);
                	}
                	info.setArrayElement(arrayID, nInts, dimension, index);
                }
                if (msg.elementNamed("groupID") != null) {
                	info.setGroupID(((PNative)msg.elementNamed("groupID")).getIntValue(0));
                }
                if (msg.elementNamed("ptr") != null) {
                	info.setObjectPtr(((PNative)msg.elementNamed("ptr")).getLongValue(0));
                }
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

    public void addPopupMenu(JList l) {
    	list = l;
    	if (! Arrays.asList(list.getMouseListeners()).contains(popup))
    		list.addMouseListener(popup);
    	//list.setComponentPopupMenu(jp);
    }
    
    public class PopupListener extends MouseAdapter {
    	JPopupMenu popup;
    	
    	PopupListener(JPopupMenu p) {
    		popup = p;
    	}
        public void mousePressed(MouseEvent e) {
        	//Component c = list.getComponentAt(e.getX(), e.getY());
        	int idx = list.locationToIndex(e.getPoint());
        	list.setSelectedIndex(idx);
        	//MsgInfo mi = (MsgInfo)list.getModel().getElementAt(idx);
        	System.out.println("mousePressed");
        	if (e.isPopupTrigger()) {
        		popup.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("deliver")) {
			int idx = list.getSelectedIndex();
			//MsgInfo mi = (MsgInfo)list.getSelectedValue();
			//Component c = list.getComponentAt(e.getX(), e.getY());
			String method = null;
			if (idx == 0) method = "ccs_single_step";
			else method = "deliverMessage";
			ParDebug.server.sendCcsRequestBytes(method, ""+idx, ParDebug.debugger.getSelectedPe());
			ParDebug.debugger.messageDelivered();
		}
	}
}
