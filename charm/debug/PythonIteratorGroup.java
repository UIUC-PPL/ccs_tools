package charm.debug;

import charm.ccs.*;

public class PythonIteratorGroup implements PythonIterator {
	int id;

	public PythonIteratorGroup (int i) {
		id = i;
	}

	public int size() {
		return 4;
	}
	public byte[] pack() {
		byte[] result = new byte[4];
		CcsServer.writeInt(result, 0, id);
		return result;
	}
	public void unpack() { }
}
