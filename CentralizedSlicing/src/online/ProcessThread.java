package online;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ProcessThread implements Runnable {
	Socket client;
	
	public ProcessThread(Socket client){
		this.client = client;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			handleClient();
		} catch (IOException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			this.client.close();
		} catch (IOException e){
			System.err.println(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void handleClient() throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		InputStream fromServer = this.client.getInputStream(); // InputStream from where to receive the map, in case of network you get it from the Socket instance.
	    ObjectInputStream mapInputStream = new ObjectInputStream(fromServer);
		Map<String, Map<Integer, Integer>> InMap = (Map<String, Map<Integer, Integer>>) mapInputStream.readObject();
		Adder.updateClock(InMap.get("VectorClock"), (int)InMap.get("data").keySet().toArray()[0]);
		Adder.incrementCount((int)InMap.get("data").values().toArray()[0]);
		DataOutputStream toServer = new DataOutputStream(this.client.getOutputStream());
		toServer.writeBytes("OK\n");
		
		Map<String, Map<Integer, Integer>> OutMap = new HashMap<>();
		Map<Integer, Integer> data = new HashMap<>();
		data.put(Adder.myID, Adder.current_num);
		OutMap.put("VectorClock", Adder.VectorClock);
		OutMap.put("data", data);
		Socket slicerSocket = new Socket("localhost", 2015);
		OutputStream toSlicer = new DataOutputStream(slicerSocket.getOutputStream());
	    ObjectOutputStream myClock = new ObjectOutputStream(toServer);
	    myClock = new ObjectOutputStream(toSlicer);
	    myClock.writeObject(OutMap);
	    BufferedReader fromSlicer = new BufferedReader(new InputStreamReader(slicerSocket.getInputStream()));
	    if (fromSlicer.readLine().equals("OK")) {
	    	slicerSocket.close();
	    } else {
	    	slicerSocket.close();
	    	System.out.println("Something happened with the slicer!");
	    }
	}
}