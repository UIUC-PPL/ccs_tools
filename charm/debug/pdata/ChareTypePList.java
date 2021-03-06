package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.ParDebug;

// Extract chares information from the charm/chares PList
public class ChareTypePList extends GenericPList {

    public ChareTypeInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (ChareTypeInfo)data.elementAt(i);
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
            long size = ((PNative)lcur.elementNamed("size")).getLongValue(0);
            //System.out.println("chare info:ptype "+name+"\n");
            String desc = ParDebug.infoCommand("ptype "+name+"\n");
            //if (desc.startsWith("no symbol")) desc = null;
            //else desc = desc.substring(7,desc.length()-7);
            int inCharm=((PNative)(lcur.elementNamed("inCharm"))).getIntValue(0);
			data.add(new ChareTypeInfo(index++, name, size, desc, (inCharm==1)));
        }
    }
 
}
