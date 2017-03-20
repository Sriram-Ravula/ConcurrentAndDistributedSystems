import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;

public class LamportClient extends Thread{
	boolean f;
	public LamportClient(boolean first){
		f=first;
	}
	public void run(){
		while(true){
			while(!Server.wantCS);
			Server.clock.tick();
			if(f){
				for(int i = 0;i<Server.addresses.size();i++){
					if(i!=Server.ID){
						LamportClientThread t = new LamportClientThread(Server.addresses.get(i), Server.ports.get(i), i);
						String request = String.valueOf(Server.clock.getClock()) + " " + String.valueOf(Server.ID) + " " + Server.message;
						Server.lamport.add(request);
						t.start();
					}
				}
				f=false;
			}
			else{
				for(int i = 0;i<Server.addresses.size();i++){
					Server.request.set(i, true);
					String request = String.valueOf(Server.clock.getClock()) + " " + String.valueOf(Server.ID) + " " + Server.message;
					Server.lamport.add(request);
				}
				
			}
			Server.wantCS = false;
		}
	}
    private synchronized static void requestCS(){
    	Server.clock.tick();
    	String request = String.valueOf(Server.clock.getClock()) + " " + String.valueOf(Server.ID) + " " + Server.message;
    	printStream.println(request);
    	Server.lamport.add(request);	
    }
}