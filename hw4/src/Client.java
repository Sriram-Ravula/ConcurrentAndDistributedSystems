//am73676_sr39533
import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;
public class Client {
	private static Socket s;
	private static InputStream input;
    private static OutputStream output;
    private static PrintStream printStream;
    private static InputStreamReader inputStream;
    private static BufferedReader bufferedReader;
    private static String message = null;
    private static DatagramPacket sPacket, rPacket;
    private static InetAddress ia;
    private static DatagramSocket datasocket;
    private static String[] ip;
    private static int[] port;
    private static int ind = 0;
    private final static int len = 65507;
    private static boolean first = true;
    
	
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);
    
    Scanner sc = new Scanner(System.in);
    int n = Integer.valueOf(sc.nextLine());
    ip = new String[n];
    port = new int[n];
    for(int i=0; i<n; i++){
    	String iport = sc.nextLine();
    	String[] tokens = iport.split(":");
    	ip[i] = tokens[0];
    	port[i] = Integer.valueOf(tokens[1]);
    }
      
    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");
      boolean isIntP = true;
      boolean isIntO = true;
  	  try {
		    int quant = Integer.parseInt(tokens[3]);
		  } catch (Exception e) {
		    isIntP = false;
		  }
  	  try {
		    int quant = Integer.parseInt(tokens[1]);
		  } catch (Exception e) {
		    isIntO = false;
		  }
      if (tokens[0].equals("purchase") && tokens.length==4 && isIntP) {
    	while(!checkServer(ind)){
    		ind++;
    		first = true;
    	}
    	tcpMethod(cmd, tokens[0]);
      } else if (tokens[0].equals("cancel") && tokens.length==2 && isIntO) {
      	while(!checkServer(ind)){
    		ind++;
    		first = true;
    	}
      	tcpMethod(cmd, tokens[0]);
      } else if (tokens[0].equals("search") && tokens.length==2) {
      	while(!checkServer(ind)){
    		ind++;
    		first = true;
    	}
      	tcpMethod(cmd, tokens[0]);
      } else if (tokens[0].equals("list") && tokens.length==1) {
      	while(!checkServer(ind)){
    		ind++;
    		first = true;
    	}
    	tcpMethod(cmd, tokens[0]);
      } else {
        System.out.println("ERROR: No such command");
      }
    }
  }
  private static void tcpMethod(String output, String action){
	  	printStream.println(output);
	  	if(action.equals("purchase")){
	    	try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	System.out.println(message);	
	  	}
	  	else if(action.equals("cancel")){
	    	try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	System.out.println(message);
	  	}
	  	else if(action.equals("search")){
	    	try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	if(message.substring(0, 1).equals("N")){
	    		System.out.println(message);
	    	}
	    	else{
		    	int n = Integer.valueOf(message);
		    	for(int i = 0; i<n; i++){
			    	try {
						message = bufferedReader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
			    	System.out.println(message);
		    	}
	    	}
	  	}
	  	else if(action.equals("list")){
	    	try {
				message = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	int n = Integer.valueOf(message);
	    	for(int i = 0; i<n; i++){
		    	try {
					message = bufferedReader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	System.out.println(message);
	    	}
	  	}
	  		

  }
  
  private static boolean checkServer(int index){
	  boolean alive = true;
	  if(first){
		  try {
				s=new Socket(ip[index], port[index]);
		  } catch (UnknownHostException e) {
				e.printStackTrace();
		  } catch (IOException e) {
				alive = false;
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
	    String check = "hi";
	  	printStream.println(check);
    	try {
			message = bufferedReader.readLine();
		} catch(SocketTimeoutException ste){
			alive = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if(!message.equals("hi"))
    		alive = false;
	    if(alive)
			try {
				s.setSoTimeout(0);
			} catch (SocketException e) {
				e.printStackTrace();
			}
	  return alive;
  }

}