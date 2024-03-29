package charm.debug;
import charm.ccs.CcsServer;
import charm.debug.fmt.*;

import java.io.IOException;
import java.util.SortedSet;
import java.util.Iterator;

import javax.swing.DebugGraphics;
import javax.swing.SwingUtilities;

/**
   Utilities for getting and setting CpdList values.
*/
public class CpdUtil {
    CpdUtil(CcsServer ccs_) {ccs=ccs_;}
    private CcsServer ccs;
  
    public void abort(String problem) {
    	System.out.println(problem);
    	//System.exit(1);
    	Runnable doWorkRunnable = new Runnable() {
			public void run() { ParDebug.debugger.quitProgram(); }
		};
		SwingUtilities.invokeLater(doWorkRunnable);
    }
   
    /// Return the number of items in this cpdList on this processor.
    public int getListLength(String listName,int forPE)
    {
	try {
	    //Build a byte array describing the ccs request:
	    int reqStr=listName.length();
	    int reqLen=4+reqStr+1;
	    // System.out.println("getListLength "+listName+" for PE "+forPE);
	    byte[] req=new byte[reqLen];
	    CcsServer.writeInt(req,0,reqStr);
	    CcsServer.writeString(req,4,reqStr+1,listName);
	    CcsServer.Request r=ccs.sendRequest("ccs_list_len",forPE,req);
 
	    //Get the response and take it apart:
	    byte[] resp=ccs.recvResponse(r);
	    if (resp.length<4) return -1;
	    return CcsServer.readInt(resp,0);
	} catch (IOException e) {
	    e.printStackTrace();
	    abort("Network error connecting to PE "+forPE+" to access list "+listName);
	    return 0;
	}
    }

    /// Returns a set of CpdList items as a byte array
    public byte[] byteList(String listName,String fmt,int forPE,int lo,int hiPlusOne,byte[] extra)
    {
	try {
	    //Build a byte array describing the ccs request:
	    int reqStr=listName.length();
	    int extraLen = extra!=null ? extra.length : 0;
	    int reqLen=4+4+4+extraLen+4+reqStr+1;
	    byte[] req=new byte[reqLen];
	    System.out.println("byteList: "+lo+" "+hiPlusOne+" "+extraLen);
	    for (int i=0; i<extraLen; ++i) System.out.print(extra[i]);
	    System.out.println("");
	    CcsServer.writeInt(req,0,lo);
	    CcsServer.writeInt(req,4,hiPlusOne);
	    CcsServer.writeInt(req,8,extraLen);
	    if (extraLen>0) CcsServer.writeBytes(req,12,extraLen,extra);
	    CcsServer.writeInt(req,12+extraLen,reqStr);
	    CcsServer.writeString(req,16+extraLen,reqStr+1,listName);
	    CcsServer.Request r=ccs.sendRequest("ccs_list_items."+fmt,forPE,req);
      
	    byte[] answer = ccs.recvResponse(r);
	    //System.out.println("Size of received data: "+answer.length);
	    return answer;
	} catch (IOException e) {
	    e.printStackTrace();
	    abort("Network error connecting to PE "+forPE+" to access list "+listName);
	    return null;
	}
    }
    
    /**
       Return these CpdList items as a string.
    */
    public String stringList(String listName,int forPE,int lo,int hiPlusOne,byte[] extra)
    {
	return new String(byteList(listName,"txt",forPE,lo,hiPlusOne,extra));
    }
  
    /**
       Return these CpdList items as a string, without any additional extra data required.
    */
    public String stringList(String listName,int forPE,int lo,int hiPlusOne)
    {
	return stringList(listName,forPE,lo,hiPlusOne,null);
    }

    /**
       Return this whole CpdList as a string.
    */
    public String stringList(String listName,int forPE,byte[] extra)
    {
	return stringList(listName,forPE,0,getListLength(listName,forPE),extra);
    }

    /**
       Return this whole CpdList as a string, without any additional extra data required.
    */
    public String stringList(String listName,int forPE)
    {
	return stringList(listName,forPE,null);
    }

    /// Return a set of CpdList items as a PList
    public PList getPList(String listName,int forPE,int lo,int hiPlusOne,byte[] extra) {
	byte[] buf=byteList(listName,"fmt",forPE,lo,hiPlusOne,extra);
	PConsumer cons=new PConsumer();
	cons.decode(buf);
	return cons.getList();
    }

    /// Return a set of CpdList items as a PList withou any additional extra data required
    public PList getPList(String listName,int forPE,int lo,int hiPlusOne) {
	return getPList(listName,forPE,lo,hiPlusOne,null);
    }
  
    /// Return everything in a CpdList as a PList
    public PList getPList(String listName,int forPE,byte[] extra) {
	return getPList(listName,forPE,0,getListLength(listName,forPE),extra);
    }
  
    /// Return everything in a CpdList as a PList without any additional extra data required
    public PList getPList(String listName,int forPE) {
	return getPList(listName,forPE,null);
    }
  
    /**
       Set this entry of this CpdList to these bytes.
    */
    public void setListItem(String listName,int forPE,int listIdx,byte[] to)
    {
	try {
	    int reqStr=listName.length();
	    int reqLen=4+4+4+to.length+4+reqStr+1;
	    byte[] req=new byte[reqLen];
	    int o=0;
	    CcsServer.writeInt(req,o,listIdx); o+=4;
	    CcsServer.writeInt(req,o,listIdx+1); o+=4;
	    CcsServer.writeInt(req,o,to.length); o+=4;
	    for (int i=0;i<to.length;i++) req[o++]=to[i];
	    CcsServer.writeInt(req,o,reqStr); o+=4;
	    CcsServer.writeString(req,o,reqStr+1,listName); o+=reqStr+1;
	    if (o!=reqLen) abort("Size mismatch during CpdList set");
	    CcsServer.Request r=ccs.sendRequest("ccs_list_items.set",forPE,req);
	    ccs.recvResponse(r);
	} catch (IOException e) {
	    e.printStackTrace();
	    abort("Network error connecting to PE "+forPE+" to write list "+listName);
	}
    }
  
