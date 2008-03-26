package charm.debug.preference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Execution implements Serializable {
	private static final long serialVersionUID = -6713359626743543537L;
	public String executable;
	public String parameters;
	public int npes;
	public String port;
	public String hostname;
	public String username;
	public boolean sshTunnel;
	public String locationOnDisk;
	
	public static Execution load(File filename) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
		Execution exec = (Execution) ois.readObject();
		exec.locationOnDisk = filename.getAbsolutePath();
		return exec;
	}
}
