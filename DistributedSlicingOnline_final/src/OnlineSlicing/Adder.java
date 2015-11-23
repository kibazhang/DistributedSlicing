package OnlineSlicing;

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
	public static int current_num = 0;
	private static final Object countLock = new Object();
	private static PrintWriter writer;
	public static Map<Integer, Integer> VectorClock = new HashMap<>();
	public static int myID;
	public static Token myToken = new Token();
	public static ArrayList<Token> tokenList = new ArrayList<>();
	public static Map<Integer, ArrayList<Integer>> localstate = new HashMap<>();
	public static Map<Integer, Map<Integer, Integer>> localstateV = new HashMap<>();
	public static SlicingThread slicing;
	public static Map<Integer, ArrayList<String>> serverMap = new HashMap<>();
	
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
	
	public static void main (String[] args) throws InterruptedException {
		myID = Integer.parseInt(args[1])-1;
		myToken.pid = myID;
		ArrayList<Integer> target = new ArrayList<>();
		target.add(myID);
		target.add(1);
		myToken.target = target;
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
					host.add(server);
					host.add(port);
					serverMap.put(ndex, host);
					VectorClock.put(ndex, 0);
					ArrayList<Integer> state = new ArrayList<>();
					//eid
					state.add(0);
					//counter
					state.add(0);
					//channel state
					state.add(0);
					myToken.gstate.put(ndex, state);
					myToken.gcut.put(ndex, 0);
					myToken.depend.put(ndex, 0);
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
		tokenList.add(myToken);
		System.out.println("Program started");
		//Monitor.initCpuUsage();
		Server dataserver;
		new Thread(
				dataserver = new Server(serverMap.get(Integer.parseInt(args[1])-1).get(0),Integer.parseInt(serverMap.get(Integer.parseInt(args[1])-1).get(1)))
				).start();
		new Thread(
				slicing = new SlicingThread()
				).start();
		System.out.println("Usage: adding number, send result to input processor(optional)");
		try {			
			writer = new PrintWriter("output"+args[1]+".txt", "UTF-8");
	        writer.write(current_num+"\n");
	        writer.flush();
	        for (int process: VectorClock.keySet()){
	        	writer.write(process+","+VectorClock.get(process)+"\n");
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
					SlicingThread.InMap = OutMap;
					SlicingThread.mode = 0;
					Monitor.incrementCount();
					Monitor.EMemory();
					if (st.length == 1){
						SlicingThread.ChannelState = 0;
						slicing.run();
					}
					else{
						SlicingThread.ChannelState = 1;
						SlicingThread.ChannelTarget = Integer.parseInt(st[1])-1;
						slicing.run();
						try {
							Socket clientSocket = new Socket(serverMap.get(Integer.parseInt(st[1])-1).get(0),Integer.parseInt(serverMap.get(Integer.parseInt(st[1])-1).get(1)));
							OutputStream toServer = new DataOutputStream(clientSocket.getOutputStream()); // OutputStream where to send the map in case of network you get it from the Socket instance.
							BufferedReader fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
							ObjectOutputStream myClock = new ObjectOutputStream(toServer);
						    myClock.writeInt(0);
						    myClock.flush();
							if(fromServer.readLine().equals("OK")){
								//System.out.println("Now send the real stuff");
							} else {
								clientSocket.close();
								System.out.println("Other processes are being funny!");
							}
						    myClock.writeObject(OutMap);
						    myClock.flush();
							if(fromServer.readLine().equals("OK")){
								clientSocket.close();
								SlicingThread.ChannelState = 0;
							} else {
								clientSocket.close();
								System.out.println("Other processes are being funny!");
								SlicingThread.ChannelState = 0;
							}
						} catch (IOException e){
							System.err.println(e);
						}
					}
				}

			}
			System.err.println("Program closed");
			Monitor.printMsgCount();
			Monitor.printMemory();
			//Monitor.getCpuUsage();
			dataserver.close();
			writer.close();
		} catch (IOException e) {
			System.out.println("IOException main");
			System.err.println("Program aborted:" + e);
		}
	}

}
