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

    public String getFunction() {return function;}

    public static Symbol get(long location) {
    	Symbol s = (Symbol)symbolTable.get(new Long(location));
    	if (s == null) {
    		// resolve the symbol in the info gdb
    		String res1 = ParDebug.infoCommand("info:info symbol "+location+"\n");
    		//System.out.println(res1);
    		int index = res1.indexOf('+');
    		String funcName = index>=0 ? res1.substring(0, index).trim() : "??";
    		String res2 = ParDebug.infoCommand("info:info line *"+location+"\n");
    		index = res2.indexOf("Line");
    		String fileName;
    		int line;
    		if (index == -1) {
    			line = 0;
    			fileName = "??";
    		} else {
    			int index2 = res2.indexOf(' ', index+5);
    			//System.out.println(res2+" "+index+" "+index2);
    			line = Integer.parseInt(res2.substring(index+5,index2));
    			index = res2.indexOf('"');
    			index2 = res2.indexOf('"', index+1);
    			fileName = res2.substring(index+1,index2).trim();
    		}
    		s = new Symbol(funcName, line, fileName);
    		Symbol.put(location, s);
    	}
    	return s;
    }

    public static void put(long location, Symbol s) {
    	symbolTable.put(new Long(location), s);
    }
}
