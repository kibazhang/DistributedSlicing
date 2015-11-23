package OnlineSlicing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Token implements Serializable{
	public Map<Integer, ArrayList<Integer>> gstate = new HashMap<>();
	public Map<Integer, Integer> gcut = new HashMap<>();
	public Map<Integer, Integer> depend = new HashMap<>();
	public int pid = -1;
	public int eid = -1;
	public ArrayList<Integer> event = new ArrayList<Integer>();
	public ArrayList<Integer> target = new ArrayList<Integer>();
	public boolean eval = false;
}
