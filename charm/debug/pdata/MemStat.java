package charm.debug.pdata;

import java.text.NumberFormat;

import javax.swing.*;

import charm.debug.fmt.*;

public class MemStat {
	
	private Single[] array;
	
	public class Single {
		int pe;
		int[][] sizes;
		int[][] counts;
		
		Single(int p) {
			pe = p;
			sizes = new int[2][5];
			counts = new int[2][5];
		}
		
		public JPanel display() {
			JPanel panel = new JPanel();
			NumberFormat f = NumberFormat.getInstance();
	    	StringBuffer buf = new StringBuffer("<html>Memory Usage: <table border=1><th><td>Num. alloc</td><td>Total size</td><td>Num. leaks</td><td>Leak size</td></th>");
	    	String names[] = {"Unknown", "System", "User", "Chare", "Message"};
	    	for (int i=0; i<5; ++i) {
	    		buf.append("<tr><td>").append(names[i]).append("</td><td align=right>")
	    		   .append(f.format(counts[0][i])).append("</td><td align=right>")
	    		   .append(f.format(sizes[0][i])).append("</td><td align=right>")
	    		   .append(f.format(counts[1][i])).append("</td><td align=right>")
	    		   .append(f.format(sizes[1][i])).append("</td></tr>");
	    	}
	    	buf.append("<tr><td>Total</td><td align=right>");
	    	buf.append(f.format(counts[0][0]+counts[0][1]+counts[0][2]+counts[0][3]+counts[0][4]));
	    	buf.append("</td><td align=right>");
	    	buf.append(f.format(sizes[0][0]+sizes[0][1]+sizes[0][2]+sizes[0][3]+sizes[0][4]));
	    	buf.append("</td><td align=right>");
	    	buf.append(f.format(counts[1][0]+counts[1][1]+counts[1][2]+counts[1][3]+counts[1][4]));
	    	buf.append("</td><td align=right>");
	    	buf.append(f.format(sizes[1][0]+sizes[1][1]+sizes[1][2]+sizes[1][3]+sizes[1][4]));
	    	buf.append("</td></tr>");
	    	buf.append("</table></html>");
	    	panel.add(new JLabel(buf.toString()));
			return panel;
		}
	}
	
	public MemStat() {}
	
	public void load(PList list) {
		if (list==null) System.out.println("list is null!");
		int count = ((PNative)list.elementNamed("count")).getIntValue(0);
        array = new Single[count];
        
        PList singles = (PList)list.elementNamed("list");
        int i=0;
        for (PAbstract cur=singles.elementAt(0);cur!=null;cur=cur.getNext()) {
            PList lcur=(PList)cur; // because cur is itself an object
            
            int pe = ((PNative)lcur.elementNamed("pe")).getIntValue(0);
            Single s = new Single(pe);
            array[i++] = s;
            
            PNative values;
            values = (PNative)lcur.elementNamed("totalsize");
            for (int j=0; j<5; ++j) s.sizes[0][j] = values.getIntValue(j);
            
            values = (PNative)lcur.elementNamed("totalcount");
            for (int j=0; j<5; ++j) s.counts[0][j] = values.getIntValue(j);

            values = (PNative)lcur.elementNamed("leaksize");
            for (int j=0; j<5; ++j) s.sizes[1][j] = values.getIntValue(j);

            values = (PNative)lcur.elementNamed("leakcount");
            for (int j=0; j<5; ++j) s.counts[1][j] = values.getIntValue(j);
        }
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("count: ").append(array.length);
		for (int i=0; i<array.length; ++i) {
			Single s = array[i];
			buf.append("{ pe=").append(s.pe).append(" ");
			for (int j=0; j<2; ++j) {
				for (int k=0; k<5; ++k) {
					buf.append(s.counts[j][k]).append(" ").append(s.sizes[j][k]).append("  ");
				}
			}
			buf.append("}");
		}
		return buf.toString();
	}
	
	public JPanel display() {
		JPanel panel = new JPanel();
		JTabbedPane tabs = new JTabbedPane();
		for (int i=0; i<array.length; ++i) {
			tabs.addTab(""+array[i].pe, array[i].display());
		}
		panel.add(tabs);
		return panel;
	}
}
