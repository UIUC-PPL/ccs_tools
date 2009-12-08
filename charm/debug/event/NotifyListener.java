package charm.debug.event;

public interface NotifyListener {
	public void receiveNotification(NotifyEvent e);
	/*
	public void notifyBreakpoint(int pe, String txt);
	public void notifyFreeze(int pe, String txt);
	public void notifyAbort(int pe, String txt);
	public void notifySignal(int pe, String txt);
	public void notifyCorruption(int pe, String txt);
	*/
}
