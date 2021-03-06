//am73676_sr39533
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

public class Server {
	
	public static int ID; //this server's ID
	public static int numServers; //the total number of servers
	public static String myAddress; //this server's IP
	public static int myPort;
	public static volatile int numAlive;
	public static ArrayList<String> addresses= new ArrayList<String>(); //the list of servers' addresses
	public static ArrayList<Integer> ports= new ArrayList<Integer>(); //the list of servers' ports
	public static volatile ArrayList<Boolean> dead  = new ArrayList<Boolean>();
	public static volatile ArrayList<Boolean> request = new ArrayList<Boolean>();
    public static volatile ArrayList<Boolean> release = new ArrayList<Boolean>();
	public static volatile Hashtable<String, Integer> nod=new Hashtable<String, Integer>();
    public static Semaphore s = new Semaphore(1, true);
    public static Semaphore t = new Semaphore(1, true);
	public static volatile LogicalClock clock;
    public static volatile Hashtable<String, Integer> items = new Hashtable<String, Integer>(); //the inventory
    public static boolean acknowledgements;
    public static volatile boolean top=false;
    public static volatile boolean wantCS;
    public static volatile String requester;
    public static volatile String message;
    public static volatile boolean first = true;
    public static volatile LinkedList<String> lamport = new LinkedList<String>();
	
	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		String inventory = null; //the inventory filename
		boolean inputOK = true; //boolean to check if the input is valid
		
		//Grab the first line of inputs
		do{ //if inputs are incorrect, try again
			String params = sc.nextLine(); //grab the first line of the input
			String[] inputs = (params.trim()).split(" "); //array that holds the three first inputs to the server
			
			try{
				ID = Integer.parseInt(inputs[0]);
				numServers = Integer.parseInt(inputs[1]);
				numAlive = numServers-1;
				inventory = inputs[2];
			}
			catch(Exception e){
				inputOK = false;
				System.out.println("Wrong input - try again!");
			}
		}
		while(!inputOK); 
		
		//Grab the IPs and ports of all the servers
		for(int i = 0; i < numServers; i++){
			do{ //if inputs are incorrect, try again
				String next = sc.nextLine();
				String[] parsed = (next.trim()).split(":");
				
				try{
					ports.add(Integer.parseInt(parsed[1]));
					addresses.add(parsed[0]);
					dead.add(false);
					request.add(false);
					release.add(false);
				}
				catch(Exception e){
					inputOK = false;
				}
			}
			while(!inputOK);
		}
		
		//Extract this server's info from the lists of IPs and Ports
		myAddress = addresses.get(ID - 1);
		myPort = ports.get(ID - 1);
		
		sc.close();
		
		//TODO: spin off Server communication threads, link to other servers, listen for clients, spin off client handlers
		clock = new LogicalClock();
		
		//parse the input file and fill the inventory
		String line = null;
		String[] item = new String[2];
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
		
		//Listening
		ConnectionListener listener = new ConnectionListener(myPort);
		listener.start();
	}
}
