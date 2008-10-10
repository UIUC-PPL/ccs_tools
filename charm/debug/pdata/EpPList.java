package charm.debug.pdata;

import charm.debug.fmt.*;
import javax.swing.DefaultListModel;
import java.util.Vector;

// Extract entry-point information from the entry point PList
public class EpPList extends GenericPList implements Cloneable {
    protected Vector systemEps;
    protected Vector userEps;
    private ChareTypePList chareList;

    public Vector getUserEps() {
        return userEps;
    }

    public Vector getSystemEps() {
        return systemEps;
    }
  
    public EpInfo getEntryFor(int index) {
        for (int i=0; i<userEps.size(); ++i) {
            if (((EpInfo)userEps.elementAt(i)).epIdx == index) return (EpInfo)userEps.elementAt(i);
        }
        for (int i=0; i<systemEps.size(); ++i) {
            if (((EpInfo)systemEps.elementAt(i)).epIdx == index) return (EpInfo)systemEps.elementAt(i);
        }
        return null;
    }

    public void setLookups(ChareTypePList chare) {
        chareList = chare;
    }

    public boolean needRefresh() {
        return false;
    }

    public void populate(DefaultListModel model) {
        for (int i=0; i<userEps.size(); ++i) {
            model.addElement(userEps.elementAt(i));
        }
        for (int i=0; i<systemEps.size(); ++i) {
            model.addElement(systemEps.elementAt(i));
        }
    }
    public EpPList() {
        systemEps=new Vector();
        userEps=new Vector();
    }

    public void load(PList list) {
        systemEps.clear();
        userEps.clear();
        for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) {
            PList lcur=(PList)cur; // because cur is itself an object
            PString name=(PString)(lcur.elementNamed("name"));
            PNative index=(PNative)(lcur.elementNamed("index"));
            PNative msgIdx=(PNative)(lcur.elementNamed("msgIdx"));
            PNative chareIdx=(PNative)(lcur.elementNamed("chareIdx"));
            PNative inCharm=(PNative)(lcur.elementNamed("inCharm"));
            if (inCharm.getIntValue(0)==1) /* intrinsic */
                systemEps.add(new EpInfo(name.getString(), index.getIntValue(0), msgIdx.getIntValue(0), chareList.elementAt(chareIdx.getIntValue(0))));
            else 
                userEps.add(new EpInfo(name.getString(), index.getIntValue(0), msgIdx.getIntValue(0), chareList.elementAt(chareIdx.getIntValue(0))));
        }
    }
    
    public Object clone() {
    	EpPList ret = new EpPList();
    	ret.chareList = this.chareList;
    	ret.systemEps = new Vector();
    	for (int i=0; i<systemEps.size(); ++i) ret.systemEps.add(((EpInfo)systemEps.elementAt(i)).clone());
    	ret.userEps = new Vector();
    	for (int i=0; i<userEps.size(); ++i) ret.userEps.add(((EpInfo)userEps.elementAt(i)).clone());    	
    	return ret;
    }
 
};


