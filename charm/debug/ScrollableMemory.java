package charm.debug;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

import charm.debug.fmt.*;
import java.util.Collections;

/* ScrollableMemory.java is used by MemoryWindow.java to display the content of
 * the memory. */

public class ScrollableMemory extends JLabel
    implements Scrollable {

    private static final int HOLE_PIXELS = 50;

    private int maxUnitIncrement = 1;
    private boolean missingPicture = false;

    private int pe;
    private MemoryPList data;
    private int verticalPixels;
    private int horizontalPixels;
    private int lineScan;
    private int lineWidth;
    private int lineStart;
    private int numLines;
    private int pixelsAvailable;
    private int memorySize;
    private int firstByte;
    private int lastByte;
    private MemoryPList.Hole holes[];
    private Slot[] crossReference;

    public int viewX, viewY;

    public ScrollableMemory(MemoryPanel parent, int forPE, int scan, int lines, int horiz) {
        super();
	int m=1;

	pe = forPE;
	PList list=ParDebug.server.getPList("converse/memory",forPE);
	data = new MemoryPList(list);
	data.sort();

	firstByte = Integer.MAX_VALUE;
	lastByte = Integer.MIN_VALUE;
	for (int i=0; i<data.size(); ++i) {
	    if (data.size(i) == 0) continue;
	    if (data.elementAt(i, 0).getLocation() < firstByte)
		firstByte = data.elementAt(i, 0).getLocation();
	    if (data.elementAt(i, data.size(i)-1).getLocation() > lastByte)
		lastByte = data.elementAt(i, data.size(i)-1).getLocation() + data.elementAt(i, data.size(i)-1).getSize();
	}

	holes = data.findHoles();

	memorySize = lastByte - firstByte + 1 - holes[0].size;

	//System.out.println("data size = "+memorySize);
	resizeImage(scan, lines, horiz);

	setHorizontalAlignment(CENTER);
	setOpaque(true);
	setBackground(Color.black);

	//Let the user scroll by dragging to outside the window.
	setAutoscrolls(true); //enable synthetic drag events
    }

    public void resizeImage(int scan, int lines, int horiz) {
	lineScan = scan;
	lineWidth = (int)(scan*3/4);
	lineStart = (lineScan - lineWidth) / 2;
	numLines = lines;
	verticalPixels = numLines*lineScan;
	horizontalPixels = horiz;

	pixelsAvailable = verticalPixels / lineScan * horizontalPixels;
	crossReference = new Slot[pixelsAvailable];
	pixelsAvailable -= HOLE_PIXELS * holes[0].position;

	BufferedImage tmp = new BufferedImage(horizontalPixels,verticalPixels,BufferedImage.TYPE_INT_ARGB);

	int color = (255<<24) + (255<<16) + (0<<8) + 100;
	int rgbarray[] = new int[lineWidth];
	for (int i=0; i<lineWidth; ++i) rgbarray[i] = color;
	color = (255<<24) + (0<<16) + (255<<8) + 0;
	int rgbhole[] = new int[lineWidth];
	for (int i=0; i<lineWidth; ++i) rgbhole[i] = color;

	for (int i=0; i<data.size(); ++i) {
	    int nextHole = holes[0].position>0 ? 1 : 0;
	    int lostMemory = 0; // count how much memory holes we already encountered
	    int additionalPixels = 0; // count how many pixels to add for past memory holes
	    for (int j=0; j<data.size(i); ++j) {
		Slot sl = data.elementAt(i, j);
		//System.out.println("slot "+sl.getLocation());

		if (nextHole > 0 && holes[nextHole].position < sl.getLocation()) {
		    // we have just passed a hole, take care of it
		    int offset = holes[nextHole].position - firstByte - lostMemory;
		    offset = (int)(((long)offset) * pixelsAvailable / memorySize); // compute the pixel offset
		    offset += additionalPixels; // correct for the memory holes
		    int line = offset / horizontalPixels;
		    offset -= line * horizontalPixels;

		    int end = holes[nextHole].position - firstByte - lostMemory;
		    end = (int)(((long)end) * pixelsAvailable / memorySize); // compute the pixel offset
		    end += additionalPixels + HOLE_PIXELS; // correct for the memory holes
		    int lineEnd = end / horizontalPixels;
		    end -= lineEnd * horizontalPixels;

		    // print the hole
		    for (int ln=line, pos=offset; ln<lineEnd || pos<end; pos++) {
			if (pos==horizontalPixels) {
			    pos = 0;
			    ln++;
			}
			// print the pixels
			//System.out.println("hole: "+pos+" "+ln*lineScan+1);
			tmp.setRGB(pos, ln*lineScan+lineStart, 1, lineWidth, rgbhole, 0, 1);
		    }
		    // add the hole to the scanner
		    lostMemory += holes[nextHole].size;
		    additionalPixels += HOLE_PIXELS;
		    nextHole = holes[0].position>nextHole ? nextHole+1 : 0;
		}

		int offset = sl.getLocation() - firstByte - lostMemory;
		offset = (int)(((long)offset) * pixelsAvailable / memorySize); // compute the pixel offset
		offset += additionalPixels; // correct for the memory holes
		//System.out.print("offset: "+offset);
		int line = offset / horizontalPixels;
		//System.out.print(" "+line);
		offset -= line * horizontalPixels;
		//System.out.println(" "+offset);

		int end = sl.getLocation() + sl.getSize() - firstByte - lostMemory;
		end = (int)(((long)end) * pixelsAvailable / memorySize); // compute the pixel offset
		end += additionalPixels; // correct for the memory holes
		int lineEnd = end / horizontalPixels;
		end -= lineEnd * horizontalPixels;

		// print the slot
		for (int ln=line, pos=offset; ln<lineEnd || pos<=end; pos++) {
		    if (pos==horizontalPixels) {
			pos = 0;
			ln++;
		    }
		    // print the pixels
		    //System.out.println("position: "+pos+" "+ln*lineScan+1);
		    tmp.setRGB(pos, ln*lineScan+lineStart, 1, lineWidth, rgbarray, 0, 1);
		    crossReference[ln * horizontalPixels + pos] = sl;
		}
	    }
	}

	setIcon(new ImageIcon(tmp));
	maxUnitIncrement = lineScan;
    }

    public Dimension getPreferredSize() {
        if (missingPicture) {
            return new Dimension(320, 320);
        } else {
            return super.getPreferredSize();
        }
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition -
                             (currentPosition / maxUnitIncrement)
                              * maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1)
                   * maxUnitIncrement
                   - currentPosition;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxUnitIncrement;
        } else {
            return visibleRect.height - maxUnitIncrement;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }

    public int getBytes() { return memorySize; }
    public int getPixels() { return pixelsAvailable; }
    public int getNumLines() { return numLines; }
    public int getLineSize() { return lineScan; }
    public int getHPixels() { return horizontalPixels; }
    public Slot getMemorySlot(int x, int y) {
	int line = y/lineScan;
	int offset = y - line*lineScan;
	// check if we are outside the printed band
	if (offset <= lineStart || offset > lineStart+lineWidth) return null;
	return crossReference[line*horizontalPixels+x];
    }
}
