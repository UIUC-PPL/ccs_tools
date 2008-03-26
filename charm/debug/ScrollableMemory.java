package charm.debug;

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;

import javax.swing.*;

import charm.debug.fmt.*;
import charm.debug.pdata.MemoryPList;
import charm.debug.pdata.Slot;

/* ScrollableMemory.java is used by MemoryWindow.java to display the content of
 * the memory. */

public class ScrollableMemory extends JLabel implements Scrollable {

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
	private int allocatedMemory;
	private long firstByte;
	private long lastByte;
	private MemoryPList.Hole holes[];
	private Slot[] crossReference;
	private int rgbnormal[][];
	private int rgbleak[][];
	private int rgbdim[][];
	private int rgbdimleak[][];
	private int rgbselected[];
	private int rgbhole[];
	private Slot selectedSlot;
	private boolean selectDim;
	private Vector chareLists;

	public int viewX, viewY;

	public ScrollableMemory(MemoryPanel parent, int forPE, int scan, int lines,
	        int horiz) {
		super();

		selectDim = false;
		lineScan = scan;
		numLines = lines;
		horizontalPixels = horiz;
		pe = forPE;

		loadImage(false, 0);

		setHorizontalAlignment(CENTER);
		setOpaque(true);
		setBackground(Color.black);

		// Let the user scroll by dragging to outside the window.
		setAutoscrolls(true); // enable synthetic drag events
	}

