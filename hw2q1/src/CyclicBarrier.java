/*
 * am73676_sr39533
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {
	private Semaphore s;
	private Semaphore lock;
	private int numWaiting;
	private int parties;
	private int houseParty;
	
	
	
	public CyclicBarrier(int parties) {
		this.parties = parties;
		s = new Semaphore(parties);
		lock = new Semaphore(1);
		numWaiting = 0;
		houseParty = 0;
	}
	
	public int await() throws InterruptedException {		
		s.acquire();
		lock.acquire();
		numWaiting++;
		int myIndex = parties - numWaiting;
		lock.release();
		while(numWaiting != parties){
			Thread.yield();
		}
		lock.acquire();
		houseParty++;
		if(houseParty==parties){
			s.release(parties);
			houseParty=0;
			numWaiting=0;
		}
		lock.release();
	    return myIndex;
	}
}
