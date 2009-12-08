package charm.debug.event;

public class NotifyEvent {
	
	public static final int BREAKPOINT = 1;
	public static final int ABORT = 2;
	public static final int SIGNAL = 3;
	public static final int FREEZE = 4;
	public static final int CORRUPTION = 5;
	
	public int type;
	public int pe;
	public String txt;
	
	public NotifyEvent(int t, int p, String s) {
		type = t;
		pe = p;
		txt = s;
	}
}