	public void loadImage(boolean isLeak, int low) {
		// System.out.println("started request for data "+(new
		// Date()).toString());
		PList list;
		if (isLeak) {
			list = ParDebug.server.getPList("converse/memory/leak", pe, low, 1, ParDebug.globals);
		} else list = ParDebug.server.getPList("converse/memory", pe);
		// System.out.println("received data from server ("+list.size()+")
		// "+(new Date()).toString());
		data = new MemoryPList();
		data.load(list);
		// System.out.println("memory list construced "+(new
		// Date()).toString());
		data.sort();
		// System.out.println("list sorted "+(new Date()).toString());

		firstByte = Long.MAX_VALUE;
		lastByte = Long.MIN_VALUE;
		for (int i = 0; i < data.size(); ++i) {
			System.out.println("list " + i + " contains " + data.size(i)
			        + " elements");
			if (data.size(i) == 0) continue;
			if (data.elementAt(i, 0).getLocation() < firstByte) firstByte = data.elementAt(i, 0).getLocation();
			if (data.elementAt(i, data.size(i) - 1).getLocation() > lastByte) lastByte = data.elementAt(i, data.size(i) - 1).getLocation()
			        + data.elementAt(i, data.size(i) - 1).getSize();
		}

		holes = data.findHoles();
		// System.out.println("holes detected "+(new Date()).toString());

		memorySize = (int) (lastByte - firstByte + 1 - holes[0].getSize());
		allocatedMemory = 0;

		// System.out.println("data size = "+memorySize);
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
		lineWidth = (lineScan * 3 / 4);
		lineStart = (lineScan - lineWidth) / 2;
		verticalPixels = numLines * lineScan;

		pixelsAvailable = verticalPixels / lineScan * horizontalPixels;
		crossReference = new Slot[pixelsAvailable];
		pixelsAvailable -= HOLE_PIXELS * holes[0].getPosition();

		BufferedImage tmp = new BufferedImage(horizontalPixels, verticalPixels, BufferedImage.TYPE_INT_ARGB);

		int color;
		rgbnormal = new int[5][lineWidth];
		// unknown
		color = (255 << 24) + (255 << 16) + (77 << 8) + 77;
		for (int i = 0; i < lineWidth; ++i)
			rgbnormal[0][i] = color;
		// system
		color = (255 << 24) + (220 << 16) + (69 << 8) + 69;
		for (int i = 0; i < lineWidth; ++i)
			rgbnormal[1][i] = color;
		// user
		color = (255 << 24) + (90 << 16) + (150 << 8) + 255;
		for (int i = 0; i < lineWidth; ++i)
			rgbnormal[2][i] = color;
		// chares
		color = (255 << 24) + (255 << 16) + (178 << 8) + 77;
		for (int i = 0; i < lineWidth; ++i)
			rgbnormal[3][i] = color;
		// messages
		color = (255 << 24) + (255 << 16) + (150 << 8) + 255;
		for (int i = 0; i < lineWidth; ++i)
			rgbnormal[4][i] = color;

		rgbleak = new int[5][lineWidth];
		color = (255 << 24) + (0 << 16) + (255 << 8) + 0;
		for (int j = 0; j < 5; ++j) {
			for (int i = 0; i < (int) (0.75 * lineWidth); ++i)
				rgbleak[j][i] = color;
			for (int i = (int) (0.75 * lineWidth); i < lineWidth; ++i)
				rgbleak[j][i] = rgbnormal[j][i];
		}
		
		rgbdim = new int[5][lineWidth];
		rgbdimleak = new int[5][lineWidth];
		for (int j = 0; j < 5; ++j) {
			for (int i = 0; i < lineWidth; ++i) {
				rgbdim[j][i] = rgbnormal[j][i] - (128 << 24);
				rgbdimleak[j][i] = rgbleak[j][i] - (128 << 24);
			}
		}

		rgbselected = new int[lineWidth];
		color = (255 << 24) + (255 << 16) + (255 << 8) + 155;
		for (int i = 0; i < lineWidth; ++i)
			rgbselected[i] = color;

		rgbhole = new int[lineWidth];
		color = (255 << 24) + (180 << 16) + (180 << 8) + 180;
		for (int i = 0; i < lineWidth; ++i)
			rgbhole[i] = color;

		for (int i = 0; i < data.size(); ++i) {
			int nextHole = holes[0].getPosition() > 0 ? 1 : 0;
			int lostMemory = 0; // count how much memory holes we already
								// encountered
			int additionalPixels = 0; // count how many pixels to add for past
										// memory holes
			for (int j = 0; j < data.size(i); ++j) {
				Slot sl = data.elementAt(i, j);
				System.out.println("slot " + sl.getLocation() + " + "
				        + sl.getSize());

				if (nextHole > 0
				        && holes[nextHole].getPosition() < sl.getLocation()) {
					// we have just passed a hole, take care of it
					int offset = (int) (holes[nextHole].getPosition()
					        - firstByte - lostMemory);
					offset = (int) (((long) offset) * pixelsAvailable / memorySize); // compute
																						// the
																						// pixel
																						// offset
					offset += additionalPixels; // correct for the memory holes
					int line = offset / horizontalPixels;
					offset -= line * horizontalPixels;

					int end = (int) (holes[nextHole].getPosition() - firstByte - lostMemory);
					end = (int) (((long) end) * pixelsAvailable / memorySize); // compute
																				// the
																				// pixel
																				// offset
					end += additionalPixels + HOLE_PIXELS; // correct for the
															// memory holes
					int lineEnd = end / horizontalPixels;
					end -= lineEnd * horizontalPixels;

					// print the hole
					for (int ln = line, pos = offset; ln < lineEnd || pos < end; pos++) {
						if (pos == horizontalPixels) {
							pos = 0;
							ln++;
						}
						// print the pixels
						// System.out.println("hole: "+pos+" "+ln*lineScan+1);
						tmp.setRGB(pos, ln * lineScan + lineStart, 1, lineWidth, rgbhole, 0, 1);
					}
					// add the hole to the scanner
					lostMemory += holes[nextHole].getSize();
					additionalPixels += HOLE_PIXELS;
					nextHole = holes[0].getPosition() > nextHole ? nextHole + 1
					        : 0;
				}

				int offset = (int) (sl.getLocation() - firstByte - lostMemory);
				offset = (int) (((long) offset) * pixelsAvailable / memorySize); // compute
																					// the
																					// pixel
																					// offset
				offset += additionalPixels; // correct for the memory holes
				System.out.print("offset: " + offset);
				int line = offset / horizontalPixels;
				System.out.print(" " + line);
				offset -= line * horizontalPixels;
				System.out.println(" " + offset);

				int end = (int) (sl.getLocation() + sl.getSize() - firstByte - lostMemory);
				end = (int) (((long) end) * pixelsAvailable / memorySize); // compute
																			// the
																			// pixel
																			// offset
				end += additionalPixels; // correct for the memory holes
				int lineEnd = end / horizontalPixels;
				end -= lineEnd * horizontalPixels;

				// print the slot
				System.out.println("drawing slot " + sl.getLocation() + "+"
				        + sl.getSize() + " from " + line + ":" + offset
				        + " to " + lineEnd + ":" + end);
				for (int ln = line, pos = offset; ln < lineEnd || pos <= end; pos++) {
					if (pos == horizontalPixels) {
						pos = 0;
						ln++;
						// if (ln==numLines) break;
					}
					// print the pixels
					// System.out.println("position: "+pos+" "+(ln*lineScan+1));
					if (sl.isLeak()) tmp.setRGB(pos, ln * lineScan + lineStart, 1, lineWidth, rgbleak[sl.getType()], 0, 1);
					else tmp.setRGB(pos, ln * lineScan + lineStart, 1, lineWidth, rgbnormal[sl.getType()], 0, 1);
					crossReference[ln * horizontalPixels + pos] = sl;
				}
			}
		}

		setIcon(new ImageIcon(tmp));
		selectSlot(selectedSlot);
		chareLists = null;
		dimByChareID(selectDim);
		maxUnitIncrement = lineScan;
	}

