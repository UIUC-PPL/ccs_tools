package charm.debug;

import charm.debug.fmt.*;
import java.util.Vector;

// Extract entry-point information from the entry point PList
public class EpPList
{
  protected Vector systemEps;
  protected Vector userEps;
  
  public Vector getUserEps()
  {
     return userEps;
  }

  public Vector getSystemEps()
  {
     return systemEps;
  }
  
  public EpPList (PList list)
  {
     systemEps=new Vector();
     userEps=new Vector();
     int count=0;
     for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) 
     {
	PList lcur=(PList)cur; // because cur is itself an object
        PString name=(PString)(lcur.elementNamed("name"));
	PNative inCharm=(PNative)(lcur.elementNamed("inCharm"));
	if (inCharm.getIntValue(0)==1) /* intrinsic */
	   systemEps.add(name.getString());
	else 
	   userEps.add(name.getString());
	count++;
     }
  }
 
};


