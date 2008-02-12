package charm.debug.preference;

import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Preference extends JPanel {
	public Dimension size;
	public Point location;
	public Recent recentConfig;
	
	public Preference() {
		size = null;
		location = null;
		recentConfig = new Recent();
	}
	
	public Object[] getRecent() {
		return recentConfig.getList();
	}
	public void addRecent(String file) {
		recentConfig.add(file);
	}
	
	public void load() {
		BufferedReader in;
		String str;
		try {
			in = new BufferedReader(new FileReader(System.getProperty("user.home")+"/.cpdrc"));
		} catch (FileNotFoundException e) {
			return;
		}
		System.out.println("Loading preference file "+System.getProperty("user.home")+"/.cpdrc");
		try {
			str = in.readLine();
			str = str.substring(str.indexOf(' ')+1);
			try {
				size = new Dimension(Integer.parseInt(str.substring(0, str.indexOf(' '))),
						Integer.parseInt(str.substring(str.indexOf(' ')+1)));
			} catch (Exception e) { }
			str = in.readLine();
			str = str.substring(str.indexOf(' ')+1);
			try {
				location = new Point(Integer.parseInt(str.substring(0, str.indexOf(' '))),
						Integer.parseInt(str.substring(str.indexOf(' ')+1)));
			} catch (Exception e) { }
			recentConfig.load(in);
			in.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Could not load configuration file.\nPlease delete the file '.cpdrc' from your home directory.", "Failed to load configuration", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public void save() {
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(System.getProperty("user.home")+"/.cpdrc"));
			out.write("WindowSize "+(size!=null?size.width+" "+size.height:"?")+"\n");
			out.write("WindowPosition "+(location!=null?location.x+" "+location.y:"?")+"\n");
			recentConfig.save(out);
			out.close();
		} catch (IOException e) {
			System.out.println("Failed to save configuration file");
		}
	}
}
