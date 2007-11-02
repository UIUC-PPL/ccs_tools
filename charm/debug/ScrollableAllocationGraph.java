package charm.debug;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

import java.util.Vector;

public class ScrollableAllocationGraph extends JLabel implements Scrollable {

	private int maxUnitIncrement = 1;
	private int height, width, barWidth, eventsPerBar, numBars;
	private Vector logs;
	private int rgbnormal[];
	private int rgbselected[];
	private int barPixels[];
	private int maximumSize;
	private int selectedPosition;
	private Graphics2D image;
	
	public int viewX, viewY;
	
	public ScrollableAllocationGraph(Vector logs, int eventsPerBar, int barWidth, int height) {
		super();
		
		this.logs = logs;
		this.eventsPerBar = eventsPerBar;
		this.barWidth = barWidth;
		this.height = height;
		numBars = (int)Math.ceil((double)logs.size() / eventsPerBar);
		width = numBars * barWidth;
		barPixels = new int[numBars];

		selectedPosition = -1;
		maximumSize = 0;
		for (int i=0; i<logs.size(); ++i) {
			int size = ((MemoryLog)logs.elementAt(i)).getSizeAfter();
			//System.out.println("size = "+size);
			if (size > maximumSize) maximumSize = size;
		}
		System.out.println("maximumSize = "+maximumSize);
		
		resize();
		
		setHorizontalAlignment(CENTER);
		setOpaque(true);
		setBackground(Color.black);

		// Let the user scroll by dragging to outside the window.
		setAutoscrolls(true); // enable synthetic drag events
	}

	private void resize() {
		
		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image = tmp.createGraphics();
		
		int color;
		rgbnormal = new int[height*barWidth];
		color = (255 << 24) + (255 << 16) + (77 << 8) + 77;
		for (int i = 0; i < height*barWidth; ++i)
			rgbnormal[i] = color;
		
		rgbselected = new int[height*barWidth];
		color = (255 << 24) + (255 << 16) + (255 << 8) + 155;
		for (int i = 0; i < height*barWidth; ++i)
			rgbselected[i] = color;

		for (int i = 0; i < numBars; ++i) {
			int barHeight = 0;
			for (int j = i*eventsPerBar; j < (i+1)*eventsPerBar && j < logs.size(); ++j) {
				int logHeight = ((MemoryLog)logs.elementAt(j)).getSizeAfter();
				if (logHeight > barHeight) barHeight = logHeight;
			}
			barPixels[i] = (int)(((long)height * barHeight) / maximumSize);
			//System.out.println("Using "+barPixels+" pixels (size="+barHeight);
			tmp.setRGB(i*barWidth, height-barPixels[i], barWidth, barPixels[i], rgbnormal, 0, 1);
		}
		
		setIcon(new ImageIcon(tmp));
		//selectSlot(selectedSlot);
		maxUnitIncrement = 10;
	}
	
	public JLabel getRowHeader() {

		BufferedImage tmp = new BufferedImage(40, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tmp.createGraphics();
		g.setBackground(Color.BLACK);
		g.clearRect(0, 0, 40, height);
		
		double maxNumTicks = (double)height / 50;
		double bytePerTickD = maximumSize / maxNumTicks;
		System.out.print("Byte/Tick = "+bytePerTickD);
		int bytePerTick;
		if (bytePerTickD < 10000000) bytePerTick = (int)(1000000 * Math.ceil(bytePerTickD/1000000));
		else if (bytePerTickD < 100000000) bytePerTick = (int)(10000000 * Math.ceil(bytePerTickD/10000000));
		else bytePerTick = (int)(100000000 * Math.ceil(bytePerTickD/100000000));
		System.out.println(" -> "+bytePerTick);
		
		g.drawLine(30, 0, 30, height);
		for (int mb=0; mb < maximumSize; mb+=bytePerTick) {
			int y = (int)(((long)height * mb) / maximumSize) + 1;
			g.drawLine(25, height-y, 35, height-y);
			g.drawString(""+(mb/1000000), 5, height-y);
		}
		
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(tmp));
		label.setVerticalAlignment(SwingConstants.TOP);
		return label;
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

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
	        int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - maxUnitIncrement;
		} else {
			return visibleRect.height - maxUnitIncrement;
		}
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	public void selectPosition(int x, int y) {
		int i = x / barWidth;
		if (x==-1) i=-1;
		if (selectedPosition >= 0) {
			((BufferedImage) ((ImageIcon) getIcon()).getImage()).setRGB(selectedPosition*barWidth,
					height-barPixels[selectedPosition], barWidth, barPixels[selectedPosition], rgbnormal, 0, 1);
		}
		selectedPosition = i;
		if (i >= 0) {
			((BufferedImage) ((ImageIcon) getIcon()).getImage()).setRGB(i*barWidth,
					height-barPixels[i], barWidth, barPixels[i], rgbselected, 0, 1);
		}
		repaint();
		createToolTip().repaint();
		//System.out.println("Tooltip "+(createToolTip().isEnabled()?"enabled":"disabled"));
	}
	
	public Point getToolTipLocation(MouseEvent e) {
		JToolTip tip = createToolTip();
		tip.setTipText(getToolTipText(e));
		System.out.println("Requested tooltip location "+e.getX()+"-"+tip.getPreferredSize().width+", "+e.getY()+"-"+tip.getPreferredSize().height);
		return new Point(e.getX()-tip.getPreferredSize().width, e.getY()-tip.getPreferredSize().height);
	}
	
	public String getToolTipText(MouseEvent e) {
		return "??";
	}
}