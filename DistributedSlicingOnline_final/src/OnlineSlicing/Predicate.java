package OnlineSlicing;

import java.util.ArrayList;
import java.util.Map;

public class Predicate {
	//Check the all channel is empty 
	public static ArrayList<Integer> checkACempty(Map<Integer, ArrayList<Integer>> gstate) {
		ArrayList<Integer> temp = new ArrayList<>();
		for(int pid: gstate.keySet()){
			if (gstate.get(pid).get(1) == 1) {
				temp.add(1);
				temp.add(pid);
				temp.add(gstate.get(pid).get(2));
				return temp;
			}
		}
		temp.add(0);
		temp.add(0);
		return temp;
	}
	
}
