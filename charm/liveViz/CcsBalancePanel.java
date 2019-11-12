package charm.liveViz;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import charm.ccs.*;
import charm.util.*;

class CcsBalancePanel extends CcsPanel {
  //// Private Member Variables ////////////
  private LiveBalancePanel balancePanel;

  private class CcsBalanceRequest extends CcsThread.request {
    public CcsBalanceRequest() {
      super("lvBalance", 0);
    }

    public void handleReply(byte[] loadData) {
      IntBuffer ib = ByteBuffer.wrap(loadData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
      int[] data = new int[ib.capacity()];
      ib.get(data);
      setBalanceData(data);
    }
  }

  //// Public Member Functions ////////////
  public CcsBalancePanel(CcsServer s) {
    super(s);
    setMinimumSize(new Dimension(200,200));
    balancePanel = new LiveBalancePanel();
    add(balancePanel, BorderLayout.CENTER);
    setFPSCap(5);
    start();
  }

  public void makeRequest() {
    sendRequest(new CcsBalanceRequest());
  }

  public void setBalanceData(int[] data) {
    balancePanel.setData(data);
    scheduleNextRequest();
  }
}
