package OnlineSlicing;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class SlicingThread implements Runnable{
	
	public static Map<String, Map<Integer, Integer>> InMap;
	public static int mode = -1;
	public static Token token;
	public static int ChannelState = -1;
	public static int ChannelTarget = -1;
	
	private void ReceiveEvent(Map<String, Map<Integer, Integer>> InMap, int pid, int eid){
		//save eid localstates in local stateMap
		//System.out.println("Received Event");
		ArrayList<Integer> event = new ArrayList<>();
		event.add(InMap.get("data").get(pid));
		event.add(ChannelState);
		event.add(ChannelTarget);
		Adder.localstate.put(eid, event);
		Adder.localstateV.put(eid, Adder.VectorClock);
		int ndex = 0;
		while(ndex < Adder.tokenList.size()){
			Token t = Adder.tokenList.get(ndex);
			//System.out.println("target for token "+ndex);
			if (t.target.size() == 2 && t.target.get(1).equals(Adder.VectorClock.get(t.target.get(0)))){
				AddEventToToken(t, InMap.get("VectorClock"), pid, eid);
				ProcessToken(t);
			}
			ndex += 1;
		}
	}
	
	private void AddEventToToken(Token t, Map<Integer, Integer> InV, int pid, int eid){	
		//System.out.println("add event to token");
		t.gstate.put(t.target.get(0), Adder.localstate.get(eid));
		t.gcut.put(pid, eid);
		t.eid = eid;
		if (t.pid == Adder.myID){
			//update my gcut here
			for(int c: t.gcut.keySet()){
				t.gcut.put(c, Math.max(t.gcut.get(c), Adder.VectorClock.get(c))); 
			}
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
				Adder.tokenList.remove(t);
			}
		}
		EvaluateToken(t);
	}
	
	private void EvaluateToken(Token t){
		ArrayList<Integer> temp = Predicate.checkACempty(t.gstate);
		if (temp.get(0) == 0){
			t.eval = true;
			if (t.pid == Adder.myID){
				output(t.pid, t.eid, t.gcut);
				ArrayList<Integer> target = new ArrayList<>();
				target.add(Adder.myID);
				target.add(t.gcut.get(Adder.myID)+1);
				t.target = target;
			}
			else{
				send(t, t.pid);
				Adder.tokenList.remove(t);
			}
		} else {
			//System.out.println("Not True");
			t.eval = false;
			ArrayList<Integer> e = new ArrayList<>();
			e.add(temp.get(1));
			e.add(t.gcut.get(temp.get(1)));
			t.target = e;
			//System.out.println(temp.get(2));
			send(t, temp.get(2));
			Adder.tokenList.remove(t);
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
	
	public void ReceiveStopSignal(){
		for (Token t: Adder.tokenList){
			if(t.pid != Adder.myID){
				send(t, t.pid);
				Adder.tokenList.remove(t);
			}
		}
	}
	
	private void send(Token t, int pid) {
		Monitor.incrementCount();
		Monitor.TMemory();
		// TODO Auto-generated method stub
		try {
			Socket clientSocket = new Socket(Adder.serverMap.get(pid).get(0),Integer.parseInt(Adder.serverMap.get(pid).get(1)));
			OutputStream toServer = new DataOutputStream(clientSocket.getOutputStream()); // OutputStream where to send the map in case of network you get it from the Socket instance.
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			ObjectOutputStream myClock = new ObjectOutputStream(toServer);
		    myClock.writeInt(1);
		    myClock.flush();
			if(fromServer.readLine().equals("OK")){
				//System.out.println("Now send the real stuff");
			} else {
				clientSocket.close();
				System.out.println("Other processes are being funny!");
			}		    
		    myClock.writeObject(t);
		    myClock.flush();
			if(fromServer.readLine().equals("OK")){
				clientSocket.close();
				//System.out.println("Successfully sent");
			} else {
				clientSocket.close();
				System.out.println("Other processes are being funny!");
			}
		} catch (IOException e){
			System.err.println(e);
		}
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
		//System.out.println("Slicing Running");
		if (mode == 0){ //received an event
			int pid = (int)InMap.get("data").keySet().toArray()[0];
			int eid = InMap.get("VectorClock").get(pid);
			ReceiveEvent(InMap, pid, eid);
		} 
		if (mode == 1){ //received a token
			ReceiveToken(token);
		}
	}

}
