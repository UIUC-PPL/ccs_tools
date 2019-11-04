package charm.liveViz;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import charm.ccs.*;
import charm.util.*;

class CcsBalancePanel extends Panel {
  //// Private Member Variables ////////////
  private final CcsThread ccsThread;
  private int fps;
  private Timer timer;
  private LiveBalancePanel balancePanel;

  private class BalanceRequestTask extends TimerTask {
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

    public void run() {
      ccsThread.addRequest(new CcsBalanceRequest(), true);
    }
  }

  //// Public Member Functions ////////////
  public CcsBalancePanel(CcsServer s) {
    setLayout(new BorderLayout());
    ccsThread = new CcsThread(s);

    fps = 1;
    balancePanel = new LiveBalancePanel();
    timer = new Timer();
    timer.schedule(new BalanceRequestTask(), 1000 / fps);

    TextField fpsField = new TextField("1");
    fpsField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        fps = Integer.parseInt(fpsField.getText());
      }
    });

    add(balancePanel, BorderLayout.CENTER);
    add(fpsField, BorderLayout.PAGE_END);
  }

  public void setBalanceData(int[] data) {
    balancePanel.setData(data);
    timer.schedule(new BalanceRequestTask(), 1000 / fps);
  }

  public void stop() {
    ccsThread.finish();
  }
}
