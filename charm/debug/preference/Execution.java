package charm.debug.preference;

import java.io.Serializable;

public class Execution implements Serializable {
	public String executable;
	public String parameters;
	public int npes;
	public String port;
	public String hostname;
	public String username;
	public boolean sshTunnel;
}
