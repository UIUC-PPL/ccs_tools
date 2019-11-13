/*
  Top-level controller-- creates toolbar,
  status bar, and main panel.
*/
package charm.liveViz;

import charm.util.Toolbar;
import java.applet.Applet;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.JSplitPane;
import charm.util.*;
import charm.ccs.*;

public class MainPanel extends Panel {
  private Label status;
  private Toolbar tools;
  private Config config;
  private CcsServer server;
  private CcsThread ccsThread;

  private ConnectionPanel connectionPanel;
  private JSplitPane splitPane;
  private CcsImagePanel2D imagePanel;
  private CcsBalancePanel balancePanel;

  private class CcsConfigRequest extends CcsThread.request {
    public CcsConfigRequest() {
      super("lvConfig", 0);
    }

    public void handleReply(byte[] configData) {
      try {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(configData));
        setConfig(new Config(is));
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  public MainPanel(String server, String port, String isTimeoutSet_, String timeoutPeriod_) {
    setLayout(new GridBagLayout());

    tools = null;
    status = new Label();

    String connMachine=server;
    int connPort = Integer.parseInt(port);
    boolean isTimeoutSet = Boolean.parseBoolean(isTimeoutSet_);
    int timeoutPeriod = Integer.parseInt(timeoutPeriod_);

    // TODO: Move all this toolbar stuff to a separate 3D view panel
    // Build the toolbar descriptions
    String[] toolDesc={
      "Pan the image (drag around)",
      "Zoom the image (drag in or out)",
      "Rotate the image (drag around in 3D)",
      "Move the slicing plane (drag forward or back)",
      "Retrieve detailed information"
    };
    //Find the toolbar image (from .jar file)
    URL toolsImg = ClassLoader.getSystemResource("charm/liveViz/toolbar.jpg");
    if (toolsImg == null) { // can't find image-- try from web...
      try { toolsImg=new URL("http://charm.cs.uiuc.edu/2001/demo/astro1/toolbar.jpg"); }
      catch (Exception E) { }
    }
    tools = new Toolbar(toolsImg, 32, 32, 5, status, toolDesc);

    connectionPanel = new ConnectionPanel(this, server, port);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor=GridBagConstraints.NORTHEAST;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = 1; gbc.gridheight = 1;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0; gbc.weighty = 0.01;
    add(connectionPanel, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = 1; gbc.gridheight = 1;
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 1.0; gbc.weighty = 0.01;
    add(status, gbc);
    validate();
  }

  public void setCcsServer(CcsServer s) {
    stop();
    if (splitPane != null) {
      splitPane.remove(imagePanel);
      splitPane.remove(balancePanel);
      remove(splitPane);
    }
    server = s;
    ccsThread = new CcsThread(server);
    ccsThread.addRequest(new CcsConfigRequest());
  }

  public void setConfig(Config c) {
    ccsThread.finish();
    if (imagePanel != null) {
      remove(imagePanel);
    }
    config = c;
    imagePanel = new CcsImagePanel2D(server, config);
    balancePanel = new CcsBalancePanel(server);
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               imagePanel, balancePanel);
    splitPane.setResizeWeight(0.5);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridwidth = 1; gbc.gridheight = 1;
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1.0; gbc.weighty = 1.0;
    add(splitPane, gbc);

    validate();
  }
  
  public void stop() {
    if (imagePanel != null) imagePanel.stop();
    if (balancePanel != null) balancePanel.stop();
    if (ccsThread != null) ccsThread.stop();
    if (server != null) server.close();
  }
}
