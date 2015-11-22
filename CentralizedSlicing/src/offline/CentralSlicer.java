package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
					boolean end = false;
					Computation front = C.frontier(E);
					if (front.getInconsistentEvent().pid != -1) {
						Event f = front.getInconsistentEvent();
						C.add(E.succ(f), false);
					} else {
						if (C.equals(E) || C.satisfies(E, e)) {
							done = true;
						} else {
							Event f = C.forbidden(E, e);
							if (E.recent[f.pid].equals(f)) {
								end = true;
							}
							C.add(E.succ(f), false);
						}
					}
					if (end) {
						done = true;
					}
				}
				JB.put(e,C.frontier(E));
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
	
	@SuppressWarnings("unchecked")
	static Map<Event,Event>[] ComputeF (Map<Event,Computation>[] JB, int pid) {
		Map<Event, Event>[] FB = new HashMap[4];
		for (int i=1; i<=3; i++) {
			Event f = E.succ(E.initial[i]);
			FB[i] = new HashMap<Event, Event>();
			for (Event e : E.events) {
				if (e.pid == pid) {
					while (((Computation)JB[i].get(f)).events.get(e.pid-1).id < e.id && !E.recent[i].equals(f)) {
						f = E.succ(f);
					}
					if (!E.recent[i].equals(f) && e.equals(f)) {
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
	
	static Computation ComputeSlice() {
		Map<Event, Computation>[] JB = new HashMap[4];
		Map<Event, Event>[][] FB = new HashMap[4][4];
		for (int pid=1; pid<=3; pid++) {
			JB[pid] = ComputeJ(pid);
		}
		for (int pid=1; pid<=3; pid++) {
			FB[pid] = ComputeF(JB,pid);
		}
		ArrayList<Integer> ignore = new ArrayList<>();
		ArrayList<String> temp = new ArrayList<>();
		for (int pid=1; pid<=3; pid++) {
			for (int j=1; j<=3; j++) {
				for (Event e : FB[pid][j].keySet()) {
					if (JB[pid].get(e).satisfies(E, e) && !E.initial[pid].equals(e)) {
						//System.out.println(e.id + " " + e.value + ", " + FB[pid][j].get(e).id + " " + FB[pid][j].get(e).value);
						//System.out.println(e.id + " -> " + FB[pid][j].get(e).id);
						temp.add(e.id + "," + FB[pid][j].get(e).id);
					} else if (!ignore.contains(e.id)){
						ignore.add(e.id);
					}
				}
			}
		}
		for (String i : temp) {
			String[] s = i.split(",");
			if (!ignore.contains(Integer.parseInt(s[1]))){ 
				System.out.println(s[0] + " -> " + s[1]);
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
				while((line = input.readLine()) != null){
					if (line.contains(",")) {
						String[] split = line.split(",");
						e.timestamp.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
					} else if (line.equals("")){
						e.id = uid++;
						e.pid = id;
						addEvent(e);
						e = new Event();
					} else {
						e.value = Integer.parseInt(line);
					}
				}
				e.id = uid++;
				e.pid = id;
				addEvent(e);
				e = new Event();
			}
		} catch (IOException ie) {
			System.err.println(ie);
		}
		ComputeSlice();
	}
}
