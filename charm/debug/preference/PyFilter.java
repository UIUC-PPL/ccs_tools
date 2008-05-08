package charm.debug.preference;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class PyFilter extends FileFilter {

    //Accept all directories and all cpd files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("py")) {
            	return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    //The description of this filter
    public String getDescription() {
        return "Python files (*.py)";
    }
}
