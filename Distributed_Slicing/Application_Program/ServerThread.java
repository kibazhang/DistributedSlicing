package Application_Program;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
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
	    ObjectInputStream mapInputStream = new ObjectInputStream(fromServer);
		Map<String, Map<Integer, Integer>> InMap = (Map<String, Map<Integer, Integer>>) mapInputStream.readObject();
		Adder.updateClock(InMap.get("VectorClock"), (int)InMap.get("data").keySet().toArray()[0]);
		Adder.incrementCount((int)InMap.get("data").values().toArray()[0]);
		DataOutputStream toServer = new DataOutputStream(this.client.getOutputStream());
		toServer.writeBytes("OK\n");
	}
}
