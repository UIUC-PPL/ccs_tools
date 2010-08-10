package charm.debug;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Processor implements Comparable {
	public static final int RUNNING = 1;
	public static final int FROZEN = 2;
	public static final int REQUESTED_FREEZE = 3;
	public static final int DEAD = 4;
	public static final int CONDITIONAL = 5;
	public static final int CONDITIONAL_DEAD = 6;
	private static final char codes[] = {' ', 'R', 'F', 'f', 'D', 'C', 'd'};
	
	int status;
	int id;
	Set sets;
	
	public Processor(int i) {
		id = i;
		status = REQUESTED_FREEZE;
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
	public boolean isDead() {return status == DEAD || status == CONDITIONAL_DEAD;}
	public boolean isConditional() {return status == CONDITIONAL || status == CONDITIONAL_DEAD;}
	
	public void setFrozen() {
		if (status != DEAD) {
			System.out.println("Processor "+id+" frozen");
			int oldstatus = status;
			status = FROZEN;
			if (oldstatus == CONDITIONAL) {
				Iterator iter = sets.iterator();
				while (iter.hasNext()) ((PeSet)iter.next()).unsetConditional();
			} else if (oldstatus == CONDITIONAL_DEAD) {
				Iterator iter = sets.iterator();
				while (iter.hasNext()) {
					PeSet pe = (PeSet)iter.next();
					pe.unsetConditional();
					pe.unsetDead();
				}
			} else {
				Iterator iter = sets.iterator();
				while (iter.hasNext()) ((PeSet)iter.next()).setFrozen();
			}
		}
	}
	public void setRunning() {
		status = RUNNING;
		Iterator iter = sets.iterator();
		while (iter.hasNext()) ((PeSet)iter.next()).setRunning();
	}
	public void setFreezing() {
		System.out.println("Processor "+id+" freezing");
		status = REQUESTED_FREEZE;
	}
	public void setDead() {
		Iterator iter = sets.iterator();
		while (iter.hasNext()) {
			PeSet p = (PeSet)iter.next();
			if (!isFrozen()) p.setFrozen();
			p.setDead();
		}
		if (status == CONDITIONAL) status = CONDITIONAL_DEAD;
		else status = DEAD;
	}
	public void setConditional() {
		System.out.println("Processor "+id+" conditional");
		status = CONDITIONAL;
		Iterator iter = sets.iterator();
		while (iter.hasNext()) {
			((PeSet)iter.next()).setConditional();
		}
	}
	
	public String toString() {
		return ""+id+"("+codes[status]+")";
	}
}