    //Sends a request to the ccs server
    public String sendCcsRequest(String ccsHandlerName, String parameterName, int destPE) {
    	byte[] reply = sendCcsRequestBytes(ccsHandlerName,parameterName,destPE);
    	if (reply != null) return new String(reply);
    	else return null;
    }
    //Sends a request to the ccs server and return the answer as bytes
    public byte[] sendCcsRequestBytes(String ccsHandlerName, String parameterName, int destPE) {
		//Build a byte array describing the ccs request:
		int reqStr=parameterName.length();
		int reqLen = reqStr+1;
		byte[] req=new byte[reqLen];
		CcsServer.writeString(req,0,reqStr+1,parameterName);
		boolean waiting = isWaitForReply(ccsHandlerName, parameterName);
		return sendCcsRequestBytes(ccsHandlerName, req, destPE, waiting);
    }
    public byte[] sendCcsRequestBytes(String ccsHandlerName, byte[] req, int destPE, boolean waitForReply) {
    	try {
    		CcsServer.Request r=ccs.sendRequest(ccsHandlerName,destPE,req);
    		if (waitForReply)
    		{
    			byte[] resp=ccs.recvResponse(r);
    			return resp;
    		}
    		else {
    			return null;
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    		abort("Network error connecting to PE "+destPE+" to perform "+ccsHandlerName);
    		return null;
    	}
    }

    //if parameter forSelectedPes <= 0, ccs message sent to all pes
    /** @deprecated This function does a broadcast by sending a request to every
     * processor individually, which is bad. It is still in use by older functions
     * that do not support yet the CCS broadcast mechanism.  */
    public void bcastCcsRequest(String ccsHandlerName, String parameterName, int forSelectedPes, int numberPes, boolean[] peList)
    {
	if (forSelectedPes <= 0)
	    { /* Send to all pes */
                for (int indexPE=0; indexPE < numberPes; indexPE++) {
                    sendCcsRequest(ccsHandlerName, parameterName, indexPE);
		}
	    }
	else
	    { /* Send to selected subset of PEs */
                for (int indexPE=0; indexPE < numberPes; indexPE++)
		    if (peList[indexPE] == true) {
			sendCcsRequest(ccsHandlerName, parameterName, indexPE);
		    }
	    }
    }
    
    /** @deprecated This function does a broadcast by sending a request to every
     * processor individually, which is bad. It is still in use by older functions
     * that do not support yet the CCS broadcast mechanism.  */
    public void bcastCcsRequest(String ccsHandlerName, String parameterName, Iterator peList) {
    	while (peList.hasNext()) {
    		sendCcsRequest(ccsHandlerName, parameterName, ((Processor)peList.next()).getId());
    	}
    }
    
    public boolean isWaitForReply(String ccsHandlerName, String parameterName) {
    	return ! (ccsHandlerName.equalsIgnoreCase("debug/converse/freeze") ||
				ccsHandlerName.equalsIgnoreCase("debug/converse/quit") ||
				ccsHandlerName.equalsIgnoreCase("debug/charm/continue") ||
				ccsHandlerName.equalsIgnoreCase("debug/charm/deliverall") ||
				ccsHandlerName.equalsIgnoreCase("debug/charm/next"));
    }
    
    public byte[] bcastCcsRequest(String ccsHandlerName, String parameterName, int []peList) {
    	if (peList.length == ParDebug.debugger.getNumPes()) return bcastCcsRequest(ccsHandlerName, parameterName);
    	boolean waitForReply = isWaitForReply(ccsHandlerName, parameterName);
		int reqStr=parameterName.length();
		int reqLen = reqStr+1;
		byte[] req=new byte[reqLen];
		CcsServer.writeString(req,0,reqStr+1,parameterName);
    	try {
    		System.out.print("Requesting "+ccsHandlerName+" to:");
    		for (int i=0; i<peList.length; ++i) System.out.print(" "+peList[i]);
    		System.out.println();
    		CcsServer.Request r=ccs.sendRequest(ccsHandlerName,peList,req);
    		if (waitForReply)
    		{
    			byte[] resp=ccs.recvResponse(r);
    			return resp;

    		}
    		else {
    			return null;
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    		abort("Network error connecting to application to perform "+ccsHandlerName);
    		return null;
    	}
    }
    
    /** @deprecated This function does a broadcast by sending a request to every
     * processor individually, which is bad. It is still in use by older functions
     * that do not support yet the CCS broadcast mechanism. */
    public byte[][] bcastCcsRequest(String ccsHandlerName, String parameterName, int npes) {
    	byte[][] ret = new byte[npes][];
    	for (int pe=0; pe<npes; ++pe) {
    		ret[pe] = sendCcsRequestBytes(ccsHandlerName, parameterName, pe);
    	}
    	return ret;
    }
    
    /* This method uses the newer CCS broadcast mechanism to communicate at once with the whole application */
    public byte[] bcastCcsRequest(String ccsHandlerName, String parameterName) {
    	return sendCcsRequestBytes(ccsHandlerName, parameterName, -1);
    }
};
