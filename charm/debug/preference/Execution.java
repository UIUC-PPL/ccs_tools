package charm.debug.preference;

import charm.util.ReflectiveXML;
import java.io.*;
import org.xml.sax.SAXException;

public class Execution implements Serializable {
	//private static final long serialVersionUID = -6713359626743543537L;
	public String executable;
	public String parameters;
	public int npes;
	public String ccshost;
	public String port;
	public int sshport;
	public String hostname;
	public String username;
	public boolean sshTunnel;
	public String locationOnDisk;
	public String workingDir;
	public String inputFile;
	public boolean waitFile;
	
	public boolean virtualDebug;
	public int virtualNpes;

	public static Execution load(File filename) throws IOException, ClassNotFoundException, SAXException {
		//try {
		FileReader fw = new FileReader(filename);
		Execution exec = (Execution)ReflectiveXML.read(fw);
		//} catch (Exception e) {
		//System.err.println(e);
		//}
		//ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
		//Execution exec = (Execution) ois.readObject();
		exec.locationOnDisk = filename.getAbsolutePath();
		return exec;
	}

	public void save(File filename) throws IOException {
		//ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
		//oos.writeObject(exec);
		//oos.close();
		FileWriter fos = new FileWriter(filename);
		ReflectiveXML.write(fos, this);
		fos.close();
	}
}
