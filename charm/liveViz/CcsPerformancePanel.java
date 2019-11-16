package charm.liveViz;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import charm.ccs.*;
import charm.util.*;

class CcsPerformancePanel extends CcsPanel {
  //// Private Member Variables ////////////
  private PerformancePanel performancePanel;

  private class CcsStatRequest extends CcsThread.request {
    public CcsStatRequest() { super("lvStatRequest", 0); }
    public void handleReply(byte[] loadData) {
      IntBuffer ib = ByteBuffer.wrap(loadData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
      if (ib.capacity() > 0) {
        int[] data = new int[ib.capacity()];
        ib.get(data);
        addStatData(data);
      } else {
        scheduleNextRequest();
      }
    }
  }

  //// Public Member Functions ////////////
  public CcsPerformancePanel(CcsServer s) {
    super(s);
    setMinimumSize(new Dimension(200,200));
    performancePanel = new PerformancePanel();

    add(performancePanel, BorderLayout.CENTER);
    setName("PerformanceDataRequestThread");
    setFPSCap(5);
    start();
  }

  public void makeRequest() {
    sendRequest(new CcsStatRequest());
  }

  public void addStatData(int[] data) {
    performancePanel.addData(data);
    scheduleNextRequest();
  }
}
