package charm.debug.preference;

import charm.util.ReflectiveXML;

import java.io.*;
import java.util.Vector;
import java.awt.*;
import javax.swing.*;
import org.xml.sax.SAXException;

public class Preference extends JPanel {
	public Dimension size;
	public Point location;
	public Recent recentConfig;
	public Recent recentPython;
	
	public Preference() {
		size = null;
		location = null;
		recentConfig = new Recent();
		recentPython = new Recent();
	}
	
	public Object[] getRecent() {
		return recentConfig.getList();
	}
	public void addRecent(String file) {
		recentConfig.add(file);
		//System.out.println("Adding recent file: "+file);
	}
	public Object[] getRecentPython() {
		if (recentPython == null) recentPython = new Recent();
		return recentPython.getList();
	}
	public void addRecentPython(String str) {
		if (recentPython == null) recentPython = new Recent();
		recentPython.add(str);
	}
	
	public static Preference load() {
		FileReader in;
		String str;
		try {
			in = new FileReader(System.getProperty("user.home")+"/.cpdrc");
		} catch (FileNotFoundException e) {
			return null;
		}
		System.out.println("Loading preference file "+System.getProperty("user.home")+"/.cpdrc");
		try {
			Preference pref = (Preference)ReflectiveXML.read(in);
			/*
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
			*/
			in.close();
			return pref;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not load configuration file.\nPlease delete the file '.cpdrc' from your home directory.", "Failed to load configuration", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(null, "Could not load configuration file.\nPlease delete the file '.cpdrc' from your home directory.", "Failed to load configuration", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	public void save() {
		FileWriter out;
		try {
			out = new FileWriter(System.getProperty("user.home")+"/.cpdrc");
			ReflectiveXML.write(out, this);
			//out.write("WindowSize "+(size!=null?size.width+" "+size.height:"?")+"\n");
			//out.write("WindowPosition "+(location!=null?location.x+" "+location.y:"?")+"\n");
			//recentConfig.save(out);
			out.close();
		} catch (IOException e) {
			System.out.println("Failed to save configuration file");
		}
	}
}
