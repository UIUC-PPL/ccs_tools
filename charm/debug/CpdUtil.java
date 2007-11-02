package charm.debug;
import charm.ccs.CcsServer;
import charm.debug.fmt.*;
import java.io.IOException;

/**
   Utilities for getting and setting CpdList values.
*/
public class CpdUtil {
    CpdUtil(CcsServer ccs_) {ccs=ccs_;}
    private CcsServer ccs;
  
    public void abort(String problem) {
	System.out.println(problem);
	System.exit(1);
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
    public String sendCcsRequest(String ccsHandlerName, String parameterName, int destPE) 
    {
    	byte[] reply = sendCcsRequestBytes(ccsHandlerName,parameterName,destPE);
    	if (reply != null) return new String(reply);
    	else return null;
    }
    //Sends a request to the ccs server and return the answer as bytes
    public byte[] sendCcsRequestBytes(String ccsHandlerName, String parameterName, int destPE) 
    {
	try {
	    //Build a byte array describing the ccs request:
	    int reqStr=parameterName.length();
	    int reqLen = reqStr+1;
	    byte[] req=new byte[reqLen];
	    CcsServer.writeString(req,0,reqStr+1,parameterName);
	    CcsServer.Request r=ccs.sendRequest(ccsHandlerName,destPE,req);
	    if ( parameterName.equalsIgnoreCase("freeze") || ccsHandlerName.equalsIgnoreCase("ccs_debug_quit") || ccsHandlerName.equalsIgnoreCase("ccs_remove_all_break_points") || ccsHandlerName.equalsIgnoreCase("ccs_set_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_remove_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_continue_break_point"))
		{
		    return null;
		}
	    else {
		byte[] resp=ccs.recvResponse(r);
		return resp;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    abort("Network error connecting to PE "+destPE+" to perform "+ccsHandlerName);
	    return null;
	}
    }

    //if parameter forSelectedPes <= 0, ccs message sent to all pes
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
};
