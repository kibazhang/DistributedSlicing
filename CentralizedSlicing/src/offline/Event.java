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
		if (value == e.value) {
			if (timestamp == e.timestamp) {
				if (pid == e.pid) {
					return true;
				}
			}
		}
		return false;
	}
	
	boolean happenedBefore(Event e) {
		boolean oneSmaller = false;
		for (int t : this.timestamp.keySet()) {
			for (int te : e.timestamp.keySet()) {
				if (this.timestamp.get(t) > e.timestamp.get(te)) {
					return false;
				}
				if (this.timestamp.get(t) < e.timestamp.get(te)) {
					oneSmaller = true;
				}
			}
		}
		if (oneSmaller) {
			return true;
		}
		return false;
	}
}
