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
import java.net.UnknownHostException;
import charm.ccs.*;
import charm.util.*;

class CcsImagePanel extends MemImagePanel {
  //// Private Member Variables ////////////
  private final CcsThread ccs;
  private final Toolbar tools;
  private Controller3d cntl;
  private Config config;

  private boolean imageRequested = false;
  private int lastWidth = 200, lastHeight = 150;
  private int redrawCount = 0;
  private volatile ByteImage curImg;

  //// Private Classes ////////////
  private class progressToLabel implements charm.ccs.CcsProgress {
    private Label dest;
    public progressToLabel(Label dest_) {dest = dest_;}
    public void setText(String s) { dest.setText(s); }
  }

  private static class ByteImage {
    public int width, height;
    public byte[] data;
    public boolean isColor;
    public ByteImage(byte[] d, int w, int h, boolean c) {
      data = d;
      width = w;
      height = h;
      isColor = c;
    }
  }

  private class CcsConfigRequest extends CcsThread.request {
    CcsImagePanel dest;
    MainPanel caller;
    public CcsConfigRequest(CcsImagePanel dest_, MainPanel caller_) {
      super("lvConfig", 0);
      dest = dest_;
      caller = caller_;
    }

    public void handleReply(byte[] configData) {
      try {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(configData));
        setConfig(new Config(is));
        caller.createGrid();
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  private class CcsImageRequest extends CcsThread.request {
    int width, height;
    CcsImagePanel dest;
    public CcsImageRequest(CcsImagePanel dest_, Controller3d.ImageRequest3d r) {
      super("lvImage", null);
      dest = dest_;
      width = lastWidth;
      height = lastHeight;
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream os = new DataOutputStream(bs);
      try {
        os.writeInt(1); /*Client version*/
        os.writeInt(1); /*Request type*/
        os.writeInt(width);
        os.writeInt(height);
        if (r != null) {
          r.x.write(os);
          r.y.write(os);
          r.z.write(os);
          r.o.write(os);
          os.writeDouble(r.minZ);
          os.writeDouble(r.maxZ);
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
      setData(bs.toByteArray());
    }

    public void handleReply(byte[] data) {
      dest.gotImage(new ByteImage(data, width, height, config.isColor));
    }
  }

  //// Private Member Functions ////////////
  private void debug(String state) {
    // System.out.println("liveViz client> "+state);
  }

  //// Public Member Functions ////////////
  public CcsImagePanel(MainPanel caller_, Label status_, Toolbar tools_,
      String server, int port, boolean isTimeoutSet, int timeoutPeriod) {
    cntl = null;
    config = null;
    tools = tools_;
    ccs = new CcsThread(new progressToLabel(status_), server,port, isTimeoutSet, timeoutPeriod);
    ccs.addRequest(new CcsConfigRequest(this, caller_));
  }

  public void stop() {
    ccs.finish();
  }

  public void setConfig(Config c) {
    config = c;
    if (config.is3d) {
      System.out.println("Box size: " + config.min + " to " + config.max);
      cntl = new Controller3d(this, tools, config.min, config.max, lastWidth, lastHeight);
    }
    if (!imageRequested) {
      debug("Sending request from gotConfig");
      requestImage();
    }
    System.out.println("Config values: color = " + config.isColor +
        " push = " + config.isPush + " 3d = " + config.is3d + "\n");
  }

  public boolean is3d() {
    return config.is3d;
  }

  public void requestImage() {
    imageRequested = true;
    Controller3d.ImageRequest3d r = null;
    if (cntl != null) {
      r = cntl.get3dRequest();
    }
    ccs.addRequest(new CcsImageRequest(this, r), true);
  }

  public void gotImage(ByteImage newImg) {
    curImg = newImg;
    debug("gotImage");
    imageRequested = false;
    repaint(1); //Redraw the onscreen image
  }

  /******** Redraw ***********/
  public void paint(Graphics g) {
    super.paint(g);
    if (cntl != null) {
      cntl.paint(g, getSize());
    }
  }

  // Fill this pixel buffer with the last stored image
  public void fillBuffer(int dest[], int destWidth, int destHeight) {
    lastWidth = destWidth;
    lastHeight = destHeight;
    if (cntl != null) {
      cntl.updateSize(destWidth, destHeight);
    }
    ByteImage image = curImg;
    // No image delivered yet
    if (image == null) {
      return;
    }
    byte[] src = image.data;
    int drawWidth = Math.min(image.width, destWidth);
    int drawHeight = Math.min(image.height, destHeight);
    debug("redraw");
    for (int y = 0; y < drawHeight; y++) {
      if (image.isColor) { // We have a color image (rgb)
        int srcIdx = y * image.width * 3; // Byte index multiplied by 3 for rgb
        int destIdx = y * destWidth;
        for (int x = 0; x < drawWidth; x++) {
          int a = 255;
          int r = ((int)src[srcIdx + x * 3 + 0]);
          int g = ((int)src[srcIdx + x * 3 + 1]);
          int b = ((int)src[srcIdx + x * 3 + 2]);
          dest[destIdx + x] = packPixel(a, r, g, b);
        }
      } else { // We have a black-and-white image (just a single val per pixel)
        int srcIdx = y * image.width;
        int destIdx = y * destWidth;
        for (int x = 0; x < drawWidth; x++) {
          int a = 255;
          int v = ((int)src[srcIdx + x + 0]);
          dest[destIdx + x] = packPixel(a, v);
        }
      }
    }

    if (!imageRequested) {
      if (destWidth != image.width || destHeight != image.height || config.isPush) {
        debug("Sending request from fillBuffer");
        requestImage();
      }
    }
  }
}
