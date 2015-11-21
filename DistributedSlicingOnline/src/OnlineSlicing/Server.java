package OnlineSlicing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
	ServerSocket server;
	int port;
	int quit;
	
	public Server (String hostname, int port) {
		this.port = port;
	}
	
	public void close(){
		this.quit = 1;
		try{
			this.server.close();
		} catch (IOException e){
			System.err.println(e);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.quit = 0;
		try{
			this.server = new ServerSocket(this.port);//just use the port here since its local
			while (true) {
				if (this.quit == 1){
					break;
				}
				Socket client = server.accept();
				new Thread(
					new ServerThread(client)
					).start();
			}
		} catch (IOException e) {
			//System.err.println(e);
		}
	}
}
