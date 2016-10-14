/*   
	 A simple class which sets up a liveviz ccs connection, and handles replies

 */
package charm.lvClient;

import charm.ccs.*;
import charm.util.*;
import java.util.*;
import java.io.*;
import java.net.UnknownHostException;


class CcsAcceptor {
  
  CcsThread ccsT;  // use one ccs connection for both python and liveviz requests
  int outstandingRequests;


  public CcsAcceptor(String servername, String port){	
	ccsT = new CcsThread(new CcsLabel(), servername, Integer.parseInt(port), false, 0);
	outstandingRequests=0;
  }


  // send 'num' liveViz requests. These will be queued up on the server
  // and whenever the server is ready, it will reply to one from the queue
  public void readyLiveViz(int num){
	for(int i=0;i<num;i++){
	  System.out.println("creating a liveViz request\n");
	  CcsImageRequest r = new CcsImageRequest();
	  ccsT.addRequest(r);
	  outstandingRequests++;
	}
  }
 	

  // Send the python string 'pycommand' to server
  public void DoPythonReq(String pycommand){
	  PythonExecute pe=new PythonExecute(pycommand, false, true, 0);
	  ccsT.addRequest(new ExecutePythonCode(pe.pack()));
	  outstandingRequests++;
 }


  public void finish(){
	// don't finish the connection when using poll mode, 
	// since there may still be images coming back to you
	//	ccsT.finish();
  }


  private class CcsImageRequest extends CcsThread.request {
	int wid,ht;
	public CcsImageRequest() {
	  super("lvImage",null);
	  wid=400;
	  ht=400;
	  ByteArrayOutputStream bs=new ByteArrayOutputStream();
	  DataOutputStream os=new DataOutputStream(bs);
	  //System.out.println("Sending request for ("+wid+"x"+ht+")");
	  try {
		os.writeInt(1); /*Client version*/
		os.writeInt(1); /*Request type*/
		os.writeInt(wid); os.writeInt(ht);
	  } catch(IOException e) {e.printStackTrace();}
	  setData(bs.toByteArray());
	}
	public void handleReply(byte[] data) {
	  System.out.println("Received a LiveViz image\n");
	  outstandingRequests--;
	}
  }

  
  // This inner class can handle the reply which will be sent back
  private class ExecutePythonCode extends CcsThread.request {
	public ExecutePythonCode(byte[] s) {super("pycode", s); }
	public void handleReply(byte[] data) {
	  int interpreterHandle = CcsServer.readInt(data, 0);
	  System.out.println("Python request was processed\n");
	  outstandingRequests--;
	}
  }


}
