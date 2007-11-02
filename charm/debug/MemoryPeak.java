package charm.debug;

public class MemoryPeak {
	int logNumber;
	int size;
	int proc;

	public MemoryPeak(int n, int s, int p) {
		logNumber = n;
		size = s;
		proc = p;
	}

	public String toString() {
		return "" + logNumber + " " + size + " on " + proc;
	}
}
