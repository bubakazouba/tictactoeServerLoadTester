package hi;

import java.util.ArrayList;
import java.util.Collections;


public class ACKManager {
	private ArrayList<Integer> sentACKS;
	private ArrayList<Integer> confirmedACKS;
	
	public ACKManager(){
		sentACKS = new ArrayList<Integer>();
		confirmedACKS = new ArrayList<Integer>();
	}
	
	public int getACK(){
		int ACK;
		synchronized (sentACKS) {
			if (sentACKS.size() == 0) {
				ACK = 0;
			}
			else{
				ACK = Collections.max(sentACKS) + 1; 
			}
			sentACKS.add(ACK);
		}
		return ACK;
	}
	
	public void confirmACK(int ACK){
		synchronized (confirmedACKS) {
			confirmedACKS.add(ACK);
		}
	}
	
	public boolean isACKConfirmed(int ACK){
		boolean confirmed;
		synchronized(confirmedACKS){
			confirmed = confirmedACKS.contains(ACK);
		}
		return confirmed;
	}
	
}//end ACKManager
