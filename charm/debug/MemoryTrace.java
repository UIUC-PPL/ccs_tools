package charm.debug;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.io.IOException;
import java.io.Reader;

public class MemoryTrace {
	private String filePrefix;
	private int numProcs;
	private int numEvents[];

	public static final int BEGIN_TRACE = 11;
	public static final int MALLOC = 24;
	public static final int FREE = 25;

	public MemoryTrace(String prefix, int procs) {
		filePrefix = prefix;
		numProcs = procs;
		numEvents = new int[procs];
	}

	private String getFilename(int i) {
		return filePrefix + i;
	}

	public int findPeaks(int procNum, int howMany, Vector result)
	        throws IOException {
		String file = getFilename(procNum);
		BufferedReader fd = new BufferedReader(new FileReader(file));

		boolean peakValid = true;
		int totalsize = 0;
		String line;
		int counter = 0;
		while ((line = fd.readLine()) != null) {
			counter++;
			// System.out.println("Reading log "+counter+": "+line);
			String[] tokens = line.split("\\s");
			int type = Integer.parseInt(tokens[0]);
			int size;
			if (type == BEGIN_TRACE) {
				size = Integer.parseInt(tokens[1]);
				if (size > totalsize) {
					type = MALLOC;
					size -= totalsize;
				} else {
					type = FREE;
					size = totalsize - size;
				}
			} else {
				size = Integer.parseInt(tokens[2]);
			}

			if (type == MALLOC) {
				peakValid = true;
				totalsize += size;
			} else if (type == FREE) {
				totalsize -= size;
				if (peakValid) {
					peakValid = false;
					int j;
					for (j = result.size(); j > 0; --j) {
						if (totalsize <= ((MemoryPeak) result.elementAt(j - 1)).size) break;
					}
					if (j < howMany) {
						if (result.size() == howMany) result.remove(howMany - 1);
						result.insertElementAt(new MemoryPeak(counter, totalsize, procNum), j);
					}
				}
			} else {
				throw new IOException("Unrecognized Memory Log format");
			}
			// System.out.println("Reading log "+counter+": "+line+" ->
			// "+totalsize);
		}

		return counter;
	}

	public Vector findAllPeaks(int howMany) throws IOException {
		Vector result = new Vector(howMany);
		for (int i = 0; i < numProcs; ++i) {
			numEvents[i] = findPeaks(i, howMany, result);
		}
		return result;
	}

	public Vector readLogs(int proc, int from, int to) throws IOException {
		Vector results = new Vector(to - from + 2);
		BufferedReader file = new BufferedReader(new FileReader(getFilename(proc)));
		int totalsize = 0;
		for (int i = 0; i < from; ++i) {
			totalsize += getNext(file, totalsize, false).getSize();
			
		}
		long stack[] = new long[1];
		stack[0] = 0;
		results.add(new MemoryLog(0, totalsize, 0, stack));
		for (int i = from; i <= to; ++i) {
			MemoryLog log = getNext(file, totalsize, true);
			results.add(log);
			totalsize += log.getSize();
		}
		return results;
	}

	private MemoryLog getNext(BufferedReader file, int totalsize, boolean getStack)
	        throws IOException {
		String line = file.readLine();
		if (line == null) return null;
		String[] tokens = line.split("\\s");
		int type = Integer.parseInt(tokens[0]);
		long location;
		int size;
		int recordedStack = 0;
		long stack[] = null;
		if (type == BEGIN_TRACE) {
			location = 0;
			size = Integer.parseInt(tokens[1]);
			if (size > totalsize) {
				size -= totalsize;
				stack = new long[1];
				stack[0] = 0;
			} else {
				size = totalsize - size;
			}
		} else {
			try {
				location = Long.decode(tokens[1]).longValue();
			} catch (NumberFormatException ne) {
				location = 0;
				if (!tokens[1].equals("(nil)")) throw ne;
			}
			if (type == MALLOC) {
				size = Integer.parseInt(tokens[2]);
				recordedStack = Integer.parseInt(tokens[3]);
				int stackSize = recordedStack;
				if (stackSize == 0) stackSize = 1;
				stack = new long[stackSize];
				stack[0] = 0; // Initialize the case where no stack is present
				for (int i=0; i<recordedStack; ++i) {
					stack[i] = Long.decode(tokens[i+4]).longValue();
				}
			}
			else if (type == FREE) {
				size = -Integer.parseInt(tokens[2]);
				stack = new long[1];
				stack[0] = 0;
			}
			else throw new IOException("Unrecognized Memory Log format");
		}
		return new MemoryLog(location, size, totalsize, stack);
	}

	public static void main(String[] argv) throws IOException {
		for (int i = 0; i < argv.length; ++i)
			System.out.println("argv " + i + ": " + argv[i]);
		if (argv.length < 1) return;
		String fileName = argv[0];
		int quantity = 10;
		int numProcs = 0;
		if (argv.length > 1) quantity = Integer.parseInt(argv[1]);
		MemoryTrace log;
		Vector peaks;
		if (argv.length <= 2) {
			log = new MemoryTrace(fileName, numProcs);
			peaks = new Vector(quantity);
			log.findPeaks(0, quantity, peaks);
		} else {
			numProcs = Integer.parseInt(argv[2]);
			log = new MemoryTrace(fileName, numProcs);
			peaks = log.findAllPeaks(quantity);
		}
		for (int i = 0; i < peaks.size(); ++i) {
			System.out.println("Peak #" + (i + 1) + ": "
			        + (MemoryPeak) peaks.elementAt(i));
		}
	}
}
