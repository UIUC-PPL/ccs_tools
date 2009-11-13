package charm.debug;

import java.awt.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import charm.debug.pdata.*;

public class PythonInstalledCode extends JDialog {
	Vector installed;
	
	JTable table;
	PythonTableModel tableModel;
	
	public static final int BEFORE = 0;
	public static final int AFTER = 1;
	
	class InstalledCode {
		String code;
		EpInfo ep;
		ChareInfo chare;
		int where;
		
		InstalledCode(String s, EpInfo e, ChareInfo c, int w) {
			code = s;
			ep = e;
			chare = c;
			where = w;
		}
	}
	
	public static final String[] colNames = {"Entry Point", "Where", "Associated Chare", "Code"};
	class PythonTableModel extends AbstractTableModel {
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return installed.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			InstalledCode line = (InstalledCode)(installed.elementAt(rowIndex));
			switch (columnIndex) {
			case 0:
				return line.ep;
			case 1:
				return (line.where==BEFORE?"BEFORE":"") + (line.where==AFTER?"AFTER":"");
			case 2:
				return line.chare;
			case 3:
				return "<html><pre>"+line.code+"</pre></html>";
			default:
				return null;
			}
        }
		
		public String getColumnName(int index) {
			return colNames[index];
		}
		
	}
	
	static class PythonCodeRenderer extends DefaultTableCellRenderer {
		 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			 Component ret = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			 int height = ret.getPreferredSize().height;
			 //System.out.println("table height: "+height);
			 if (table.getRowHeight(row) != height) table.setRowHeight(row, height);
			 return ret;
		 }
	}
	
	public PythonInstalledCode(boolean modal) {
		super((Frame)null, "Python script", modal);
		installed = new Vector();
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setMinimumSize(new Dimension(400, 300));
		
		tableModel = new PythonTableModel();
		table = new JTable(tableModel);
		table.getColumnModel().getColumn(3).setCellRenderer(new PythonCodeRenderer());
		//table.setDefaultRenderer(String.class, new PythonCodeRenderer());
		JScrollPane tableScroll = new JScrollPane(table);
		//table.setFillsViewportHeight(true);
		add(tableScroll);
		//JLabel tmp = new JLabel("tmp");
		//add(tmp);
	}

	public void add(String code, Vector[] eps, ChareInfo chare) {
		int firstRow = tableModel.getRowCount();
		for (int i=0; i<eps[BEFORE].size(); ++i) installed.add(new InstalledCode(code, (EpInfo)eps[0].elementAt(i), chare, BEFORE));
		for (int i=0; i<eps[AFTER].size(); ++i) installed.add(new InstalledCode(code, (EpInfo)eps[1].elementAt(i), chare, AFTER));
		int lastRow = tableModel.getRowCount()-1;
		tableModel.fireTableRowsInserted(firstRow, lastRow);
		//add(new JLabel("new code"));
	}
}
