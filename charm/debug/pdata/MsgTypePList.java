package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.ParDebug;
import charm.debug.inspect.Inspector;

// Extract messages information from the charm/messages PList
public class MsgTypePList extends GenericPList {

    public MsgTypeInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (MsgTypeInfo)data.elementAt(i);
    }

    public boolean needRefresh() {
        return false;
    }

    public void load(PList list) {
        data.clear();
        int index = 0;
	if (list==null) System.out.println("list is null!");
	for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) {
            PList lcur=(PList)cur; // because cur is itself an object
            
            String name = ((PString)lcur.elementNamed("name")).getString();
            int size;
            if (Inspector.is64bit()) size = (int)((PNative)lcur.elementNamed("size")).getLongValue(0);
            else size = ((PNative)lcur.elementNamed("size")).getIntValue(0);
            //System.out.println("msg info:ptype "+name+"\n");
            String desc = ParDebug.infoCommand("ptype "+name+"\n");
            //if (desc.startsWith("no symbol")) desc = null;
            //else desc = desc.substring(7,desc.length()-7);
            data.add(new MsgTypeInfo(index++, name, size, desc));
        }
    }
 
}
