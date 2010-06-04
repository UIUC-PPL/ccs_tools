package charm.debug.pdata;

import charm.debug.fmt.PList;
import javax.swing.DefaultListModel;
import javax.swing.JList;

import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * Superclass for all the classes containing some information existant on the
 * parallel application. All the subclasses are created starting from a
 * fmt.PList, which is the list automatically created from the pupping of the
 * data out of the application.
 */
public abstract class GenericPList {
    protected Vector data;

    public abstract boolean needRefresh();
    public abstract void load(PList p);
    public void removePopupMenu(JList l) { }
    public void addPopupMenu(JList l) { }

    public GenericPList() {
        data = new Vector();
    }
    
    public int size() { return data.size(); }

    public void populate(DefaultListModel model, JList list) {
        for (int i=0; i<data.size(); ++i) {
            model.addElement(data.elementAt(i));
        }
        list.setComponentPopupMenu(null);
        addPopupMenu(list);
   }

}
