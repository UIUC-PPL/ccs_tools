package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.inspect.Inspector;
import java.nio.*;

// Extract messages from the converse/localqueue PList
public class CharePList extends GenericPList {
    EpPList epList;
    MsgTypePList msgList;
    ChareTypePList chareList;

    public ChareInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (ChareInfo)data.elementAt(i);
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
            
            String name = ((PString)lcur.elementNamed("name")).getString();
            PAbstract info;
            info = lcur.elementNamed("id");
            byte[] id = null;
            if (info != null) id = ((PString)info).getBytes();
            String type = null;
            info = lcur.elementNamed("type");
            if (info != null) type = ((PString)info).getString();
            ByteBuffer buf = null;
            info = lcur.elementNamed("value");
            if (info != null) buf = ByteBuffer.wrap(((PString)info).getBytes()).order(Inspector.getByteOrder());
            data.add(new ChareInfo(name, id, type!=null?Inspector.getTypeCreate(type):null, buf));
        }
    }
 
}
