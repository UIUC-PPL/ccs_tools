package charm.debug.pdata;

import javax.swing.JComponent;
import charm.debug.inspect.InspectPanel;

/**
 * Superclass for all the classes containing a piece of information existant on
 * the parallel application.
 */
public abstract class GenericInfo {
    public abstract void getDetails(InspectPanel panel);
}
