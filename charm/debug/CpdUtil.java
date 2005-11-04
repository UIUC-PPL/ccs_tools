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
  public byte[] byteList(String listName,String fmt,int forPE,int lo,int hiPlusOne)
  {
    try {
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
  public String stringList(String listName,int forPE,int lo,int hiPlusOne)
  {
    return new String(byteList(listName,"txt",forPE,lo,hiPlusOne));
  }
  
  /**
    Return this whole CpdList as a string.
  */
  public String stringList(String listName,int forPE)
  {
    return stringList(listName,forPE,0,getListLength(listName,forPE));
  }
  
  /// Return a set of CpdList items as a PList
  public PList getPList(String listName,int forPE,int lo,int hiPlusOne) {
      byte[] buf=byteList(listName,"fmt",forPE,lo,hiPlusOne);
      PConsumer cons=new PConsumer();
      cons.decode(buf);
      return cons.getList();
  }
  /// Return everything in a CpdList as a PList
  public PList getPList(String listName,int forPE) {
      return getPList(listName,forPE,0,getListLength(listName,forPE));
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
  
};
