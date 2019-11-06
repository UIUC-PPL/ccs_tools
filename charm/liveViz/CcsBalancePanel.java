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
  boolean running;

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

    running = true;
    fps = 1;
    balancePanel = new LiveBalancePanel();
    timer = new Timer();
    scheduleNext();

    TextField fpsField = new TextField("1", 3);
    fpsField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        fps = Integer.parseInt(fpsField.getText());
      }
    });

    add(balancePanel, BorderLayout.CENTER);

    Button pauseButton = new Button("Pause");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pause();
      }
    });

    Button resumeButton = new Button("Resume");
    resumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resume();
      }
    });

    Panel controlPanel = new Panel();
    controlPanel.add(fpsField);
    controlPanel.add(pauseButton);
    controlPanel.add(resumeButton);
    add(controlPanel, BorderLayout.PAGE_END);
  }

  public void setBalanceData(int[] data) {
    balancePanel.setData(data);
    scheduleNext();
  }

  public void scheduleNext() {
    if (running) {
      timer.schedule(new BalanceRequestTask(), 1000 / fps);
    }
  }

  public void pause() { running = false; }

  public void resume() {
    if (running == false) {
      running = true;
      scheduleNext();
    }
  }

  public void stop() {
    running = false;
    ccsThread.finish();
  }
}
