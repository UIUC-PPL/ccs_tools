/*
 Simple CpdList ccs client: prints out contents of list.
 Orion Sky Lawlor, olawlor@acm.org, 6/7/2002
 */
package charm.debug;

import java.io.*;
import java.util.*;
import charm.ccs.CcsServer;
import charm.debug.fmt.*;

public class TinyClient
{
    private CpdUtil cpd;
    private void decodeList(byte[] buf) {
      charm.debug.fmt.Verbose dest=new charm.debug.fmt.Verbose();
      dest.decode(buf);
    }
    
    public TinyClient(CcsServer ccs_,String listName,int forPE) {
    	cpd=new CpdUtil(ccs_);
	if (listName==null) listName="converse/lists";
	int nItems=cpd.getListLength(listName,forPE);
	System.out.println("Cpd list "+listName+" contains "+nItems+" items");
	
	String items=cpd.stringList(listName,forPE,0,nItems);
	System.out.println("\n------ Text version: \n"+items);
	
	byte[] bitems=cpd.byteList(listName,"fmt",forPE,0,nItems);
	System.out.println("\n------ Binary version ("+
	      bitems.length+" bytes)");
	decodeList(bitems);
    }
    
    public static void main(String args[]) {
    	if (args.length<2) {
		System.out.println("Usage: java client <host> <port> "+
			"[ <list> [ <pe> ] ]");
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
	new TinyClient(ccs,listName,forPE);
    }
};
