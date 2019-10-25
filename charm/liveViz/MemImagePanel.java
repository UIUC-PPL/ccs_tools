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

  // Fill this pixel buffer
  public void fillBuffer(int dest[], int w, int h) {
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        // Fill the buffer with black pixels
        dest[y*w+x] = packPixel(255, 0);
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
