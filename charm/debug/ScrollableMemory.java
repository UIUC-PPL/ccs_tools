package charm.debug;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

import charm.debug.fmt.*;
import java.util.Collections;
import java.util.Date;

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
    private int rgbnormal[][];
    private int rgbleak[];
    private int rgbselected[];
    private int rgbhole[];
    private Slot selectedSlot;

    public int viewX, viewY;

    public ScrollableMemory(MemoryPanel parent, int forPE, int scan, int lines, int horiz) {
        super();
	int m=1;

	lineScan = scan;
	numLines = lines;
	horizontalPixels = horiz;
	pe = forPE;

	loadImage(false);

	setHorizontalAlignment(CENTER);
	setOpaque(true);
	setBackground(Color.black);

	//Let the user scroll by dragging to outside the window.
	setAutoscrolls(true); //enable synthetic drag events
    }

    public void loadImage(boolean isLeak) {
	//System.out.println("started request for data "+(new Date()).toString());
	PList list;
	if (isLeak) {
	    list=ParDebug.server.getPList("converse/memory/leak",pe,ParDebug.globals);
	}
	else list=ParDebug.server.getPList("converse/memory",pe);
	//System.out.println("received data from server ("+list.size()+") "+(new Date()).toString());
	data = new MemoryPList(list);
	//System.out.println("memory list construced "+(new Date()).toString());
	data.sort();
	//System.out.println("list sorted "+(new Date()).toString());

	firstByte = Integer.MAX_VALUE;
	lastByte = Integer.MIN_VALUE;
	for (int i=0; i<data.size(); ++i) {
	    System.out.println("list "+i+" contains "+data.size(i)+" elements");
	    if (data.size(i) == 0) continue;
	    if (data.elementAt(i, 0).getLocation() < firstByte)
		firstByte = data.elementAt(i, 0).getLocation();
	    if (data.elementAt(i, data.size(i)-1).getLocation() > lastByte)
		lastByte = data.elementAt(i, data.size(i)-1).getLocation() + data.elementAt(i, data.size(i)-1).getSize();
	}

	holes = data.findHoles();
	//System.out.println("holes detected "+(new Date()).toString());

	memorySize = lastByte - firstByte + 1 - holes[0].size;

	//System.out.println("data size = "+memorySize);
	selectedSlot = null;
	resizeImage();
    }

    public void resizeImage(int scan, int lines, int horiz) {
	lineScan = scan;
	numLines = lines;
	horizontalPixels = horiz;
	resizeImage();
    }

    private void resizeImage() {
	lineWidth = (int)(lineScan*3/4);
	lineStart = (lineScan - lineWidth) / 2;
	verticalPixels = numLines*lineScan;

	pixelsAvailable = verticalPixels / lineScan * horizontalPixels;
	crossReference = new Slot[pixelsAvailable];
	pixelsAvailable -= HOLE_PIXELS * holes[0].position;

	BufferedImage tmp = new BufferedImage(horizontalPixels,verticalPixels,BufferedImage.TYPE_INT_ARGB);

	int color = (255<<24) + (255<<16) + (77<<8) + 77;
	rgbnormal = new int[5][lineWidth];
	for (int i=0; i<lineWidth; ++i) rgbnormal[0][i] = color;
	color = (255<<24) + (255<<16) + (178<<8) + 77;
	for (int i=0; i<lineWidth; ++i) rgbnormal[3][i] = color;
	color = (255<<24) + (77<<16) + (77<<8) + 255;
	for (int i=0; i<lineWidth; ++i) rgbnormal[4][i] = color;

	color = (255<<24) + (0<<16) + (255<<8) + 0;
	rgbleak = new int[lineWidth];
	for (int i=0; i<lineWidth; ++i) rgbleak[i] = color;
	color = (255<<24) + (255<<16) + (255<<8) + 155;
	rgbselected = new int[lineWidth];
	for (int i=0; i<lineWidth; ++i) rgbselected[i] = color;
	color = (255<<24) + (180<<16) + (180<<8) + 180;
	rgbhole = new int[lineWidth];
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
		    if (sl.isLeak()) tmp.setRGB(pos, ln*lineScan+lineStart, 1, lineWidth, rgbleak, 0, 1);
		    else tmp.setRGB(pos, ln*lineScan+lineStart, 1, lineWidth, rgbnormal[sl.getType()], 0, 1);
		    crossReference[ln * horizontalPixels + pos] = sl;
		}
	    }
	}

	setIcon(new ImageIcon(tmp));
	selectSlot(selectedSlot);
	maxUnitIncrement = lineScan;
    }

    private void drawSlot(Slot sl, int[] color) {
	int lostMemory = 0;
	int additionalPixels = 0;
	for (int index = 1; index <= holes[0].position && holes[index].position < sl.getLocation(); index++) {
	    lostMemory += holes[index].size;
	    additionalPixels += HOLE_PIXELS;
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
	    ((BufferedImage)((ImageIcon)getIcon()).getImage()).setRGB(pos, ln*lineScan+lineStart, 1, lineWidth, color, 0, 1);
	}
	repaint();
    }

    public void selectSlot(Slot sl) {
	if (selectedSlot != null) {
	    if (selectedSlot.isLeak()) drawSlot(selectedSlot, rgbleak);
	    else drawSlot(selectedSlot, rgbnormal[selectedSlot.getType()]);
	}
	selectedSlot = sl;
	if (sl != null) {
	    drawSlot(sl, rgbselected);
	}
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
	// Check if we are outside the printed band. this in general returns
	// null. Nevertheless, if we are in a non printed band, but between two
	// bands containing the same Slot, just assume we are still inside. This
	// prevent the flickering effect while moving the mouse.
	if ((offset <= lineStart && line > 0 &&
	     crossReference[line*horizontalPixels+x] == crossReference[(line-1)*horizontalPixels+x]) ||
	    (offset > lineStart+lineWidth && line < verticalPixels/lineScan &&
	     crossReference[line*horizontalPixels+x] == crossReference[(line+1)*horizontalPixels+x])) {
	    return crossReference[line*horizontalPixels+x];
	}
	if (offset <= lineStart || offset > lineStart+lineWidth) return null;
	return crossReference[line*horizontalPixels+x];
    }
}
