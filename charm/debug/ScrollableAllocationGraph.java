package charm.debug;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import java.util.Vector;
import java.util.List;
import java.util.Iterator;

public class ScrollableAllocationGraph extends JLabel implements Scrollable, ActionListener {

	private int maxUnitIncrement = 1;
	private int height, width, barWidth, eventsPerBar, numBars;
	private Vector logs;
	private int rgbnormal[];
	private int rgbselected[];
	private int maximumSize;
	private int selectedPosition;
	//private Graphics2D image;
	private MemoryBar bars[];

	JPopupMenu popup;
	int popupOverBarIndex;
	public int viewX, viewY;
	private int markedBar, lastMarkedBar;
	
	public ScrollableAllocationGraph(Vector logs, int eventsPerBar, int barWidth, int height) {
		super();
		
		this.logs = logs;
		this.eventsPerBar = eventsPerBar;
		this.barWidth = barWidth;
		this.height = height;
		numBars = (int)Math.ceil((double)logs.size() / eventsPerBar);
		width = numBars * barWidth;

		markedBar = -1;
		lastMarkedBar = -1;
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
		
		popup = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Set Mark");
		menuItem.setActionCommand("setmark");
		menuItem.addActionListener(this);
		popup.add(menuItem);
		menuItem = new JMenuItem("Remove Mark");
		menuItem.setActionCommand("removemark");
		menuItem.addActionListener(this);
		popup.add(menuItem);
		addMouseListener(new PopupListener(this));
	}
	