	private void drawSlot(Slot sl, int[] color) {
		drawSlot(sl, color, lineWidth);
	}
	
	private void drawSlot(Slot sl, int[] color, int lineW) {
		long lostMemory = 0;
		int additionalPixels = 0;
		for (int index = 1; index <= holes[0].getPosition()
		        && holes[index].getPosition() < sl.getLocation(); index++) {
			lostMemory += holes[index].getSize();
			additionalPixels += HOLE_PIXELS;
		}

		int offset = (int) (sl.getLocation() - firstByte - lostMemory);
		offset = (int) (((long) offset) * pixelsAvailable / memorySize); // compute
																			// the
																			// pixel
																			// offset
		offset += additionalPixels; // correct for the memory holes
		// System.out.print("offset: "+offset);
		int line = offset / horizontalPixels;
		// System.out.print(" "+line);
		offset -= line * horizontalPixels;
		// System.out.println(" "+offset);

		int end = (int) (sl.getLocation() + sl.getSize() - firstByte - lostMemory);
		end = (int) (((long) end) * pixelsAvailable / memorySize); // compute
																	// the pixel
																	// offset
		end += additionalPixels; // correct for the memory holes
		int lineEnd = (int) end / horizontalPixels;
		end -= lineEnd * horizontalPixels;

		// print the slot
		for (int ln = line, pos = offset; ln < lineEnd || pos <= end; pos++) {
			if (pos == horizontalPixels) {
				pos = 0;
				ln++;
			}
			// print the pixels
			// System.out.println("position: "+pos+" "+ln*lineScan+1);
			((BufferedImage) ((ImageIcon) getIcon()).getImage()).setRGB(pos, ln
			        * lineScan + lineStart, 1, lineW, color, 0, 1);
		}
		repaint();
	}

	public void selectSlot(Slot sl) {
		if (selectedSlot != null) {
			if (selectedSlot.isLeak()) drawSlot(selectedSlot, rgbleak[selectedSlot.getType()]);
			else drawSlot(selectedSlot, rgbnormal[selectedSlot.getType()]);
		}
		if (selectDim) {
			// dim the different type
			if (selectedSlot != null) {
				Vector list = (Vector)chareLists.elementAt(selectedSlot.getChareID());
				for (int i=0; i<list.size(); ++i) {
					Slot s = (Slot)list.elementAt(i);
					if (s.isLeak()) drawSlot(s, rgbdimleak[s.getType()]);
					else drawSlot(s, rgbdim[s.getType()]);
				}
			}
			if (sl != null) {
				Vector list = (Vector)chareLists.elementAt(sl.getChareID());
				for (int i=0; i<list.size(); ++i) {
					Slot s = (Slot)list.elementAt(i);
					if (s.isLeak()) drawSlot(s, rgbleak[s.getType()]);
					else drawSlot(s, rgbnormal[s.getType()]);
				}
			}
		}
		selectedSlot = sl;
		if (sl != null) drawSlot(sl, rgbselected, lineWidth>>1);
	}

