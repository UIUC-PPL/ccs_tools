package charm.debug;

import charm.ccs.CcsServer;
import charm.debug.fmt.*;

/**
  Set the value of a readonly global variable.
*/
public class SetReadonly {
  
  class ReadonlyInfo {
    public int idx; // entry in readonly cpdList
    public String name; // name of readonly (e.g., "nElements")
    public String type; // string data type of readonly (e.g., "int")
    public long size; // native size (in bytes) of readonly
    public PAbstract value; // value of readonly
  };
  
  /**
    Return the 0-based index of the plist entry with this name.
    Takes a PList of variable names.
  */
  public ReadonlyInfo findName(String name,PList list,boolean verbose) {
    int count=0;
    for (PAbstract cur=list.elementAt(0);cur!=null;cur=cur.getNext()) 
    {
       PList lcur=(PList)cur; // because cur is itself an object
       PString n=(PString)(lcur.elementNamed("name"));
       if (verbose) System.out.println("Readonly '"+n.getString()+"'");
       if (n.getString().equals(name)) {
         ReadonlyInfo ri=new ReadonlyInfo();
         ri.idx=count;
	 ri.name=n.getString();
	 ri.type=((PString)(lcur.elementNamed("type"))).getString();
	 ri.size=((PNative)(lcur.elementNamed("size"))).getLongValue(0);
	 ri.value=lcur.elementNamed("value");
	 return ri;
       }
       count++;
    }
    return null;
  }    
  
  private CpdUtil cpd;
  
  
  public SetReadonly(CcsServer ccs,String args[]) {
        cpd=new CpdUtil(ccs);
	int nPe=ccs.getNumPes();
	System.out.println("Connected: The CCS server has "+nPe+" processors."); 
	String listName="charm/readonly";
	String roName=args[2];
	String roValue=null;
	if (args.length>3) roValue=args[3];
	
	PList ros=cpd.getPList(listName,0);
	ReadonlyInfo ri=findName(roName,ros,false);
	if (ri==null) {
		findName("",ros,true);// print out readonlies 
		cpd.abort("Can't find readonly "+roName);
	}
	if (!ri.type.equals("int")) cpd.abort("Can only set int readonlies, not "+ri.type);
	System.out.println("Old value: "+ri.value);
	if (roValue!=null) {
	  int val=Integer.parseInt(roValue);
	  byte[] bval=new byte[8+4];
	  int o=0,l;
	  l=8; CcsServer.writeLong(bval,o,ri.size); o+=l;
	  l=4; CcsServer.writeInt(bval,o,val); o+=l;
	  for (int pe=0;pe<nPe;pe++)
	  	cpd.setListItem(listName,pe,ri.idx,bval);
	  System.out.println("New value: "+val);
	}
	System.exit(0);
  }

  
  public static void main(String args[]) {
    	if (args.length!=3 && args.length!=4) {
		System.out.println("Usage: setReadonly <host> <port> "+
			"  <readonly name> <new value>");
		System.exit(1);
	}
    	String[] ccsArgs=new String[2];
	ccsArgs[0]=args[0]; ccsArgs[1]=args[1];
	CcsServer ccs=CcsServer.create(ccsArgs,false);
	new SetReadonly(ccs,args);
  }
};
