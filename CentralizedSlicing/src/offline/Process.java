package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Process {

	int pid;
	//Send event and local state values whenever local state changes
	
	void readEvents(int id) {
		try{
			BufferedReader input = new BufferedReader(new FileReader("output" + id + ".txt"));
			String line;
			Event e = new Event();
			e.pid = id;
			CentralSlicer CS = new CentralSlicer();
			while((line = input.readLine()) != null){
				if (line.contains(",")) {
					String[] split = line.split(",");
					e.timestamp.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				} else if (line.equals("\n")){
					CS.addEvent(e);
					e = new Event();
					e.pid = id;
				} else {
					e.value = Integer.parseInt(line);
				}
			} 
		} catch (IOException ie) {
			System.err.println(ie);
		}
	}
	
	public void main (String[] args) {
		Map<Integer, ArrayList<String>> serverMap = new HashMap<>();
		readEvents(Integer.parseInt(args[1])-1);
	}
}