	public void dimByChareID(boolean dim) {
		if (dim && chareLists == null) {
			// construct the lists
			chareLists = new Vector();
			for (int i=0; i<data.size(); ++i) {
				for (int j=0; j<data.size(i); ++j) {
					Slot sl = data.elementAt(i, j);
					int type = sl.getChareID();
					if (chareLists.size() <= type) chareLists.setSize(type+1);
					if (chareLists.elementAt(type)==null) chareLists.setElementAt(new Vector(), type);
					((Vector)chareLists.elementAt(type)).add(sl);
				}
			}
		}
		if (dim) {
			for (int i=0; i<data.size(); ++i) {
				for (int j=0; j<data.size(i); ++j) {
					Slot sl = data.elementAt(i, j);
					if (sl.isLeak()) drawSlot(sl, rgbdimleak[sl.getType()]);
					else drawSlot(sl, rgbdim[sl.getType()]);
				}
			}
		} else {
			for (int i=0; i<data.size(); ++i) {
				for (int j=0; j<data.size(i); ++j) {
					Slot sl = data.elementAt(i, j);
					if (sl.isLeak()) drawSlot(sl, rgbleak[sl.getType()]);
					else drawSlot(sl, rgbnormal[sl.getType()]);
				}
			}
		}
		selectDim = dim;
		selectSlot(selectedSlot);
	}

	public Slot getSelectedSlot() {
		return selectedSlot;
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
	        int orientation, int direction) {
		// Get the current position.
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL) {
			currentPosition = visibleRect.x;
		} else {
			currentPosition = visibleRect.y;
		}

		// Return the number of pixels between currentPosition
		// and the nearest tick mark in the indicated direction.
		if (direction < 0) {
			int newPosition = currentPosition
			        - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			return ((currentPosition / maxUnitIncrement) + 1)
			        * maxUnitIncrement - currentPosition;
		}
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
	        int orientation, int direction) {
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

	public int getPe() { return pe; }
	public int getBytes() { return memorySize; }
	public int getPixels() { return pixelsAvailable; }
	public int getNumLines() { return numLines; }
	public int getLineSize() { return lineScan; }
	public int getHPixels() { return horizontalPixels; }

	public Slot getMemorySlot(int x, int y) {
		int line = y / lineScan;
		int offset = y - line * lineScan;
		// Check if we are outside the printed band. this in general returns
		// null. Nevertheless, if we are in a non printed band, but between two
		// bands containing the same Slot, just assume we are still inside. This
		// prevent the flickering effect while moving the mouse.
		if ((offset <= lineStart && line > 0 && crossReference[line
		        * horizontalPixels + x] == crossReference[(line - 1)
		        * horizontalPixels + x])
		        || (offset > lineStart + lineWidth
		                && line < verticalPixels / lineScan && crossReference[line
		                * horizontalPixels + x] == crossReference[(line + 1)
		                * horizontalPixels + x])) {
			return crossReference[line * horizontalPixels + x];
		}
		if (offset <= lineStart || offset > lineStart + lineWidth) return null;
		return crossReference[line * horizontalPixels + x];
	}

	public int getAllocatedMemory() {
		if (allocatedMemory == 0) {
			for (int i = 0; i < data.size(0); ++i)
				allocatedMemory += data.elementAt(0, i).getSize();
		}
		return allocatedMemory;
	}

	public int getNumAllocations() {
		return data.size(0);
	}
}
