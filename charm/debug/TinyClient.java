/*
 Simple CCS Client.
 Orion Sky Lawlor, olawlor@acm.org, 6/7/2002
 */
package charm.debug;

import java.io.*;
import java.util.*;
import charm.ccs.CcsServer;
import charm.debug.fmt.*;

public class TinyClient
{
    CcsServer ccs;
    //Get the length of a CpdList
    private int getListLength(String listName,int forPE) throws IOException
    {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,reqStr);
      CcsServer.writeString(req,4,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_len",forPE,req);
      
      //Get the response and take it apart:
      byte[] resp=ccs.recvResponse(r);
      if (resp.length<4) return -1;
      return CcsServer.readInt(resp,0);
    }
    
    //Convert a set of CpdList items into a string
    private byte[] byteList(String listName,String fmt,int forPE,int lo,int hiPlusOne) throws IOException
    {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+4+4+0+4+reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,lo);
      CcsServer.writeInt(req,4,hiPlusOne);
      CcsServer.writeInt(req,8,0); /*no additional request data*/
      CcsServer.writeInt(req,12,reqStr);
      CcsServer.writeString(req,16,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_items."+fmt,forPE,req);
      
      //Get the response and print it:
      byte[] resp=ccs.recvResponse(r);
      return resp;
    }
    private String stringList(String listName,int forPE,int lo,int hiPlusOne) throws IOException
    {
      return new String(byteList(listName,"txt",forPE,lo,hiPlusOne));
    }
    
    private void decodeList(byte[] buf) {
      charm.debug.fmt.Verbose dest=new charm.debug.fmt.Verbose();
      dest.decode(buf);
    }
    
    public TinyClient(CcsServer ccs_,String listName,int forPE) {
    	ccs=ccs_;
	if (listName==null) listName="converse/lists";
	try {
	  int nItems=getListLength(listName,forPE);
	  System.out.println("Cpd list "+listName+" contains "+nItems+" items");
	  
	  String items=stringList(listName,forPE,0,nItems);
	  System.out.println("\n------ Text version: \n"+items);
	 
	  byte[] bitems=byteList(listName,"fmt",forPE,0,nItems);
	  System.out.println("\n------ Binary version ("+
	  	bitems.length+" bytes)");
	  decodeList(bitems);
	}
	catch (IOException e) {
	  e.printStackTrace();
	}
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
