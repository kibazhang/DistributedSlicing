package offline;

import java.util.ArrayList;

public class Computation {
	ArrayList<Event> events = new ArrayList<Event>();
	Event recent[] = new Event[4];
	Event initial[] = new Event[4];
	
	Computation () {
		events = new ArrayList<Event>();
		recent = new Event[4];
		initial = new Event[4];
	}
	
	Computation (Computation E) {
		for (int i=0; i<4; i++) {
			events.add(E.initial[i]);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		Computation E = (Computation) obj;
		if (E.events.size() == events.size()) {
			for (Event e : events) {
				if (!E.events.contains(e)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	Computation frontier(Computation E) {
		Computation front = new Computation();
		for (Event e : E.events) {
			if (E.recent[e.pid].equals(e)) {
				//front.add(e,false);
			} else if (events.contains(e) && !events.contains(succ(e))) {
				front.add(e, false);
			}
		}
		return front;
	}
	
	Event getInconsistentEvent() {
		Event e = new Event();
		e.pid = -1;
		for (Event g : events) {
			for (Event f : events) {
				if (succ(f) != null) {
					if (succ(f).happenedBefore(g)) {
						e.pid = f.pid;
						e.timestamp = f.timestamp;
						e.value = f.value;
						e.id = f.id;
					}
				}
			}
		}
		return e;
	}
	
	void add(Event e, boolean setSucc) {
		if (setSucc) {
			int pid = e.pid;
			if (initial[pid] == null) {
				initial[pid] = e;
			}
			if (recent[pid] == null) {
				recent[pid] = e;
			} else {
				for (Event ev : events) {
					if (ev.equals(recent[pid])) {
						ev.successor = e;
					}
				}
				recent[pid] = e;
			}
		}
		events.add(e);
	}
	
	//b = (x1 >= 1) and (x3 <= 3)
	boolean satisfies (Computation E) {
		Computation front = frontier(E);
		int sat = 0;
		for (Event e : front.events) {
			if (e.pid == 1) {
				if (e.value >= 1) {
					sat++;
				}
			}
			if (e.pid == 3) {
				if (e.value <= 3) {
					sat++;
				}
			}
		}
		if (sat == 2) {
			return true;
		}
		return false;
	}
	
	Event forbidden(Computation E) {
		Computation front = frontier(E);
		for (Event e : front.events) {
			if (e.pid == 1) {
				if (e.value < 1) {
					return e;
				}
			}
			if (e.pid == 3) {
				if (e.value > 3) {
					return e;
				}
			}
		}
		return null;
	}
	
	Event succ(Event f) {
		return f.successor;
	}
	
	Event first(int pid) {
		Event first = null;
		for (Event e : events) {
			if (e.pid == pid) {
				if (first == null) {
					first = e;
				}
				if (first.happenedBefore(e)) {
					first = e;
				}
			}
		}
		return first;
	}
}