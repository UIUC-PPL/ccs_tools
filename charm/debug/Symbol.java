package charm.debug;

import java.util.Hashtable;

public class Symbol {
    public static Hashtable symbolTable = new Hashtable();

    private String function;
    private int line;
    private String file;

    public String toString() {
	return function+" at "+file+":"+line;
    }

    public Symbol(String str, int l, String f) {
	function = str;
	line = l;
	file = f;
    }

    public static Symbol get(int location) {
	return (Symbol)symbolTable.get(new Integer(location));
    }

    public static void put(int location, Symbol s) {
        symbolTable.put(new Integer(location), s);
    }
}
