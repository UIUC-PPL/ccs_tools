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
  private CcsThread ccsThread;
  private boolean running;
  private TextField fpsCapField;
  private int fpsCap;
  private Timer ccsTimer;
  private Timer fpsTimer;

  private Panel controlPanel;
  private Label fpsLabel;
  private int fpsCounter;

  private void updateFPS() {
    fpsLabel.setText("FPS: " + fpsCounter);
    fpsCounter = 0;
    fpsTimer.schedule(new TimerTask() {
      public void run() { updateFPS(); }
    },
    1000);
  }

  private void tick() {
    fpsCounter++;
  }

  //// Public Member Functions ////////////
  public CcsPanel() {
    setLayout(new BorderLayout());

    running = false;
    fpsCap = 1;
    fpsTimer = new Timer();

    fpsLabel = new Label("FPS: 000");
    Panel fpsCapPanel = new Panel();
    Label fpsCapLabel = new Label("FPS Cap:");
    fpsCapField = new TextField(""+fpsCap, 4);
    fpsCapPanel.add(fpsCapLabel);
    fpsCapPanel.add(fpsCapField);
    Button updateButton = new Button("Update");
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setFPSCap(Integer.parseInt(fpsCapField.getText()));
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
    controlPanel.setLayout(new GridLayout(0,5));
    controlPanel.add(fpsLabel);
    controlPanel.add(fpsCapPanel);
    controlPanel.add(updateButton);
    controlPanel.add(pauseButton);
    controlPanel.add(resumeButton);

    add(controlPanel, BorderLayout.PAGE_END);

    updateFPS();
  }

  public void setThread(CcsThread t) {
    stop();
    ccsThread = t;
    if (ccsTimer != null) {
      ccsTimer.cancel();
    }
    ccsTimer = new Timer();
    start();
  }

  public void addToControlPanel(Component c) {
    controlPanel.add(c);
  }

  public void setControlPanelVisibility(boolean v) {
    controlPanel.setVisible(v);
    validate();
  }

  public void hideControlPanel() {
    setControlPanelVisibility(false);
  }

  public void showControlPanel() {
    setControlPanelVisibility(true);
  }

  public void scheduleNextRequest() {
    if (running) {
      tick();
      ccsTimer.schedule(new TimerTask() {
        public void run() { makeRequest(); }
      },
      1000 / fpsCap);
    }
  }

  public abstract void makeRequest();

  public void sendRequest(CcsThread.request r) {
    synchronized (ccsThread) {
      ccsThread.addRequest(r, false);
    }
  }

  public void setFPSCap(int f) {
    fpsCapField.setText(""+f);
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
  }
}
