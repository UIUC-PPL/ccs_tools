package charm.debug;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.NumberFormat;

import charm.debug.pdata.Slot;
import charm.debug.inspect.InspectPanel;

public class MemoryPanel extends JPanel
    implements ActionListener, MouseListener, MouseMotionListener {

    private JMenuBar menuBar;
    private JMenu menuAction;
    private JMenuItem menuLeak;
    private JMenuItem menuLeakFast;
    private JMenu menuInfo;
    private JMenuItem menuStat;
    private JMenuItem menuInspect;
    private JSlider verticalZoom;
    private JSlider horizontalZoom;
    private JScrollPane displayPane;
    private JPanel topPane;
    private JPanel controlPane;
    private ScrollableMemory memoryData;
    private JButton update;
    private JTextField horizontalPixels;
    private JTextField verticalLines;
    private JTextField lineSize;
    private JLabel bytesPixel;
    private JButton minusLines, plusLines;
    private JButton minusVert, plusVert;
    private JButton minusHPixels, plusHPixels;
    private JLabel minusBytes, plusBytes;
    private JTextArea info;
    private JScrollPane infoScrollPane;
    private boolean traceWrite;

    public MemoryPanel() {
	setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

	// Create the menu
	menuBar = new JMenuBar();
	menuBar.add(menuAction = new JMenu("Action"));
	menuAction.setMnemonic('A');
	menuAction.add(menuLeak = new JMenuItem("Search Leaks",'L'));
	menuLeak.addActionListener(this);
	menuAction.add(menuLeakFast = new JMenuItem("Quick Search Leaks",'Q'));
	menuLeakFast.addActionListener(this);
	menuBar.add(menuInfo = new JMenu("Info"));
	menuInfo.setMnemonic('I');
	menuInfo.add(menuStat = new JMenuItem("Show Statistics",'L'));
	menuStat.addActionListener(this);
	menuInfo.add(menuInspect = new JMenuItem("Inspect",'I'));
	menuInspect.addActionListener(this);

	topPane = new JPanel();
	topPane.setLayout(new BorderLayout());

	// top panel for control
	JPanel controlPaneFlow = new JPanel();
	controlPaneFlow.setLayout(new FlowLayout(FlowLayout.LEFT));

	controlPane = new JPanel();
	GridBagLayout controlLayout = new GridBagLayout();
	controlPane.setLayout(controlLayout);
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.VERTICAL;
	c.insets = new Insets(1,1,1,1);

	// first column of control
	//JPanel first = new JPanel();
	//GridBagLayout firstLayout = new GridBagLayout();
	//first.setLayout(firstLayout);

	//JPanel firstTop = new JPanel();
	//firstTop.setLayout(new FlowLayout());

	JLabel spacing0 = new JLabel();
	spacing0.setPreferredSize(new Dimension(50,1));
	spacing0.setMinimumSize(new Dimension(10,1));
	c.weightx = 1;
	c.gridheight = 2;
	controlLayout.setConstraints(spacing0, c);
	controlPane.add(spacing0);

	// Number of lines
	c.weightx = 0;
	c.gridheight = 1;
	minusLines = new JButton("-");
	minusLines.addActionListener(this);
	controlLayout.setConstraints(minusLines, c);
	controlPane.add(minusLines);

	JPanel linesPane = new JPanel();
	linesPane.setLayout(new BoxLayout(linesPane, BoxLayout.Y_AXIS));
	verticalLines = new JTextField();
	verticalLines.setHorizontalAlignment(JTextField.RIGHT);
	//verticalLines.setNextFocusableComponent(lineSize);
	verticalLines.addActionListener(this);
	JLabel textLines = new JLabel("Number of lines");
	linesPane.add(verticalLines);
	linesPane.add(textLines);
	controlLayout.setConstraints(linesPane, c);
	controlPane.add(linesPane);

	plusLines = new JButton("+");
	plusLines.addActionListener(this);
	controlLayout.setConstraints(plusLines, c);
	controlPane.add(plusLines);

	JLabel spacing1 = new JLabel();
	spacing1.setPreferredSize(new Dimension(50,1));
	spacing1.setMinimumSize(new Dimension(10,1));
	c.weightx = 1;
	controlLayout.setConstraints(spacing1, c);
	controlPane.add(spacing1);

	// Horizontal pixels
	c.weightx = 0;
	minusHPixels = new JButton("-");
	minusHPixels.addActionListener(this);
	controlLayout.setConstraints(minusHPixels, c);
	controlPane.add(minusHPixels);

	JPanel hPixelsPane = new JPanel();
	hPixelsPane.setLayout(new BoxLayout(hPixelsPane, BoxLayout.Y_AXIS));
	horizontalPixels = new JTextField();
	horizontalPixels.setHorizontalAlignment(JTextField.RIGHT);
	//horizontalPixels.setNextFocusableComponent(verticalLines);
	horizontalPixels.addActionListener(this);
	JLabel textHPixels = new JLabel("Horizontal pixels", JLabel.CENTER);
	hPixelsPane.add(horizontalPixels);
	hPixelsPane.add(textHPixels);
	controlLayout.setConstraints(hPixelsPane, c);
	controlPane.add(hPixelsPane);

	plusHPixels = new JButton("+");
	plusHPixels.addActionListener(this);
	controlLayout.setConstraints(plusHPixels, c);
	controlPane.add(plusHPixels);

	JLabel spacing2 = new JLabel();
	spacing2.setPreferredSize(new Dimension(50,1));
	spacing2.setMinimumSize(new Dimension(10,1));
	c.weightx = 1;
	controlLayout.setConstraints(spacing2, c);
	controlPane.add(spacing2);

	// Update button
	update = new JButton("Update");
	update.setActionCommand("update");
	update.addActionListener(this);
	c.weightx = 0;
	c.gridheight = 2;
	c.fill = GridBagConstraints.NONE;
	controlLayout.setConstraints(update, c);
	controlPane.add(update);

	JLabel spacing3 = new JLabel();
	spacing3.setPreferredSize(new Dimension(1,1));
	spacing3.setMinimumSize(new Dimension(1,1));
	c.weightx = 1000;
	c.fill = GridBagConstraints.VERTICAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	controlLayout.setConstraints(spacing3, c);
	controlPane.add(spacing3);

	// Line size
	minusVert = new JButton("-");
	minusVert.addActionListener(this);
	c.gridwidth = 1;
	c.gridheight = 1;
	c.weightx = 0;
	c.gridx = 1;
	controlLayout.setConstraints(minusVert, c);
	controlPane.add(minusVert);

	JPanel vertPane = new JPanel();
	vertPane.setLayout(new BoxLayout(vertPane, BoxLayout.Y_AXIS));
	lineSize = new JTextField();
	lineSize.setHorizontalAlignment(JTextField.RIGHT);
	//lineSize.setNextFocusableComponent(horizontalPixels);
	lineSize.addActionListener(this);
	JLabel textVert = new JLabel("Line size");
	vertPane.add(lineSize);
	vertPane.add(textVert);
	c.gridx = 2;
	controlLayout.setConstraints(vertPane, c);
	controlPane.add(vertPane);

	plusVert = new JButton("+");
	plusVert.addActionListener(this);
	c.gridx = 3;
	controlLayout.setConstraints(plusVert, c);
	controlPane.add(plusVert);

	// Bytes per pixel
	//minusBytes = new JLabel("");
	//controlLayout.setConstraints(minusBytes, c);
	//controlPane.add(minusBytes);

	//JPanel bytesPane = new JPanel();
	//bytesPane.setLayout(new BoxLayout(bytesPane, BoxLayout.Y_AXIS));
	bytesPixel = new JLabel(" ");
	bytesPixel.setHorizontalAlignment(JTextField.CENTER);
	//JLabel textBytes = new JLabel("Bytes per pixel");
	//bytesPane.add(bytesPixel);
	//bytesPane.add(textBytes);
	c.gridx = 5;
	c.gridwidth = 3;
	c.fill = GridBagConstraints.BOTH;
	controlLayout.setConstraints(bytesPixel, c);
	controlPane.add(bytesPixel);

	//plusBytes = new JLabel("");
	//c.gridwidth = GridBagConstraints.REMAINDER;
	//controlLayout.setConstraints(plusBytes, c);
	//controlPane.add(plusBytes);

	//controlPaneFlow.add(controlPane);
	topPane.add(controlPane, BorderLayout.NORTH);

	// the main panel for display is added later in the center of topPane by loadData
	displayPane = new JScrollPane();
	topPane.add(displayPane, BorderLayout.CENTER);

	// bottom panel to contain the information text: it is a JScrollPane to
	// allow scrolling, which containts a JPanel. This itself contains the
	// real writable TextArea.
	info = new JTextArea();
	info.setText("Prova di scrittura\ndi una frase lunga.");
	//info.setPreferredSize(new Dimension(100, 500));
	info.setEditable(false);
	infoScrollPane = new JScrollPane(info);
	infoScrollPane.setBorder(BorderFactory.createTitledBorder("Information"));
	infoScrollPane.setPreferredSize(new Dimension(100, 150));
	/*
	JPanel infoPane = new JPanel();
	infoScrollPane = new JScrollPane(infoPane);
	infoScrollPane.setBorder(BorderFactory.createTitledBorder("Information"));
	infoScrollPane.setPreferredSize(new Dimension(100, 150));
	infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.X_AXIS));
	info = new JTextArea();
	info.setText("Prova di scrittura\ndi una frase lunga.");
	//info.setPreferredSize(new Dimension(100, 500));
	info.setEditable(false);
	infoPane.add(info, BorderLayout.CENTER);
	*/

	// main panel divided between the control/display, and the info area
	JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPane, infoScrollPane);
	mainPane.setResizeWeight(1);
	add(mainPane);

    }

    public JMenuBar getMenu() {return menuBar;}

    public void loadData(MemoryDialog input) {
	int pe = input.getPe();
	int scan = input.getScan();
	int lines = input.getLines();
	int hPixels = input.getHPixels();
	verticalLines.setText(""+lines);
	horizontalPixels.setText(""+hPixels);
	lineSize.setText(""+scan);
	memoryData = new ScrollableMemory(this, pe, scan, lines, hPixels);
	memoryData.addMouseListener(this);
	memoryData.addMouseMotionListener(this);
	displayBytes();
	displayPane.setViewportView(memoryData);
	traceWrite = true;
    }

    private void displayBytes() {
	int bytes = memoryData.getBytes() / memoryData.getPixels();
	if (bytes >= 10) {
	    bytesPixel.setText("Bytes per pixel: "+bytes);
	} else {
	    // display also 2 decimal digits
	    int decimal1 = (int)((long)memoryData.getBytes() * 10 / memoryData.getPixels()) % 10;
	    int decimal2 = (int)((long)memoryData.getBytes() * 100 / memoryData.getPixels()) % 10;
	    bytesPixel.setText("Bytes per pixel: "+bytes+"."+decimal1+decimal2);
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == menuLeak || e.getSource() == menuLeakFast) {
	    System.out.println("Leak search");
	    memoryData.loadImage(true, (e.getSource()==menuLeakFast) ? 1 : 0);
	    traceWrite = true;
	    displayBytes();
	    repaint();
	} else if (e.getSource() == menuStat) {
	    JOptionPane.showMessageDialog(this, "Memory Usage: "+NumberFormat.getInstance().format(memoryData.getAllocatedMemory())+" bytes\nAllocated blocks: "+NumberFormat.getInstance().format(memoryData.getNumAllocations()), "Memory Statistics", JOptionPane.INFORMATION_MESSAGE);
	} else if (e.getSource() == menuInspect) {
		Slot sl = memoryData.getSelectedSlot();
		if (sl!=null) {
            JFrame frame = new JFrame("Memory inspector");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            InspectPanel inspect = new InspectPanel();
            JComponent newContentPane = inspect;
            newContentPane.setOpaque(true);
            inspect.load(memoryData.getPe(), sl.getLocation(), null);
            frame.setContentPane(newContentPane);
            frame.pack();
            frame.setVisible(true);
		}
	} else if (e.getActionCommand().equals("update")) {
	    memoryData.loadImage(false, 0);
	    traceWrite = true;
	    displayBytes();
	    repaint();
	} else {
	    int lines, scan, horiz;
	    // check that all parameters are valid
	    try {
		lines = Integer.parseInt(verticalLines.getText());
		scan = Integer.parseInt(lineSize.getText());
		horiz = Integer.parseInt(horizontalPixels.getText());
	    } catch (NumberFormatException ne) {
		JOptionPane.showMessageDialog(this, "All values must be positive integers", "Error", JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    if (scan <= 3) {
		JOptionPane.showMessageDialog(this, "The width of the line must be greater than 3", "Error", JOptionPane.ERROR_MESSAGE);
		lineSize.requestFocus();
	    } else if (lines <= 0) {
		JOptionPane.showMessageDialog(this, "The number of lines must be positive", "Error", JOptionPane.ERROR_MESSAGE);
		verticalLines.requestFocus();
	    } else if (horiz <= 10) {
		JOptionPane.showMessageDialog(this, "The number of horizontal pixels must be greater than 10", "Error", JOptionPane.ERROR_MESSAGE);
		horizontalPixels.requestFocus();
	    } else {

		// apply any kind of specific behaviour
		if (e.getSource() == minusLines) {
		    lines -= lines*0.1;
		    verticalLines.setText(""+lines);
		}
		if (e.getSource() == plusLines) {
		    lines += lines*0.1;
		    verticalLines.setText(""+lines);
		}
		if (e.getSource() == verticalLines) {
		}
		if (e.getSource() == minusVert) {
		    scan -= scan*0.1;
		    if (scan < 4) scan = 4;
		    lineSize.setText(""+scan);
		}
		if (e.getSource() == plusVert) {
		    scan += scan*0.1;
		    lineSize.setText(""+scan);
		}
		if (e.getSource() == lineSize) {
		}
		if (e.getSource() == minusHPixels) {
		    horiz -= horiz*0.1;
		    horizontalPixels.setText(""+horiz);
		}
		if (e.getSource() == plusHPixels) {
		    horiz += horiz*0.1;
		    horizontalPixels.setText(""+horiz);
		}
		if (e.getSource() == horizontalPixels) {
		}

		// complete the resizing with the generation of a new image
		if (lines != memoryData.getNumLines() ||
		    scan != memoryData.getLineSize() ||
		    horiz != memoryData.getHPixels()) {
		    // all right, update the size of the image
		    memoryData.resizeImage(scan, lines, horiz);
		    displayBytes();
		    repaint();
		}
	    }
	}
    }

    public void mouseClicked(MouseEvent e) {
	if (e.getSource() == memoryData) {
	    traceWrite = !traceWrite;
	    updatePosition(e);
	}
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {
	deletePosition();
    }

    public void mousePressed(MouseEvent e) {
	if (e.getSource() == memoryData) {
	    memoryData.viewX = e.getX();
	    memoryData.viewY = e.getY();
	    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}
    }

    public void mouseReleased(MouseEvent e) {
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	updatePosition(e);
    }

    public void mouseDragged(MouseEvent e) {
	if (e.getSource() == memoryData) {
	    JViewport jv = (JViewport)memoryData.getParent();
	    Point p = jv.getViewPosition();
	    int newX = p.x - (e.getX()-memoryData.viewX);
	    int newY = p.y - (e.getY()-memoryData.viewY);
	    int maxX = memoryData.getWidth() - jv.getWidth();
	    int maxY = memoryData.getHeight() - jv.getHeight();
	    if (newX > maxX) newX = maxX;
	    if (newY > maxY) newY = maxY;
	    if (newX < 0) newX = 0;
	    if (newY < 0) newY = 0;
	    jv.setViewPosition(new Point(newX, newY));
	}
    }

    public void mouseMoved(MouseEvent e) {
	if (e.getSource() == memoryData) {
	    updatePosition(e);
	} else {
	    deletePosition();
	}
    }

    private void updatePosition(MouseEvent e) {
	if (traceWrite) {
	    Slot sl = memoryData.getMemorySlot(e.getX(), e.getY());
	    if (sl != null) {
		info.setText(sl.toString());
		info.setCaretPosition(0);
		memoryData.selectSlot(sl);
	    } else {
		//info.setText("mouse at: "+e.getX()+" "+e.getY());
		info.setText("");
		memoryData.selectSlot(null);
	    }
	}
    }

    private void deletePosition() {
	if (traceWrite) {
	    info.setText("");
	    memoryData.selectSlot(null);
	}
    }
}
