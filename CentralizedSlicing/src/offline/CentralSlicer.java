package offline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CentralSlicer {
	/*
		foreach event e in computation:
			find the least consistent cut that satisfies B and includes e
			
		One process acts as the central slicer - CS
		Each process Pi sends details (state/vector clock etc.) of relevant events to CS
	*/

	//online - maintain queue of other process events. When data is received, add it to the queue.
	//	For each event calculate JB(e) using the linearity property
	
	/*
	 * ComputeJ
	 * 
	 * Input: computation (E,->), regular predicate b
	 * Output: JB(e) for each event e
	 * 
	 * C = empty
	 * for each event e
	 * 	done = false
	 * 	if C = E then done = true
	 * 	while !done:
	 * 		if there exists events f and g in frontier(C) such that 
	 * 			succ(f) -> g then //C is not a consistent cut
	 * 			C = C union succ(f) //advance beyond f to next event on this process
	 * 		else
	 * 			if (C=E) or (C staisfies bc) then
	 * 				done = true
	 * 			else
	 * 				f = forbidden(bc,C) //invoke the linearity property
	 * 				C = C union succ(f) //advance beyond f
	 * 	JB(e) = C
	 */
	Map<Event,Computation> ComputeJ (Computation E, String b, int pid) {
		Computation C = new Computation();
		Map<Event,Computation> JB = new HashMap<>();
		for (Event e : E.events) {
			if (e.pid == pid) {
				boolean done = false;
				if (C.equals(E)) {
					done = true;
				}
				while (!done) {
					Computation front = C.frontier(E);
					if (front.getInconsistentEvent().pid == -1) {
						Event f = front.getInconsistentEvent();
						C.add(E.succ(f), false);
					} else {
						if (C.equals(E) || C.satisfies(b)) {
							done = true;
						} else {
							Event f = C.forbidden(b);
							C.add(E.succ(f), false);
						}
					}
				}
				JB.put(e,C);
			}
		}
		return JB;
	}
	
	class Computation {
		ArrayList<Event> events;
		Event recent[] = new Event[2];
		
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
				if (events.contains(e) && !events.contains(succ(e))) {
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
					if (succ(f).timestamp < g.timestamp) {
						e.pid = f.pid;
						e.timestamp = f.timestamp;
						e.value = f.value;
					}
				}
			}
			return e;
		}
		
		void add(Event e, boolean setSucc) {
			if (setSucc) {
				int pid = e.pid;
				if (recent[pid] == null || recent[pid] == new Event()) {
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
		
		boolean satisfies (String b) {
			return false;
		}
		
		Event forbidden(String b) {
			return new Event();
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
					if (first.timestamp < e.timestamp) {
						first = e;
					}
				}
			}
			return first;
		}
	}
	
	class Event {
		int value;
		int timestamp;
		int pid;
		Event successor;
		
		@Override
		public boolean equals (Object obj) {
			Event e = (Event)obj;
			if (value == e.value) {
				if (timestamp == e.timestamp) {
					if (pid == e.pid) {
						return true;
					}
				}
			}
			return false;
		}
	}
	/*
	 * ComputeF
	 * 
	 * Input: computation (E,->), JB(e)
	 * Output: FB(e) for each event
	 * 
	 * for each process in the system
	 * 	f = empty
	 * 	for each event in this process //visited in order given by ->p
	 * 		while JB(e) is not a subset of JB(f) //check if e is in JB(f)
	 * 			f = succ(f)
	 * 		FB(e)[i] = f
	 */
	
	Map<Event,Event>[] ComputeF (Computation E, Map<Event,Computation>[] JB, int pid) {
		Map<Event, Event>[] FB = new HashMap[3];
		for (int i=0; i<3; i++) {
			Event f = E.first(i);
			for (Event e : E.events) {
				if (e.pid == pid) {
					while (!JB[i].get(f).events.contains(e)) {
						f = E.succ(f);
					}
					FB[i].put(e,f);
				}
			}
		}
		return FB;
	}
	
	/*
	 * SliceForRegular
	 * 
	 * Input: computation (E,->), regular predicate b
	 * Output: slice (E,->)b
	 * 
	 * compute JB(e) for each event e using ComputeJ
	 * compute JB(e) for each event e using ComputeF
	 * construct SB(E) the skeletal representation of (E,->)b
	 * output SB(E)
	 */
	
	Computation SliceForRegular(Computation E, String b) {
		Map<Event, Computation>[] JB = null;
		Map<Event, Event>[][] FB = null;
		for (int pid=0; pid<3; pid++) {
			JB[pid] = ComputeJ(E,b,pid);
		}
		for (int pid=0; pid<3; pid++) {
			FB[pid] = ComputeF(E,JB,pid);
		}
		Computation SB = new Computation();
		return SB;
	}
}