	private void resize() {
		
		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		//image = tmp.createGraphics();
		
		int color;
		rgbnormal = new int[height*barWidth];
		color = (255 << 24) + (255 << 16) + (77 << 8) + 77;
		for (int i = 0; i < height*barWidth; ++i)
			rgbnormal[i] = color;
		
		rgbselected = new int[height*barWidth];
		color = (255 << 24) + (255 << 16) + (255 << 8) + 155;
		for (int i = 0; i < height*barWidth; ++i)
			rgbselected[i] = color;

		bars = new MemoryBar[numBars];
		for (int i = 0; i < numBars; ++i) {
			int barHeight = 0;
			int lastLog = (i+1)*eventsPerBar;
			if (logs.size() <= lastLog) lastLog = logs.size()-1;
			for (int j = i*eventsPerBar+1; j <= lastLog; ++j) {
				int logHeight = ((MemoryLog)logs.elementAt(j)).getSizeAfter();
				if (logHeight > barHeight) barHeight = logHeight;
			}
			int barPixels = (int)(((long)height * barHeight) / maximumSize);
			bars[i] = new MemoryBar(logs.subList(i*eventsPerBar+1, lastLog+1), barHeight, i, barPixels);
			//System.out.println("Using "+barPixels+" pixels (size="+barHeight);
			tmp.setRGB(i*barWidth, height-barPixels, barWidth, barPixels, rgbnormal, 0, 1);
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
	
	class MemoryBar {
		List logs;
		int position;
		int pixels;
		int maximum;
		int maxMalloc;
		int maxFree;
		
		public MemoryBar(List l, int m, int pos, int p) {
			logs = l;
			maximum = m;
			position = pos;
			pixels = p;
			maxMalloc = maxFree = 0;
			for (Iterator i=logs.iterator(); i.hasNext(); ) {
				MemoryLog log = (MemoryLog)i.next();
				int size = log.getSize();
				if (size > maxMalloc) maxMalloc = size;
				else if (-size > maxFree) maxFree = -size;
			}
		}
	}
	
	class PopupListener extends MouseAdapter {
		ScrollableAllocationGraph parent;
		
		PopupListener(ScrollableAllocationGraph p) {
			super();
			parent = p;
		}
		
		public void mousePressed(MouseEvent e)
		{ checkForTriggerEvent(e); } 

		public void mouseReleased(MouseEvent e)
		{ checkForTriggerEvent(e); } 

		private void checkForTriggerEvent(MouseEvent e)
		{
			if (e.isPopupTrigger()) {
				parent.popupOverBarIndex = parent.getMemoryBarIndex(e.getX());
				parent.popup.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}
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
	
	public void selectPosition(MouseEvent e) {
		int bar = getMemoryBarIndex(e);
		int startPosition, endPosition;
		BufferedImage image = (BufferedImage) ((ImageIcon) getIcon()).getImage();
		// delete old selection
		if (selectedPosition != -1) {
			startPosition = endPosition = selectedPosition;
			if (lastMarkedBar != -1) {
				if (lastMarkedBar > selectedPosition) endPosition = lastMarkedBar;
				else startPosition = lastMarkedBar;
			}
			for (int i=startPosition; i<= endPosition; ++i) {
				image.setRGB(bars[i].position*barWidth,
						height-bars[i].pixels, barWidth, bars[i].pixels, rgbnormal, 0, 1);
			}
		}
		
		lastMarkedBar = markedBar;
		selectedPosition = bar;
		
		// write new selection
		if (selectedPosition != -1) {
			startPosition = endPosition = selectedPosition;
			if (markedBar != -1) {
				if (markedBar > selectedPosition) endPosition = markedBar;
				else startPosition = markedBar;
			}
			for (int i=startPosition; i <= endPosition; ++i) {
				image.setRGB(bars[i].position*barWidth,
						height-bars[i].pixels, barWidth, bars[i].pixels, rgbselected, 0, 1);
			}
		}
		repaint();
		createToolTip().repaint();
		//System.out.println("Tooltip "+(createToolTip().isEnabled()?"enabled":"disabled"));
	}

	public List getSelectedLogs() {
		if (selectedPosition == -1) return null;
		if (markedBar >= 0) {
			int firstBar = markedBar < selectedPosition ? markedBar : selectedPosition;
			int lastBar = markedBar < selectedPosition ? selectedPosition : markedBar;
			int lastLog = (lastBar+1)*eventsPerBar;
			if (logs.size() <= lastLog) lastLog = logs.size()-1;
			return logs.subList(firstBar*eventsPerBar+1, lastLog+1);
		}
		else return bars[selectedPosition].logs;
	}
	
	public List getLogsFromBeginning() {
		if (selectedPosition == -1) return null;
		int lastLog = (selectedPosition+1)*eventsPerBar;
		if (logs.size() <= lastLog) lastLog = logs.size()-1;
		return logs.subList(0, lastLog+1);
	}
	
	int getMemoryBarIndex(int position) {
		if (position==-1) return -1;
		int i = position / barWidth;
		return i;
	}
	
	int getMemoryBarIndex(MouseEvent e) {
		if (e == null) return -1;
		return getMemoryBarIndex(e.getX());
	}
	
	MemoryBar getMemoryBar(MouseEvent e) {
		int index = getMemoryBarIndex(e);
		if (index == -1) return null;
		return bars[index];
	}
	
	public Point getToolTipLocation(MouseEvent e) {
		JToolTip tip = createToolTip();
		tip.setTipText(getToolTipText(e));
		//System.out.println("Requested tooltip location "+e.getX()+"-"+tip.getPreferredSize().width+", "+e.getY()+"-"+tip.getPreferredSize().height);
		return new Point(e.getX()-tip.getPreferredSize().width, e.getY()-tip.getPreferredSize().height);
	}
	
	public String getToolTipText(MouseEvent e) {
		MemoryBar bar = getMemoryBar(e);
		return "<html>Total memory: "+bar.maximum
		+"<br>Biggest allocation: "+bar.maxMalloc
		+"<br>Biggest free: "+bar.maxFree
		+"</html>";
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("ScrollableAllocationGraph action: "+e);
		if (e.getActionCommand().equals("setmark")) {
			markedBar = popupOverBarIndex;
		}
		else if (e.getActionCommand().equals("removemark")) {
			markedBar = -1;
		}
	}
}