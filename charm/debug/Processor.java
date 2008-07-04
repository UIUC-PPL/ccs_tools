package charm.debug;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Processor implements Comparable {
	public static final int RUNNING = 1;
	public static final int FROZEN = 2;
	public static final int REQUESTED_FREEZE = 3;
	public static final int DEAD = 4;
	
	int status;
	int id;
	Set sets;
	
	public Processor(int i) {
		id = i;
		status = FROZEN;
		sets = new HashSet();
	}
	
	public void addToSet(PeSet p) {
		sets.add(p);
	}
	
	public int compareTo(Object o) {
		Processor p = (Processor)o;
		return id - p.id;
	}
	
	public int getId() {return id;}

	public boolean isFrozen() {return status == FROZEN;}
	public boolean isRunning() {return status == RUNNING;}
	public boolean isFreezing() {return status == REQUESTED_FREEZE;}
	public boolean isDead() {return status == DEAD;}
	
	public void setFrozen() {
		if (status != DEAD) {
			status = FROZEN;
			Iterator iter = sets.iterator();
			while (iter.hasNext()) ((PeSet)iter.next()).setFrozen();
		}
	}
	public void setRunning() {
		status = RUNNING;
		Iterator iter = sets.iterator();
		while (iter.hasNext()) ((PeSet)iter.next()).setRunning();
	}
	public void setFreezing() {
		status = REQUESTED_FREEZE;
	}
	public void setDead() {
		Iterator iter = sets.iterator();
		while (iter.hasNext()) {
			PeSet p = (PeSet)iter.next();
			if (!isFrozen()) p.setFrozen();
			p.setDead();
		}
		status = DEAD;
	}
	
	public String toString() {
		return ""+id+"("+(status==RUNNING?"R":(status==DEAD?"D":status==FROZEN?"F":"f"))+")";
	}
}
