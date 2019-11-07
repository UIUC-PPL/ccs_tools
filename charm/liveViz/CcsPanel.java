package charm.liveViz;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import charm.ccs.*;
import charm.util.*;

abstract class CcsPanel extends Panel {
  //// Private Member Variables ////////////
  private final CcsThread ccsThread;
  private boolean running;
  private TextField fpsField;
  private int fpsCap;
  private Timer timer;

  private Panel controlPanel;

  //// Public Member Functions ////////////
  public CcsPanel(CcsServer s) {
    setLayout(new BorderLayout());
    ccsThread = new CcsThread(s);

    running = false;
    fpsCap = 1;
    timer = new Timer();

    Label fpsLabel = new Label("FPS Cap:");
    fpsField = new TextField(""+fpsCap, 4);
    Button updateButton = new Button("Update");
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setFPSCap(Integer.parseInt(fpsField.getText()));
      }
    });

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

    controlPanel = new Panel();
    controlPanel.add(fpsLabel);
    controlPanel.add(fpsField);
    controlPanel.add(updateButton);
    controlPanel.add(pauseButton);
    controlPanel.add(resumeButton);

    add(controlPanel, BorderLayout.PAGE_END);
  }

  public void scheduleNextRequest() {
    if (running) {
      timer.schedule(new TimerTask() {
        public void run() { makeRequest(); }
      },
      1000 / fpsCap);
    }
  }

  public abstract void makeRequest();

  public void sendRequest(CcsThread.request r) {
    ccsThread.addRequest(r, true);
  }

  public void setFPSCap(int f) {
    fpsField.setText(""+f);
    fpsCap = f;
  }

  public void start() { resume(); }

  public void pause() { running = false; }

  public void resume() {
    if (running == false) {
      running = true;
      scheduleNextRequest();
    }
  }

  public void stop() {
    running = false;
    ccsThread.finish();
  }
}
