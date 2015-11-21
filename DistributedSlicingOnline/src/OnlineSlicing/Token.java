package OnlineSlicing;

import java.util.ArrayList;
import java.util.Map;

public class Token {
	public Map<Integer, ArrayList<Integer>> gstate = null;
	public Map<Integer, Integer> gcut = null;
	public Map<Integer, Integer> depend = null;
	public int pid = -1;
	public int eid = -1;
	public ArrayList<Integer> event = new ArrayList<Integer>();
	public ArrayList<Integer> target = new ArrayList<Integer>();
	public boolean eval = false;
}
