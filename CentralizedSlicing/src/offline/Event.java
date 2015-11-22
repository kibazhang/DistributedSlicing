package offline;

import java.util.HashMap;
import java.util.Map;

public class Event {
	int value;
	Map<Integer, Integer> timestamp;
	int pid;
	int id;
	Event successor;
	
	Event() {
		timestamp = new HashMap<>();
		pid = 0;
		value = 0;
		successor = null;
	}
	
	@Override
	public boolean equals (Object obj) {
		Event e = (Event)obj;
		if (e == null) {
			return false;
		}
		return id == e.id;
	}
	
	@Override
	public int hashCode () {
		return this.id;
	}
	
	boolean happenedBefore(Event e) {
		boolean oneSmaller = false;
		for (int t : this.timestamp.keySet()) {
			if (this.timestamp.get(t) > e.timestamp.get(t)) {
				return false;
			}
			if (this.timestamp.get(t) < e.timestamp.get(t)) {
				oneSmaller = true;
			}
		}
		if (oneSmaller) {
			return true;
		}
		return false;
	}
}
