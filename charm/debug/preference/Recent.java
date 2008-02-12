package charm.debug.preference;

import java.util.Vector;
import java.io.*;
import javax.swing.*;

public class Recent implements Serializable {
	Vector exec;
	int maxAllowed;
	
	public Recent() {
		maxAllowed = 10;
		exec = new Vector();
	}
	
	public void add(String last) {
		exec.remove(last);
		exec.insertElementAt(last, 0);
		if (exec.size() > maxAllowed) exec.remove(maxAllowed);
	}
	
	public Object[] getList() {
		return exec.toArray();
	}
	
	public void load(BufferedReader in) throws IOException {
		String str = in.readLine();
		maxAllowed = Integer.parseInt(str.substring(str.indexOf(' ')+1));
		str = in.readLine();
		int howmany = Integer.parseInt(str.substring(str.indexOf(' ')+1));
		for (int i=0; i<howmany; ++i) {
			str = in.readLine();
			exec.add(str.substring(str.indexOf(' ')+1));
		}
	}
	
	public void save(BufferedWriter o) throws IOException {
		o.write("RecentConfigMax "+maxAllowed+"\n");
		o.write("RecentConfig "+exec.size()+"\n");
		for (int i=0; i<exec.size(); ++i) {
			o.write("File "+((String)exec.elementAt(i))+"\n");
		}
	}
}
