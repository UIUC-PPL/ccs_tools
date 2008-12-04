/*
 Simple CpdList ccs client: prints out contents of list.
 Orion Sky Lawlor, olawlor@acm.org, 6/7/2002
 */
package charm.debug;

import charm.ccs.CcsServer;

public class TinyClient
{
    private CpdUtil cpd;
    private void decodeList(byte[] buf) {
      charm.debug.fmt.Verbose dest=new charm.debug.fmt.Verbose();
      dest.decode(buf);
    }

    public TinyClient(CcsServer ccs_,String listName,int forPE, int lo, int hi) {
    	cpd=new CpdUtil(ccs_);
	charm.debug.inspect.Inspector.initialize(cpd);
	if (listName==null) listName="converse/lists";
	int nItems=cpd.getListLength(listName,forPE);
	System.out.println("Cpd list "+listName+" contains "+nItems+" items");
	
	if (lo==-1) lo=0;
	if (hi==-1) hi=nItems;
	String items=cpd.stringList(listName,forPE,lo,hi);
	System.out.println("\n------ Text version: \n"+items);
	
	byte[] bitems=cpd.byteList(listName,"fmt",forPE,lo,hi,null);
	System.out.println("\n------ Binary version ("+
	      bitems.length+" bytes)");
	for (int i=0;i<bitems.length;++i) System.out.println(bitems[i]+" "+(char)bitems[i]);
	System.out.println("}");
	decodeList(bitems);
	System.out.println("string={"+listName+"}"+listName.equals("converse/memory"));
    }
    
    public static void main(String args[]) {
    	if (args.length<2) {
		System.out.println("Usage: java client <host> <port> "+
			"[ <list> [ <pe> ] [ <lo> [ <hi> ] ] ]");
		System.exit(1);
	}
    	String[] ccsArgs=new String[2];
	ccsArgs[0]=args[0]; ccsArgs[1]=args[1];
	CcsServer ccs=CcsServer.create(ccsArgs,false);
	System.out.println("Connected: The CCS server has "+ccs.getNumPes()+" processors."); 
	String listName=null;
        int forPE=0;
	if (args.length>2) listName=args[2];
        if (args.length>3) forPE=Integer.parseInt(args[3]);
    int lo = -1;
    int hi = -1;
    if (args.length>4) lo=Integer.parseInt(args[4]);
    if (args.length>5) hi=Integer.parseInt(args[5]);
    System.out.println("args "+args.length+": "+lo+" "+hi);
	new TinyClient(ccs,listName,forPE,lo,hi);
    }
};
