/*
A non-blocking wrapper around CcsServer-- allows
asynchronous network communication easily.

by Orion Lawlor, olawlor@acm.org, 6/14/2001
*/
package charm.ccs;

import java.util.Stack;
import java.io.*;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

public class CcsThread implements Runnable {
  private boolean isBad; // Records that an error occured
  private Stack requests; // Keeps track of CcsRequests
  private volatile boolean keepGoing; // To signal exit
  private CcsServer ccs;
  private boolean isTimeoutSet;
  private int timeoutPeriod;

  /// Place to receive status information during communication

  private CcsProgress status;//Place to show status info.
  private Thread myThread;

  //Initialization just stashes info--
  // real work starts when thread begins running.
  private String hostName;
  private int port;

  private void ioError(Exception e, String what) {
    isBad = true;
    keepGoing = false;
    status.setText(what+" ("+hostName+":"+port+")");
    System.out.println("ERROR> "+what);
    System.out.println("Traceback: "+e);
    e.printStackTrace();
  }

  // Thin wrapper around a block of data
  public static class message {
    private byte[] data;
    public message(byte[] data_) {
      data=data_;
    }
    public message(int nBytes) {
      data=new byte[nBytes];
    }
    public byte[] getData() {return data;}
    protected void setData(byte[] data_) {data=data_;}
  }

  // Represents an outgoing CCS request
  public static class request extends message {
    int onPE;
    String handler;
    public request(String handler_,byte[] data_) {
      super(data_); handler=handler_;
    }
    public request(String handler_,int nBytes) {
      super(nBytes); handler=handler_;
    }
          public void setPE(int pe) {onPE=pe;}
    public int getPE() {return onPE;}
    public String getHandler() {return handler;}
    public void handleReply(byte[] data) {
      if (data.length!=0)
        System.out.println("Overload handleReply to actually use these "+data.length+" bytes of reply!");
    }
  }


  public CcsThread(CcsProgress status_, String hostName_, int port_) {
    requests = new Stack();
    status = status_;
    hostName = hostName_;
    port = port_;
    isBad = false;
    keepGoing = true;
    isTimeoutSet = false;
    timeoutPeriod = 0;
    // Start our run method
    myThread = new Thread(this);
    myThread.start();
  }

  public CcsThread(CcsProgress status_, String hostName_, int port_, boolean isTimeoutSet_, int timeoutPeriod_) {
    requests = new Stack();
    status = status_;
    hostName = hostName_;
    port = port_;
    isBad = false;
    keepGoing = true;
    isTimeoutSet = isTimeoutSet_;
    timeoutPeriod = timeoutPeriod_;
    // Start our run method
    myThread = new Thread(this);
    myThread.start();
  }

  public void addRequest(request req) { addRequest(req, false); }
  public void addRequest(request req, boolean flushOld) {
    // Clean out all previous requests
    if (flushOld) {
      while (!requests.empty()) {
        requests.pop();
      }
    }
    requests.push(req);
  }

  public void finish() { keepGoing = false; }
  public boolean isInvalid() { return isBad; }

  public void run() {
    if (ccs == null) {
      System.out.println("Connecting to " + hostName + ":" + port + "...\n");
      status.setText("Connecting to " + hostName + ":" + port + "...");
      try {
        ccs = new CcsServer(hostName, port, isTimeoutSet, timeoutPeriod);
      }
      catch (UnknownHostException e) { ioError(e, "Bad host name"); }
      catch (IOException e) { ioError(e, "Could not connect"); }
    }

    if (!keepGoing) return;

    status.setText("Connected to " + hostName + " (" + ccs.getNumPes() + " processors)");
    while (keepGoing) {
      while (requests.empty() && keepGoing) {
        // Wait for another request
        try { // Give other threads a chance
          int sleepMs = 30;
          Thread.sleep(sleepMs);
        } catch (InterruptedException E) {}
      }
      if (!keepGoing) break;

      request curReq = (request)requests.pop();
      status.setText("Sending request " + curReq.getHandler());
      try {
        ccs.sendRequest(curReq.getHandler(), curReq.getPE(), curReq.getData());
      } catch(IOException e) {
        ioError(e, "Error sending request");
        break;
      }

      byte[] reply;
      try {
        reply = ccs.recvResponse();
      } catch(SocketTimeoutException e1) {
        if (isTimeoutSet) {
          System.out.println("timeout!");
          requests.push(curReq);
          continue;
        } else {
          break;
        }
      } catch(IOException e) {
        ioError(e, "Error receiving response");
        break;
      }

      curReq.handleReply(reply);

      status.setText("");
    }
  }
}
