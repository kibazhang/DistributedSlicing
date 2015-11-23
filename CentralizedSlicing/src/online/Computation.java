package online;

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
		Computation front = this.frontier(E);
		int count = 0;
		for (Event e : front.events) {
			if (e.equals(E.recent[e.pid])) {
				count++;
			}
		}
		return count == 3;
		/*
		if (E.events.size() == events.size()) {
			for (Event e : events) {
				if (!E.events.contains(e)) {
					return false;
				}
			}
			return true;
		}
		return false;
		*/
	}
	
	Computation frontier(Computation E) {
		Computation front = new Computation();
		for (Event e : E.events) {
			if (E.recent[e.pid].equals(e)) {
				if (events.contains(e)) {
					front.add(e,false);
				}
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
						e.successor = f.successor;
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
				recent[pid].id++;
				int v = recent[pid].timestamp.get(e.pid);
				recent[pid].timestamp.put(e.pid, v+1);
				e.successor = recent[pid];
			} else {
				for (Event ev : events) {
					if (ev.successor.equals(recent[pid])) {
						ev.successor = e;
						events.remove(recent[pid]);
					}
				}
				e.id = recent[pid].id;
				recent[pid] = e;
				recent[pid].id++;
				int v = recent[pid].timestamp.get(e.pid);
				recent[pid].timestamp.put(e.pid, v+1);
				e.successor = recent[pid];
			}
		}
		events.add(e);
		events.add(recent[e.pid]);
	}
	
	//b = (x1 >= 1) and (x3 <= 3)
	boolean satisfies (Computation E, Event e) {
		Computation front = frontier(E);
		int sat = 0;
		for (Event ev : front.events) {
			if (ev.pid == e.pid) {
				if (ev.id < e.id) {
					return false;
				}
			}
			if (ev.pid == 1) {
				if (ev.value >= 1) {
					sat++;
				}
			}
			if (ev.pid == 3) {
				if (ev.value <= 3) {
					sat++;
				}
			}
		}
		if (sat == 2) {
			return true;
		}
		return false;
	}
	
	Event forbidden(Computation E, Event e) {
		Computation front = frontier(E);
		for (Event ev : front.events) {
			if (ev.pid == e.pid) {
				if (ev.id < e.id) {
					return ev;
				}
			}
			if (ev.pid == 1) {
				if (ev.value < 1) {
					return ev;
				}
			}
			if (ev.pid == 3) {
				if (ev.value > 3) {
					return ev;
				}
			}
		}
		return null;
	}
	
	Event succ(Event f) {
		//if (f.successor == null) {
		//	return f;
		//}
		return f.successor;
	}
}