//am73676_sr39533
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;

public class MultithreadedServer extends Thread{
	private InputStream input;
    private OutputStream output;
    private PrintStream printStream;
    private InputStreamReader inputStream;
    private BufferedReader bufferedReader;
    private String message = null;
	Socket theClient;
	private static volatile Hashtable<String, Integer> items = Server.items;
	private static OrderLog ol = new OrderLog();
    private static int myTimestamp = 0; //holds the time stamp for the current request
    
    
    public MultithreadedServer(Socket s){
    	theClient = s;	
    }
    
    public void run(){
  	  try{
  		  input = theClient.getInputStream();
		  output = theClient.getOutputStream();
		  printStream = new PrintStream(output);
		  inputStream = new InputStreamReader(input);
		  bufferedReader = new BufferedReader(inputStream);
		  message=bufferedReader.readLine();
  		  while(message!=null){
  	  			  if(message.equals("hi")){
  	  				  printStream.println(message);
	  				  try {
  	  					  message = bufferedReader.readLine();
  	  				  } catch (IOException e) {
  					// TODO Auto-generated catch block
  	  					  e.printStackTrace();
  	  				  }  
  	  			  }
  	  			  else{
  	  				  askForCS();
  	  			  }
  		  }
  	  }
  	  catch(Exception e){
  		  System.err.println(e);
  	  }
    }
    private void askForCS(){
    	try {
			Server.s.acquire(); //locks the server.requester variable so that multiple clients cannot edit, only one client at a time here
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    		Server.message=message;
    		
			Server.clock.tick();
			String requester = String.valueOf(Server.clock.getClock()) + " " + String.valueOf(Server.ID) + " " + Server.message;
			Server.requester=requester;
			Server.nod.put(requester, 0);
			Lamport.addToList(requester, true);
			if(Server.first){
				for(int i = 0;i<Server.numServers;i++){
					if(i!=(Server.ID-1)){
						LamportClientThread t = new LamportClientThread(Server.addresses.get(i), Server.ports.get(i), i);
						Server.request.set(i,true); //send message to this server
						t.start();
					}
				}
				for(int i = 0;i<Server.numServers;i++){
					if(!Server.dead.get(i) && (i!=(Server.ID-1))){
						while(Server.request.get(i) && !Server.dead.get(i));//waits for all threads connected to each server to send request
					}	
				}
				Server.first=false;
			}
			else{
				for(int i = 0;i<Server.numServers;i++){
					Server.request.set(i, true); //signals threads that they can start sending the request
				}
				for(int i = 0;i<Server.numServers;i++){
					if(!Server.dead.get(i) && (i!=(Server.ID-1)))
						while(Server.request.get(i) && !Server.dead.get(i)); //waits for all threads connected to each server sends request
				}
			}

			Server.s.release(); //allow other clients to ask for the CS
			
			while((Server.nod.get(requester)<Server.numAlive) && (!Server.top) && (!Server.lamport.peek().equals(requester))); //waits for all requests to bounce back, the server to notice top of linked list is its server, and that the command at top is associated with specific client
			Server.top=false;
			String response=processMessage(message, false); //processes clients message and makes a string response
			printStream.println(response);// sends message back to client
			
			try {
				Server.t.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(int i = 0;i<Server.numServers;i++){
				Server.release.set(i, true); //signals threads that they can send release message
			}
			for(int i = 0;i<Server.numServers;i++){
				if(!Server.dead.get(i) && (i!=(Server.ID-1)))
					while(Server.release.get(i)&&!Server.dead.get(i)); //waits for all threads connected to each server to send release
			}
			Server.t.release();
			
			try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    }
    public synchronized static String processMessage(String m, boolean external){
    	m = m.trim();
    	if(!external)
    		Server.lamport.pollFirst();
    	String[] n = m.split(" ");
    	if(n[0].equals("purchase")){
    		String state = editInventory(n[2],Integer.valueOf(n[3]), true, false);
			if(state.equals("A")){
				Order o = new Order(n[1],n[2],Integer.valueOf(n[3]));
				ol.add(o);
				return "Your order has been placed, " + o.getId() + " " + n[1] + " " + n[2] + " " + n[3];
			}	
    		else if(state.equals("E"))
    			return "Not Available - Not enough items";
    		else if(state.equals("N"))
    			return "Not Available - We do not sell this product";
    	}
    	else if(n[0].equals("cancel")){
    		Order o = ol.cancel(Integer.valueOf(n[1]));
    		if(o!=null){
    			editInventory(o.getProduct(), o.getQuantity(), false, false);
    			return "Order " + n[1] + " is canceled";
    		}
    		else
    			return n[1] + " not found, no such order";
    		
    	}
    	else if(n[0].equals("search")){
    		ArrayList<Order> a = ol.search(n[1]);
    		if(a.isEmpty())
    			return "No order found for " + n[1];
    		else{
    			return String.valueOf(a.size())+ "\n" + ol.ordersToString(a);
    		}
    			
    	}
    	else if(n[0].equals("list")){
    		return editInventory("?", 0,false, true);
    	}
    	return m;
    }
    private synchronized static String editInventory(String product, int amount, boolean decrease, boolean search){
    	if(decrease){
        	if(items.get(product)==null)
        		return "N";
        	else if(amount>items.get(product))
      	  		return "E";
      	  	int count = items.get(product);
      	  	count-=amount;
      	  	items.put(product, count);
      	  	return "A";
    	}
    	else if (search){
    		String s = "";
    		int lines = 0;
    		Set<String> keys = items.keySet();
    		for(String a: keys){
    			s+= a + " " + items.get(a) + "\n";
    			lines++;
    		}
    		s=String.valueOf(lines) +"\n" + s.trim();
    		return s;
    	}
    	else{
    		int count = items.get(product);
      	  	count+=amount;
      	  	items.put(product, count);
    		return "";
    	}
    }
}
