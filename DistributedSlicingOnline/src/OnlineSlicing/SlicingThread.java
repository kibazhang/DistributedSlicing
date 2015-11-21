package OnlineSlicing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SlicingThread implements Runnable{
	
	public static Map<String, Map<Integer, Integer>> InMap;
	public static int mode = -1;
	public static Token token;
	public static int ChannelState = -1;
	
	private void ReceiveEvent(Map<String, Map<Integer, Integer>> InMap, int pid, int eid){
		//save eid localstates in local stateMap
		ArrayList<Integer> event = new ArrayList<>();
		event.add(InMap.get("data").get(pid));
		event.add(ChannelState);
		Adder.localstate.put(eid, event);
		Adder.localstateV.put(eid, Adder.VectorClock);
		for(Token t: Adder.tokenList){
			if (t.target.isEmpty() != true && t.target.get(0) == pid && t.target.get(1) == eid){
				AddEventToToken(t, InMap.get("VectorClock"), pid, eid);
				ProcessToken(t);
			}
		}
	}
	
	private void AddEventToToken(Token t, Map<Integer, Integer> InV, int pid, int eid){		
		t.gstate.put(pid, Adder.localstate.get(eid));
		t.gcut.put(pid, eid);
		if (t.pid == Adder.myID){
			if (t.event.isEmpty() == true){
				ArrayList<Integer> e = new ArrayList<>();
				e.add(pid);
				e.add(eid);
				t.event = e;
			} else {
				t.event.set(0, pid);
				t.event.set(1, eid);
			}
		}
		for(int c: t.depend.keySet()){
			t.depend.put(c, Math.max(t.depend.get(c), InV.get(c))); 
		}
	}
	
	private void ProcessToken(Token t){
		for(int c: t.gcut.keySet()){
			if(t.gcut.get(c) < t.depend.get(c)){
				if(t.target.isEmpty() == true){
					ArrayList<Integer> e = new ArrayList<>();
					e.add(c);
					e.add(t.gcut.get(c)+1);
					t.target = e;
				} else {
					t.event.set(0, c);
					t.event.set(1, t.gcut.get(c)+1);
				}
				send(t, c);
			}
		}
		EvaluateToken(t);
	}
	
	private void EvaluateToken(Token t){
		ArrayList<Integer> temp = Predicate.checkACempty(t.gstate);
		if (temp.get(0) == 0){
			t.eval = true;
			send(t, t.pid);
		} else {
			t.eval = false;
			ArrayList<Integer> e = new ArrayList<>();
			e.add(temp.get(1));
			e.add(t.gcut.get(temp.get(1))+1);
			t.target = e;
			send(t, temp.get(1));
		}
	}
	
	private void ReceiveToken(Token t){
		if (t.eval == true && t.pid == Adder.myID){
			output(t.pid, t.eid, t.gcut);
			ArrayList<Integer> e = new ArrayList<>();
			e.add(Adder.myID);
			e.add(t.gcut.get(Adder.myID)+1);
			t.target = e;
		} else {
			ArrayList<Integer> ne = t.target;
			if(Adder.localstate.keySet().contains(ne.get(1))){
				AddEventToToken(t, Adder.localstateV.get(ne.get(1)), ne.get(0), ne.get(1));
				EvaluateToken(t);
			}
		}
	}
	
	private void ReceiveStopSignal(){
		for (Token t: Adder.tokenList){
			if(t.pid != Adder.myID){
				send(t, t.pid);
			}
		}
	}
	
	private void send(Token t, int pid) {
		// TODO Auto-generated method stub
		
	}

	private void output(int pid, int eid, Map<Integer, Integer> gcut) {
		// TODO Auto-generated method stub
		System.out.println("PID: "+pid);
		System.out.println("EID: "+eid);
		System.out.println("The global cut is:");
		for (int i: gcut.keySet()){
			System.out.println(i+","+gcut.get(i));
		}
		System.out.println();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mode == 0){
			int pid = (int)InMap.get("data").keySet().toArray()[0];
			int eid = InMap.get("VectorClock").get(pid);
			ReceiveEvent(InMap, pid, eid);
		} 
		if (mode == 1){
			ReceiveToken(token);
		}
	}

}
