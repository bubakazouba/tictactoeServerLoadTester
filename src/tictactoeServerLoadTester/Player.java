package hi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


public class Player {
	ClientUDP myUDP;
	String gameId;//receive gameId and whichPlayer from server when username is sent when initializing the gam
	String username;
	int serverPort = 5005;
	boolean isX;
	public Player(String username){
		this.username=username;
		try {
			myUDP=new ClientUDP(5005, InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			System.out.println("Could not connect to host: "+e);
		}
	}
	
	public void startGame(){
		JSONObject obj=new JSONObject();
        try {
			obj.put("username", username);
		} catch (JSONException e) {}
        myUDP.username=username;
		myUDP.send(obj);
		
		receiveGameId();
		
		Random rand = new Random();
		int i;
		if(isX){
			for(i=0;i<5;i++){//TODO: unhardcode the 5
				String sentCoordinates = Double.toString(rand.nextDouble());
				sendPlay(sentCoordinates);
				String receivedCoordinates = getPlay();
				if(new StringBuilder(receivedCoordinates).reverse().toString().equals(sentCoordinates)){
//					System.out.println("SUCCESS!!!!!");
				}
				else {
					System.out.println("ERROR!!!");
				}
			}
		}//end isX
		else {
			for(i=0;i<5;i++){
				String receivedCoordinates = getPlay();
				String sentCoordinates=new StringBuilder(receivedCoordinates).reverse().toString();
				sendPlay(sentCoordinates);
			}
		}
		//stop the socket from listening
//		myUDP.keepListening=false;
//		myUDP.socket.close();
	}

	private void receiveGameId(){
		JSONObject json=myUDP.receive();
		try {
			gameId=json.getString("gameId");
			isX=json.getString("whichPlayer").equals("1");
		} catch (JSONException e) {
			System.out.println("JSON exception error:"+e);
		}
	}//end receiveGameId

	private void sendPlay(String coordinates){
		JSONObject msg = new JSONObject();
		try {
			msg.put("gameId", gameId);
			msg.put("username", username);
			msg.put("coordinates", coordinates);
			myUDP.send(msg);
		} catch (JSONException e) {
			System.out.println("JSON exception error:"+e);
		}
	}//end sendPlay
	
	private String getPlay(){
        JSONObject play = myUDP.receive();
        try {
			return (String)play.get("coordinates");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return null;
	}

}
