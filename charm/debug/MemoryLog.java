package charm.debug;

public class MemoryLog {
	private int number;
	private long location;
	private int size;
	private int sizeBefore;
	private long stack[];

	public MemoryLog(int n, long l, int s, int before) {
		number = n;
		location = l;
		size = s;
		sizeBefore = before;
		stack = null;
	}

	public MemoryLog(long l, int s, int before) {
		location = l;
		size = s;
		sizeBefore = before;
		stack = null;
		//System.out.println("creating log "+location+": "+sizeBefore+" + "+size);
	}
	
	public MemoryLog(long l, int s, int before, long[] st) {
		location = l;
		size = s;
		sizeBefore = before;
		stack = st;
	}
	
	public int getNumber() { return number; }
	public long getLocation() { return location; }
	public int getSize() { return size; }
	public int getSizeBefore() { return sizeBefore; }
	public int getSizeAfter() { return sizeBefore + size; }
	public long[] getStack() { return stack; }
	public long getStack(int i) { return stack[i]; }
	
	public int hashCode() { return (int)(location^(location>>>32)); }
	public boolean equals(MemoryLog o) { return location == o.location; } 
}
