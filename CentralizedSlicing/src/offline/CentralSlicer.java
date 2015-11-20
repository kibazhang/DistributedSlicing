package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CentralSlicer {
	
	static Computation E = new Computation();
	
	static void addEvent(Event e) {
		E.add(e, true);
	}
	
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
	 * C = initial values
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
	static Map<Event,Computation> ComputeJ (int pid) {
		Computation C = new Computation(E);
		Map<Event,Computation> JB = new HashMap<>();
		for (Event e : E.events) {
			if (e.pid == pid) {
				boolean done = false;
				if (C.equals(E)) {
					done = true;
				}
				while (!done) {
					Computation front = C.frontier(E);
					if (front.getInconsistentEvent().pid != -1) {
						Event f = front.getInconsistentEvent();
						C.add(E.succ(f), false);
					} else {
						if (C.equals(E) || C.satisfies(E)) {
							done = true;
						} else {
							Event f = C.forbidden(E);
							C.add(E.succ(f), false);
						}
					}
				}
				JB.put(e,C);
			}
		}
		return JB;
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
	
	static Map<Event,Event>[] ComputeF (Map<Event,Computation>[] JB, int pid) {
		Map<Event, Event>[] FB = new HashMap[4];
		for (int i=1; i<=3; i++) {
			Event f = E.initial[i];
			FB[i] = new HashMap<Event, Event>();
			for (Event e : E.events) {
				if (e.pid == pid) {
					while (!JB[i].get(f).events.contains(e) && !E.recent[i].equals(f)) {
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
	
	static Computation SliceForRegular() {
		Map<Event, Computation>[] JB = new HashMap[4];
		Map<Event, Event>[][] FB = new HashMap[4][4];
		for (int pid=1; pid<=3; pid++) {
			JB[pid] = ComputeJ(pid);
		}
		for (int pid=1; pid<=3; pid++) {
			FB[pid] = ComputeF(JB,pid);
		}
		for (int pid=1; pid<=3; pid++) {
			for (int j=1; j<=3; j++) {
				for (Event e : FB[pid][j].keySet()) {
					System.out.println(e.id + " " + e.value + ", " + FB[pid][j].get(e).id + " " + FB[pid][j].get(e).value);
				}
				System.out.println("\n");
			}
		}
		Computation SB = new Computation();
		return SB;
	}
	
	@SuppressWarnings("resource")
	public static void main (String[] args) {
		try{
			int uid = 0;
			for (int id=1; id<4; id++) {
				BufferedReader input = new BufferedReader(new FileReader("C:\\Users\\Erik\\Documents\\GitHub\\DistributedSlicing\\CentralizedSlicing\\src\\output" + id + ".txt"));
				String line;
				Event e = new Event();
				e.pid = id;
				while((line = input.readLine()) != null){
					if (line.contains(",")) {
						String[] split = line.split(",");
						e.timestamp.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
					} else if (line.equals("")){
						e.id = uid++;
						addEvent(e);
						e = new Event();
						e.pid = id;
					} else {
						e.value = Integer.parseInt(line);
					}
				}
			}
		} catch (IOException ie) {
			System.err.println(ie);
		}
		SliceForRegular();
	}
}
