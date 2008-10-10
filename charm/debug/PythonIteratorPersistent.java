package charm.debug;

import charm.ccs.CcsServer;

public class PythonIteratorPersistent extends PythonIteratorGroup {
	int[] eps;
	
	public PythonIteratorPersistent(int _id, int _n, int[] _eps) {
		super(_id);
		eps = new int[_n];
		for (int i=0; i<_n; ++i) eps[i] = _eps[i];
	}
	
	public int size() {
		return super.size() + 4*(eps.length+1);
	}
	
	public byte[] pack() {
		byte[] result = new byte[size()];
		CcsServer.writeInt(result, 0, id);
		CcsServer.writeInt(result, 4, eps.length);
		for (int i=0; i<eps.length; ++i) {
			CcsServer.writeInt(result, 8+i*4, eps[i]);
		}
		return result;
	}
}
