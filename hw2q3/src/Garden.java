import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
	final Lock lock = new ReentrantLock();
	final Condition diggingStarted = lock.newCondition();
	final Condition maryHasShovel = lock.newCondition();
	final Condition newtonHasShovel = lock.newCondition();
	int holesDug = 0;
    int seedsFilled = 0;
    int holesFilled = 0;
	public Garden() {   }; 
	public void startDigging() {
		if(holesDug != 0){
			try {
				newtonHasShovel.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while((holesDug == seedsFilled + 4) || (seedsFilled == holesFilled + 8)){
			maryHasShovel.signal();
		}
	}; 
	public void doneDigging() {
		lock.lock();
		try{
			holesDug++;
		}
		finally{
			lock.unlock();
		}
		maryHasShovel.signal();
		diggingStarted.signal();
	}; 
	public void startSeeding() {
		while(seedsFilled == holesDug);
		try {
			diggingStarted.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	public void doneSeeding() {
		lock.lock();
		try{
			seedsFilled++;
		}
		finally{
			lock.unlock();
		}
	}; 
	public void startFilling() {
		while(seedsFilled == holesFilled);
		try {
			newtonHasShovel.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}; 
	public void doneFilling() {
		lock.lock();
		try{
			holesFilled++;
		}
		finally{
			lock.unlock();
		}
		newtonHasShovel.signal();
	}; 
 
    /*
    * The following methods return the total number of holes dug, seeded or 
    * filled by Newton, Benjamin or Mary at the time the methods' are 
    * invoked on the garden class. */
   public int totalHolesDugByNewton() {return holesDug;}; 
   public int totalHolesSeededByBenjamin() {return seedsFilled;}; 
   public int totalHolesFilledByMary() {return holesFilled;}; 
}
