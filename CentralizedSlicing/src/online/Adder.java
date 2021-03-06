package online;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Adder {
	
	//current number
	static int current_num = 0;
	private static final Object countLock = new Object();
	private static PrintWriter writer;
	public static Map<Integer, Integer> VectorClock = new HashMap<>();
	public static int myID;
	public static Map<Integer, ArrayList<Integer>> localstate = new HashMap<>();
	public static Map<Integer, Map<Integer, Integer>> localstateV = new HashMap<>();
	public static String centralSlicerHostname = "";
	public static int centralSlicerPort = 0;
	
	public static void updateClock(Map<Integer, Integer> clock, int pi){
		for(int i : clock.keySet()){
			if (i == pi) {
				VectorClock.put(i, clock.get(i));
			} else {
				VectorClock.put(i, Math.max(VectorClock.get(i), clock.get(i)));
			}
		}
	}
	
    public static void incrementCount(int num) {
        synchronized (countLock) {
            current_num += num;
        }
        VectorClock.put(myID, VectorClock.get(myID)+1);
        writer.write(current_num+"\n");
        writer.flush();
        for (int process: VectorClock.keySet()){
        	writer.write(process+","+VectorClock.get(process)+"\n");
        	writer.flush();
        }
    	writer.write("\n");
    	writer.flush();
    }
	
	public static void main (String[] args) {
		Map<Integer, ArrayList<String>> serverMap = new HashMap<>();
		if (args.length > 1)
			myID = Integer.parseInt(args[1]);
		else
			myID = 1;
		args = new String[2];
		args[1] = "1";
		args[0] = "C:\\Users\\Erik\\Documents\\GitHub\\DistributedSlicing\\DistributedSlicingOnline\\available_servers.txt";
		if(args.length > 0) {
			try{
				BufferedReader server_file = new BufferedReader(new FileReader(args[0]));
				String line;
				int ndex = 0;
				while((line = server_file.readLine()) != null){
					StringTokenizer st = new StringTokenizer(line);
					String server = st.nextToken();
					String port = st.nextToken();
					ArrayList<String> host = new ArrayList<String>();
					if (centralSlicerHostname.equals("")) {
						centralSlicerHostname = server;
						centralSlicerPort = Integer.parseInt(port);
					} else {
						host.add(server);
						host.add(port);
						serverMap.put(ndex, host);
						VectorClock.put(ndex, 0);
					}
					ndex += 1;
				} 
				server_file.close();
			} catch (IOException ie) {
				System.err.println(ie);
			}
		} else {
			System.err.println("Please provide me a file with available servers, names as \"available_servers.txt\".");
			System.exit(-1);
		}
		System.out.println("Program started");
		
		Process process;
		new Thread(
				process = new Process(serverMap.get(Integer.parseInt(args[1])).get(0),Integer.parseInt(serverMap.get(Integer.parseInt(args[1])).get(1)))
				).start();
		System.out.println("Usage: adding number, send result to input processor(optional)");
		try {			
			writer = new PrintWriter("output"+args[1]+".txt", "UTF-8");
	        writer.write(current_num+"\n");
	        writer.flush();
	        for (int proc : VectorClock.keySet()){
	        	writer.write(proc+","+VectorClock.get(proc)+"\n");
	        	writer.flush();
	        }
	    	writer.write("\n");
	    	writer.flush();
			while (true) {
				BufferedReader fromUser = new BufferedReader(new InputStreamReader(System.in));
				String user_request = fromUser.readLine();
				if(user_request.equals("quit")) {
					break;
				} else {
					String[] st = user_request.split(",");
					incrementCount(Integer.parseInt(st[0]));
					Map<String, Map<Integer, Integer>> OutMap = new HashMap<>();
					Map<Integer, Integer> data = new HashMap<>();
					data.put(myID, current_num);
					OutMap.put("VectorClock", VectorClock);
					OutMap.put("data", data);
					try {
						Socket clientSocket = new Socket(serverMap.get(Integer.parseInt(st[1])).get(0),Integer.parseInt(serverMap.get(Integer.parseInt(st[1])).get(1)));
						Socket slicerSocket = new Socket(centralSlicerHostname, centralSlicerPort);
						OutputStream toServer = new DataOutputStream(clientSocket.getOutputStream()); // OutputStream where to send the map in case of network you get it from the Socket instance.
						OutputStream toSlicer = new DataOutputStream(slicerSocket.getOutputStream());
					    ObjectOutputStream myClock = new ObjectOutputStream(toServer);
					    myClock.writeObject(OutMap);
						BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						if(fromServer.readLine().equals("OK")){
							clientSocket.close();
						} else {
							clientSocket.close();
							System.out.println("Other processes are being funny!");
						}
					    myClock = new ObjectOutputStream(toSlicer);
					    myClock.writeObject(OutMap);
					    BufferedReader fromSlicer = new BufferedReader(new InputStreamReader(slicerSocket.getInputStream()));
					    if (fromSlicer.readLine().equals("OK")) {
					    	slicerSocket.close();
					    } else {
					    	slicerSocket.close();
					    	System.out.println("Something happened with the slicer!");
					    }
					} catch (IOException e){
						System.err.println(e);
					}
				}

			}
			System.err.println("Program closed");
			process.close();
			writer.close();
		} catch (IOException e) {
			System.out.println("IOException main");
			System.err.println("Program aborted:" + e);
		}
	}

}
