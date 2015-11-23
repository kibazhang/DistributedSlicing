package OnlineSlicing;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable {
	Socket client;
	
	public ServerThread(Socket client){
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
	    DataOutputStream toServer = new DataOutputStream(this.client.getOutputStream());
		ObjectInputStream InputStream = new ObjectInputStream(fromServer);
	    Map<String, Map<Integer, Integer>> InMap = new HashMap<String, Map<Integer, Integer>>();
	    Token t = new Token();
	    int type = InputStream.readInt();
		//System.out.println("sending the ack");
		toServer.writeBytes("OK\n");
		if (type == 0){
    		//System.out.println("Try to decode the map object");
	    	InMap = (Map<String, Map<Integer, Integer>>) InputStream.readObject();
    		//System.out.println("Have decoded the map object");
		} else {
	    	//System.out.println("Try to decode the token object");
    		t = (Token) InputStream.readObject();
    		//System.out.println("Have decoded the token object");
    		Adder.tokenList.add(t);
		}
		//System.out.println("sending the ack");
		toServer.writeBytes("OK\n");
	    if (type == 0){
	    	Adder.updateClock(InMap.get("VectorClock"), (int)InMap.get("data").keySet().toArray()[0]);
			Adder.incrementCount((int)InMap.get("data").values().toArray()[0]);
			Map<String, Map<Integer, Integer>> NewMap = new HashMap<>();
			Map<Integer, Integer> data = new HashMap<>();
			data.put(Adder.myID, Adder.current_num);
			NewMap.put("VectorClock", Adder.VectorClock);
			NewMap.put("data", data);
			SlicingThread.InMap = NewMap;
			SlicingThread.mode = 0;
			Adder.slicing.run();
	    } else {
	    	SlicingThread.token = t;
			SlicingThread.mode = 1;
			Adder.slicing.run();
	    }	
	}
}
