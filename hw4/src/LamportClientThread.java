import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;

public class LamportClientThread extends Thread{
	private Socket s;
	private InputStream input;
    private OutputStream output;
    private PrintStream printStream;
    private InputStreamReader inputStream;
    private BufferedReader bufferedReader;
    private String message = null;
    private boolean first=true;
    private int port;
    private String ip;
    private int sid;
    private boolean dead = false;
    public LamportClientThread(String serverIP, int portNumber, int serverID){
    	ip = serverIP;
    	port=portNumber;
    	sid=serverID;
    }
    public void run(){
    	System.out.println("Lamport Client Thread started on server " +sid);
    	while(!dead){
    		System.out.println("outputstream deadlock checkpoint 0 " + sid);
    		System.out.println(Server.request.get(sid));
    		while(!Server.request.get(sid) && !Server.release.get(sid));
    		System.out.println("outputstream deadlock checkpoint 0.5 " +sid);
    		System.out.println(Server.request.get(sid));
    		if(Server.request.get(sid)){
        		if(checkServer()){
        			System.out.println("outputstream deadlock checkpoint 1 " +sid);
        			requestCS();
            		Server.request.set(sid, false);
            		System.out.println("outputstream deadlock checkpoint 9 "+sid);
            		
        		}
        		else{
        			System.out.println("outputstream deadlock checkpoint 1.5" +sid);
        			Server.dead.set(sid, true);
        			Server.numAlive--;
        			dead=true;
        		}
    		}
    		else if(Server.release.get(sid)){
    			releaseCS();
    			Server.release.set(sid, false);
    		}
    	}
    }
    private synchronized void releaseCS(){
      	printStream.println("release");
    }
    private synchronized void requestCS(){
    	System.out.println("outputstream deadlock checkpoint 2 " +sid);
    	String request = Server.requester;
    	printStream.println(request);
    	System.out.println("outputstream deadlock checkpoint 3 " +sid);
      	try {
  			message = bufferedReader.readLine();
  			System.out.println("outputstream deadlock checkpoint 4 "+sid);
  		} catch(SocketTimeoutException ste){
  			dead=true;
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
      	if(message.equals(request))
      		Server.nod.put(request, Server.nod.get(request)+1);
    }
    private boolean checkServer(){
  	  boolean alive = true;
  	  if(first){
  		  try {
  				s=new Socket(ip, port);
  				first=false;
  		  } catch (UnknownHostException e) {
  				e.printStackTrace();
  		  } catch (IOException e) {
  				alive = false;
  				return alive;
  		  }
  	  }
  	  try {
  			s.setSoTimeout(100);
  		} catch (SocketException e2) {
  			// TODO Auto-generated catch block
  			e2.printStackTrace();
  		}
  	  try {
  			input = s.getInputStream();
  	  } catch (IOException e1) {
  			e1.printStackTrace();
  		}
  	  	
  	    try {
  			output = s.getOutputStream();
  		} catch (IOException e1) {
  			e1.printStackTrace();
  		}
  	    printStream = new PrintStream(output);
  	  	inputStream = new InputStreamReader(input);
  	  	bufferedReader = new BufferedReader(inputStream);
  	  	message = null;
  	    String check = "hello";
  	  	printStream.println(check);
      	try {
  			message = bufferedReader.readLine();
  		} catch(SocketTimeoutException ste){
  			alive = false;
  		} catch (IOException e) {
  			alive = false;
  		}
      	if(message==null){
      		alive=false;
      		return alive;
      	}	
      	if(!message.equals("hello"))
      		alive = false;
  	  return alive;
    }
}