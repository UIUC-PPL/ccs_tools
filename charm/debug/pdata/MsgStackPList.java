package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.inspect.Inspector;

// Extract messages from the converse/localqueue PList
public class MsgStackPList extends GenericPList {

    public MsgStackInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (MsgStackInfo)data.elementAt(i);
    }

    public boolean needRefresh() {
        return true;
    }

    public void load(PList list) {
    	data.clear();
    	if (list==null) System.out.println("list is null!");
    	int i=0;
    	for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) {
    		PList lcur=(PList)cur; // because cur is itself an object

    		long obj, msg;
    		PNative lobj = (PNative)lcur.elementNamed("obj");
    		PNative lmsg = (PNative)lcur.elementNamed("msg");
    		if (Inspector.is64bit()) {
    			obj = lobj.getLongValue(0);
    			msg = lmsg.getLongValue(0);
    		} else {
    			obj = lobj.getIntValue(0);
    			msg = lmsg.getIntValue(0);
    		}
    		data.add(new MsgStackInfo(i++, obj, msg));
    	}
    }

}
