package charm.debug;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.io.IOException;
import java.awt.image.*;
import java.util.List;

public class AllocationGraphPanel extends JPanel implements ActionListener,
        MouseListener, MouseMotionListener, ItemListener {

    private JMenuBar menuBar;
    private JMenu menuAction;
    private JCheckBoxMenuItem menuAllocationTree;
    private JCheckBoxMenuItem menuAllocationTreeFromBeginning;
	private JScrollPane displayPane;
	private ScrollableAllocationGraph allocationData;
	private JFrame atFrame;
	private AllocationTreePanel at;
	private boolean traceWrite;

	AllocationGraphPanel() {
		setLayout(new BorderLayout());
		atFrame = null;
		
		// Create the menu
		menuBar = new JMenuBar();
		menuBar.add(menuAction = new JMenu("Allocation Tree"));
		menuAction.setMnemonic('A');
		menuAction.add(menuAllocationTree = new JCheckBoxMenuItem("Show"));
		menuAllocationTree.addItemListener(this);
		menuAction.add(menuAllocationTreeFromBeginning = new JCheckBoxMenuItem("Start from beginning"));
		menuAllocationTreeFromBeginning.addItemListener(this);

		displayPane = new JScrollPane();
		add(displayPane, BorderLayout.CENTER);
	}

	class Corner extends JComponent {
		protected void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0,0,getWidth(),getHeight());
		}
	}

    public JMenuBar getMenu() {return menuBar;}

    public void load(JFrame frame, MemoryTrace log, AllocationGraphDialog input) {
		Vector logs;
		traceWrite = true;
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				if (atFrame != null) atFrame.dispose();
			}
		}
		);
		
		try {
			logs = log.readLogs(input.getPe(), input.getFirstEvent(), input.getLastEvent());
		} catch (IOException e) {
			System.out.println("An error occured while trying to load the memory logs");
			return;
		}
		allocationData = new ScrollableAllocationGraph(logs, input.getEventsPerBar(), input.getBarWidth(), input.getHeight());
		allocationData.addMouseListener(this);
		allocationData.addMouseMotionListener(this);
		ToolTipManager.sharedInstance().registerComponent(allocationData);
		
//		BufferedImage tmp = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		for (int i=0; i<input.getWidth(); i++) {
//			for (int j=0; j<input.getHeight(); j++) {
//				tmp.setRGB(i, j, (255<<24)+255);
//			}
//		}
		displayPane.setViewportView(allocationData);

		
//		BufferedImage tmp2 = new BufferedImage(50, input.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		for (int i=0; i<50; i++) {
//			for (int j=0; j<input.getHeight(); j++) {
//				tmp2.setRGB(i, j, (255<<24)+(255<<8));
//			}
//		}
//		JLabel bar = new JLabel();
//		bar.setIcon(new ImageIcon(tmp2));
//		displayPane.setRowHeaderView(bar);
		displayPane.setRowHeaderView(allocationData.getRowHeader());
		
		displayPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
	}

    class AllocationTreeWindowClosed extends WindowAdapter {
    	AllocationGraphPanel parent;
    	AllocationTreeWindowClosed(AllocationGraphPanel agp) {
    		super();
    		parent = agp;
    	}
    	public void windowClosing(WindowEvent e) {
    		parent.menuAllocationTree.setSelected(false);
    		parent.menuAllocationTree.setText("Show");
    	}
    }
    
	public void actionPerformed(ActionEvent e) {

	}
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getItemSelectable() == menuAllocationTree) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (atFrame == null) {
		    		atFrame = new JFrame("Allocation Tree");
		    		atFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		    		atFrame.addWindowListener(new AllocationTreeWindowClosed(this));
		    		at = new AllocationTreePanel();
		    		JComponent newContentPane = at;
		    		newContentPane.setOpaque(true);
		    		atFrame.setContentPane(newContentPane);
		    		atFrame.setTitle("Allocation Tree");
		    		
					List logs;
					if (menuAllocationTreeFromBeginning.isSelected()) logs = allocationData.getLogsFromBeginning();
					else logs = allocationData.getSelectedLogs();
		    		at.loadMemoryLogs(logs, menuAllocationTreeFromBeginning.isSelected());
					atFrame.pack();
				}
				atFrame.setVisible(true);
				menuAllocationTree.setText("Hide");
			} else {
				atFrame.setVisible(false);
				menuAllocationTree.setText("Show");
			}
		} else if (e.getItemSelectable() == menuAllocationTreeFromBeginning) {
			updateAllocationTreeFrame();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == allocationData) {
		    traceWrite = !traceWrite;
		    updatePosition(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		deletePosition();
	}

	public void mousePressed(MouseEvent e) {
		if (e.getSource() == allocationData) {
		    allocationData.viewX = e.getX();
		    allocationData.viewY = e.getY();
		    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
	}

	public void mouseReleased(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		updatePosition(e);
	}

	public void mouseDragged(MouseEvent e) {
		if (e.getSource() == allocationData) {
		    JViewport jv = (JViewport)allocationData.getParent();
		    Point p = jv.getViewPosition();
		    int newX = p.x - (e.getX()-allocationData.viewX);
		    int newY = p.y - (e.getY()-allocationData.viewY);
		    int maxX = allocationData.getWidth() - jv.getWidth();
		    int maxY = allocationData.getHeight() - jv.getHeight();
		    if (newX > maxX) newX = maxX;
		    if (newY > maxY) newY = maxY;
		    if (newX < 0) newX = 0;
		    if (newY < 0) newY = 0;
		    jv.setViewPosition(new Point(newX, newY));
		}
	}

	public void mouseMoved(MouseEvent e) {
		updatePosition(e);
	}
	
	private void updatePosition(MouseEvent e) {
		if (traceWrite) {
			//System.out.println("Tooltip JPanel "+(createToolTip().isEnabled()?"enabled":"disabled"));
			if (e.getSource() == allocationData) {
				allocationData.selectPosition(e);
			} else {
				allocationData.selectPosition(null);
			}
			updateAllocationTreeFrame();
		}
	}

	private void updateAllocationTreeFrame() {
		if (atFrame != null && atFrame.isVisible()) {
			List logs;
			if (menuAllocationTreeFromBeginning.isSelected()) logs = allocationData.getLogsFromBeginning();
			else logs = allocationData.getSelectedLogs();
			at.loadMemoryLogs(logs, menuAllocationTreeFromBeginning.isSelected());
			//at.repaint();
		}
	}
	
	private void deletePosition() {
		if (traceWrite) allocationData.selectPosition(null);
	}
}
