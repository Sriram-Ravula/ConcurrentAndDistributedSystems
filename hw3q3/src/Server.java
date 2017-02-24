import java.io.*;
import java.util.*;
import java.net.*;
public class Server extends Thread {
    private static Hashtable<String, Integer> items = new Hashtable<String, Integer>();
	Socket theClient;
	private static boolean tcpOn = true;
    public Server(Socket s){
    	theClient = s;	
    }
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");
      System.exit(-1);
    }
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];

 // This will reference one line at a time
    String line = null;
    String[] item = new String[2];
    String inventory = "inventory.txt";

    try {
        FileReader fileReader = 
            new FileReader(inventory);

        BufferedReader bufferedReader = 
            new BufferedReader(fileReader);

        while((line = bufferedReader.readLine()) != null) {
        	line = line.trim();
        	item = line.split(" ");
        	if(item.length == 2)
        		items.put(item[0], Integer.valueOf(item[1]));
        }   
        bufferedReader.close();         
    }
    catch(FileNotFoundException ex) {
        System.out.println(
            "Unable to open file '" + inventory + "'");                
    }
    catch(IOException ex) {
        System.out.println(
            "Error reading file '" + inventory + "'");                  
    }
    // parse the inventory file

    // TODO: handle request from clients
	System.out.println("IDK started:");
	try {
		int port = tcpPort;
		if(!tcpOn)
			port = udpPort;		
		ServerSocket listener = new ServerSocket(port);
		Socket s;
		while ( (s = listener.accept()) != null) {
			Thread t = new Server(s);
			t.start();
		}
	} catch (IOException e) {
		System.err.println("Server aborted:" + e);
	}
  }
  public void run(){
	  try{
		  InputStream input = theClient.getInputStream();
		  OutputStream output = theClient.getOutputStream();
		  PrintStream printStream = new PrintStream(output);
		  InputStreamReader inputStream = new InputStreamReader(input);
		  BufferedReader bufferedReader = new BufferedReader(inputStream);
		  String message = null;
		  message = bufferedReader.readLine();
		  //implement methods that take the input of the message
		  
	  }
	  catch(Exception e){
		  System.err.println(e);
	  }
  }
  public synchronized void increaseInventory(String product){
	  int count = items.get(product);
	  count++;
	  items.put(product, count);
  }
  public synchronized void decreaseInventory(String product){
	  int count = items.get(product);
	  count--;
	  items.put(product, count);
  }
  public synchronized void changePort(){
	  if(!tcpOn)
		  tcpOn = true;
	  else
		  tcpOn = false;
  }
}
