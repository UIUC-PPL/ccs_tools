package charm.debug.pdata;

import charm.debug.fmt.*;
import charm.debug.inspect.Inspector;
import java.nio.*;

// Extract messages information from the charm/messages PList
public class ReadonlyPList extends GenericPList {

    public ReadonlyInfo elementAt(int i) {
        if (i >= data.size()) return null;
        return (ReadonlyInfo)data.elementAt(i);
    }

    public boolean needRefresh() {
        return true;
    }

    public void load(PList list) {
        data.clear();
	if (list==null) System.out.println("list is null!");
	for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) {
            PList lcur=(PList)cur; // because cur is itself an object
            
            String name = ((PString)lcur.elementNamed("name")).getString();
            String type = ((PString)lcur.elementNamed("type")).getString();
            long size = ((PNative)lcur.elementNamed("size")).getLongValue(0);
            byte[] memory = ((PString)lcur.elementNamed("value")).getBytes();
            data.add(new ReadonlyInfo(name, Inspector.getTypeCreate(type), size, ByteBuffer.wrap(memory).order(Inspector.getByteOrder())));
        }
    }
}
