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

class CcsImagePanel2D extends Panel {
  //// Private Member Variables ////////////
  private final CcsThread ccsThread;
  private int fps;
  private Timer timer;
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

  private class ImageRequestTask extends TimerTask {
    public void run() {
      ccsThread.addRequest(new CcsImageRequest(), true);
    }
  }

  //// Public Member Functions ////////////
  public CcsImagePanel2D(CcsServer s, Config c) {
    setLayout(new BorderLayout());
    ccsThread = new CcsThread(s);
    config = c;

    fps = 30;
    imagePanel = new MemImagePanel();
    timer = new Timer();
    timer.schedule(new ImageRequestTask(), 1000 / fps);

    TextField fpsField = new TextField("30");
    fpsField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent e) {
        fps = Integer.parseInt(fpsField.getText());
      }
    });

    add(imagePanel, BorderLayout.CENTER);
    add(fpsField, BorderLayout.PAGE_END);
  }

  public void setImageData(byte[] data, int w, int h, boolean isColor) {
    imagePanel.setImageData(data, w, h, isColor);
    timer.schedule(new ImageRequestTask(), 1000 / fps);
  }

  public void stop() {
    ccsThread.finish();
  }
}
