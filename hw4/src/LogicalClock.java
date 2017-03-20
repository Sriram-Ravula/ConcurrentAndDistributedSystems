
public class LogicalClock {
	
	static int clock;
	
	public LogicalClock (){
		clock = 1;
	}
	
	public synchronized void tick(){
		clock++;
	}
	
	public synchronized void sendAction(){
		clock++;
	}
	
	public synchronized static int getClock(){
		return clock;
	}
	
	public synchronized void recieveAction(int other){
		if(clock > other)
			clock++;
		else
			clock = other + 1;
	}
}