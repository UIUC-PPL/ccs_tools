/*
A panel that displays a memory image.

by Orion Lawlor,  6/14/2001
olawlor@acm.org
*/
package charm.liveViz;

import java.awt.*;
import java.awt.image.*;

class MemImagePanel extends Panel {
  // Cache the pixel buffer to improve garbage collection
  private int buffer[] = null;
  private int cachedWidth = -1;
  private int cachedHeight = -1;

  public static class ByteImage {
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
  private volatile ByteImage currImg;

  private void createBuffer(int w, int h) {
    cachedWidth = w;
    cachedHeight = h;
    buffer = new int[w*h];
  }

  private void drawBuffer(Graphics g) {
    int w = getSize().width;
    int h = getSize().height;
    if (cachedWidth != w || cachedHeight != h) {
      createBuffer(w,h);
    }
    fillBuffer(buffer, w, h);
    Image img = createImage(new MemoryImageSource(w, h, buffer, 0, w));
    g.drawImage(img, 0, 0, null);
  }

  // Private member functions for packing ints into bytes for pixels
  // Full color pixel
  protected int packPixel(int a, int r, int g, int b) {
    return ((a & 0xff) << 24) + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
  }

  // Black and white pixel
  protected int packPixel(int a, int v) {
    return ((a & 0xff) << 24) + ((v & 0xff) << 16) + ((v & 0xff) << 8) + (v & 0xff);
  }

  public MemImagePanel() {}
  public Dimension getMinimumSize() { return new Dimension(150,100); }
  public Dimension getPreferredSize() { return new Dimension(500,300); }

  public void setImageData(byte[] data, int w, int h, boolean c) {
    currImg = new ByteImage(data, w, h, c);
    repaint(1);
  }

  // Fill this pixel buffer
  public void fillBuffer(int dest[], int destWidth, int destHeight) {
    ByteImage image = currImg;

    // No image delivered yet
    if (image == null) {
      return;
    }
    byte[] src = image.data;
    int drawWidth = Math.min(image.width, destWidth);
    int drawHeight = Math.min(image.height, destHeight);

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
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {
    drawBuffer(g);
  }
}
