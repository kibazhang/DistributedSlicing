package online;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import offline.Monitor;

public class CentralSlicer {
	
	static Computation E = new Computation();
	static Map<Event, Computation>[] JB = new HashMap[4];
	static Map<Event, Event>[][] FB = new HashMap[4][4];
	static int uid = 0;
	
	static void addEvent(Event e) {
		E.add(e, true);
	}
	
	static void ComputeJ (int pid, Event newEvent) {
		Computation C = new Computation(E);
		ArrayList<Event> eventsToCheck = new ArrayList<>();
		if (JB[pid] == null)
			JB[pid] = new HashMap<Event, Computation>();
		for (Event e : E.events) {
			if (JB[pid].containsKey(e)) {
				Computation cut = JB[pid].get(e);
				if (cut.events.get(newEvent.pid).id == newEvent.id) {
					eventsToCheck.add(e);
				}
			} else {
				eventsToCheck.add(e);
			}
		}
		for (Event e : eventsToCheck) {
			if (e.pid == pid) {
				if (JB[pid].containsKey(e)) {
					C.events = JB[pid].get(e).events;
				}
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
				JB[pid].put(e,C.frontier(E));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	static void ComputeF (int pid, Event newEvent) {
		ArrayList<Event> eventsToCheck = new ArrayList<>();
		for (int i=0; i<=3; i++) {
			for (Event e : E.events) {
				if (FB[pid][i].containsKey(e)) {
					if (FB[pid][i].get(e).id == newEvent.id) {
						eventsToCheck.add(e);
					}
				} else {
					eventsToCheck.add(e);
				}
			}
		}
		for (int i=1; i<=3; i++) {
			Event f;
			if (FB[pid][i] == null)
				FB[pid][i] = new HashMap<Event, Event>();
			for (Event e : eventsToCheck) {
				if (e.pid == pid) {
					if (FB[pid][i].containsKey(e)) {
						f = FB[pid][i].get(e);
					} else {
						f = E.succ(E.initial[i]);
					}
					while (((Computation)JB[i].get(f)).events.get(e.pid-1).id < e.id && !E.recent[i].equals(f)) {
						f = E.succ(f);
					}
					if (!E.recent[i].equals(f) && e.equals(f)) {
						f = E.succ(f);
					}
					FB[pid][i].put(e,f);
				}
			}
		}
	}
	
	static Computation ComputeSlice(Event e) {
		for (int pid=1; pid<=3; pid++) {
			ComputeJ(pid, e);
		}
		for (int pid=1; pid<=3; pid++) {
			ComputeF(pid, e);
		}
		ArrayList<Integer> ignore = new ArrayList<>();
		ArrayList<String> temp = new ArrayList<>();
		for (int pid=1; pid<=3; pid++) {
			for (int j=1; j<=3; j++) {
				for (Event ev : FB[pid][j].keySet()) {
					if (JB[pid].get(ev).satisfies(E, ev) && !E.initial[pid].equals(ev)) {
						temp.add(ev.id + "," + FB[pid][j].get(ev).id);
					} else if (!ignore.contains(ev.id)){
						ignore.add(ev.id);
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
		ServerSocket listener;
		try {
			listener = new ServerSocket(2015);
			while (true) {
				Socket client = listener.accept();
				InputStream fromProcess = client.getInputStream(); // InputStream from where to receive the map, in case of network you get it from the Socket instance.
			    ObjectInputStream mapInputStream = new ObjectInputStream(fromProcess);
				Map<String, Map<Integer, Integer>> InMap = (Map<String, Map<Integer, Integer>>) mapInputStream.readObject();
				Event newEvent = new Event();
				newEvent.timestamp = InMap.get("VectorClock");
				newEvent.value = (int)InMap.get("data").values().toArray()[0];
				newEvent.id = uid++;
				newEvent.pid = (int)InMap.get("data").keySet().toArray()[0];
				Monitor.incrementCount();
				Monitor.EMemory();
				addEvent(newEvent);
				ComputeSlice(newEvent);
				Monitor.printMemory();
				Monitor.printMsgCount();
				newEvent = new Event();
				DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
				toServer.writeBytes("OK\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
