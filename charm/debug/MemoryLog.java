package charm.debug;

import java.io.BufferedReader;
import java.io.IOException;

public class MemoryLog {
	private int number;
	private long location;
	private int size;
	private int sizeBefore;

	public MemoryLog(int n, long l, int s, int before) {
		number = n;
		location = l;
		size = s;
		sizeBefore = before;
	}

	public MemoryLog(long l, int s, int before) {
		location = l;
		size = s;
		sizeBefore = before;
		//System.out.println("creating log "+location+": "+sizeBefore+" + "+size);
	}
	
	public int getNumber() { return number; }
	public long getLocation() { return location; }
	public int getSize() { return size; }
	public int getSizeBefore() { return sizeBefore; }
	public int getSizeAfter() { return sizeBefore + size; }
}
