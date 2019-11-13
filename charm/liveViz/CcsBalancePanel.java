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

class CcsBalancePanel extends CcsPanel {
  //// Private Member Variables ////////////
  private LiveBalancePanel balancePanel;
  private CcsThread interactiveThread;

  private class CcsBalanceRequest extends CcsThread.request {
    public CcsBalanceRequest() { super("lvBalanceData", 0); }
    public void handleReply(byte[] loadData) {
      IntBuffer ib = ByteBuffer.wrap(loadData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
      int[] data = new int[ib.capacity()];
      ib.get(data);
      setBalanceData(data);
    }
  }

  private class CcsDoBalanceRequest extends CcsThread.request {
    public CcsDoBalanceRequest() { super("lvBalanceInteraction", 0); }
    public void handleReply(byte[] loadData) {}
  }

  //// Public Member Functions ////////////
  public CcsBalancePanel(CcsServer s) {
    super(s);
    setMinimumSize(new Dimension(200,200));
    balancePanel = new LiveBalancePanel();

    Button rotateButton = new Button("Rotate");
    rotateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        balancePanel.rotate();
      }
    });

    Checkbox showChares = new Checkbox("Show Chare Loads");
    Checkbox showBG = new Checkbox("Show BG Load");
    Checkbox showIdle = new Checkbox("Show Idle");
    Checkbox sortByLoad = new Checkbox("Sort By Load");

    showChares.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean on = ((Checkbox)e.getSource()).getState();
        if (on) {
          balancePanel.showChares();
        } else {
          balancePanel.hideChares();
        }
      }
    });

    showBG.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean on = ((Checkbox)e.getSource()).getState();
        if (on) {
          balancePanel.showBG();
        } else {
          balancePanel.hideBG();
        }
      }
    });

    showIdle.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean on = ((Checkbox)e.getSource()).getState();
        if (on) {
          balancePanel.showIdle();
        } else {
          balancePanel.hideIdle();
        }
      }
    });

    sortByLoad.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean on = ((Checkbox)e.getSource()).getState();
        if (on) {
          balancePanel.setSortOrder(LiveBalancePanel.SortOrder.BY_LOAD);
        } else {
          balancePanel.setSortOrder(LiveBalancePanel.SortOrder.BY_PE);
        }
      }
    });

    interactiveThread = new CcsThread(s);
    Button balanceButton = new Button("Balance");
    balanceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        interactiveThread.addRequest(new CcsDoBalanceRequest());
      }
    });

    addToControlPanel(rotateButton);
    addToControlPanel(showChares);
    addToControlPanel(showBG);
    addToControlPanel(showIdle);
    addToControlPanel(sortByLoad);
    addToControlPanel(balanceButton);

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
