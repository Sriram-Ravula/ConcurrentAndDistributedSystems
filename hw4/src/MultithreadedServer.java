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
	private static InputStream input;
    private static OutputStream output;
    private static PrintStream printStream;
    private static InputStreamReader inputStream;
    private static BufferedReader bufferedReader;
    private static String message = null;
	Socket theClient;
	private static Hashtable<String, Integer> items = Server.items;
	private static OrderLog ol = new OrderLog();
	private static boolean client;
    private static boolean first = true;
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
		  String message = null;
		  message=bufferedReader.readLine();
  		  while(message!=null){
  	  			  if(message.equals("hi")){
  	  				  printStream.println(message);
  	  				  client = true;
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
    private synchronized void askForCS(){
			  Server.wantCS = true;
			  Server.message=message;
			  while(!Server.acknowledgements & !Server.top);
			  String response=processMessage(message);
			  printStream.println(response);
			  Server.wantCS = false;
			  try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    public synchronized static String processMessage(String m){
    	m = m.trim();
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