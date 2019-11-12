/*
A panel that displays an image aquired via CCS & liveViz.

by Orion Lawlor,  6/14/2001
olawlor@acm.org
*/
package charm.liveViz;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.net.UnknownHostException;
import charm.ccs.*;
import charm.util.*;

class CcsImagePanel2D extends CcsPanel {
  //// Private Member Variables ////////////
  private MemImagePanel imagePanel;
  private Config config;

  public class CcsImageRequest extends CcsThread.request {
    int width, height;
    public CcsImageRequest() {
      super("lvImage", null);
      width = imagePanel.getWidth();
      height = imagePanel.getHeight();
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream os = new DataOutputStream(bs);
      try {
        os.writeInt(1);
        os.writeInt(1);
        os.writeInt(width);
        os.writeInt(height);
      } catch(IOException e) {
        e.printStackTrace();
      }
      setData(bs.toByteArray());
    }

    public void handleReply(byte[] data) {
      setImageData(data, width, height, config.isColor);
    }
  }

  //// Public Member Functions ////////////
  public CcsImagePanel2D(CcsServer s, Config c) {
    super(s);
    setMinimumSize(new Dimension(200,200));
    config = c;
    imagePanel = new MemImagePanel();
    add(imagePanel, BorderLayout.CENTER);
    setFPSCap(30);
    start();
  }

  public void makeRequest() {
    sendRequest(new CcsImageRequest());
  }

  public void setImageData(byte[] data, int w, int h, boolean isColor) {
    imagePanel.setImageData(data, w, h, isColor);
    scheduleNextRequest();
  }
}
