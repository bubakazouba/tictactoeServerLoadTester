package hi;
//messageForThread has to be changed
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientUDP{
    DatagramSocket socket;
    InetAddress local;
    int serverPort;
    public ACKManager ACKManager = new ACKManager();//only for sending
    private ArrayList<Integer> recvACKS; //recvACKS = [list of ACKS of messages that are received and proccessed]
    private JSONObject messageForThread;//this won't work if we have alot of games, multiple threads can override it, I should make an object of a class that extends Thread and then put that variable in it
    JSONObject json;
    BlockingQueue<String> msgsBQ;
    String username="";
    boolean keepListening=true;
    
    /*
     * start up the socket
     */
    public ClientUDP(int serverPort, InetAddress serverInetAddress){
        this.serverPort = serverPort;
        msgsBQ = new ArrayBlockingQueue<String>(10);//wouldn't need more than that for the demo 
        recvACKS = new ArrayList<Integer>();
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException e1) {
            System.out.println("couldn't create socket error:"+e1);
        }
        local = serverInetAddress;
        
        Thread listenThread = new Thread(new Runnable(){public void run(){
            while (keepListening) {
                byte[] receiveData = new byte[1024];
                DatagramPacket p = new DatagramPacket(receiveData,receiveData.length);
                String messageReceived = null;
                try{
                    socket.receive(p);
                    messageReceived = new String(p.getData(), 0, p.getLength());//to prevent having garbage data after the message
                    JSONObject msgJ = new JSONObject(messageReceived);
//                    System.out.println("ClientUDP||listenThread||got msgJ="+msgJ.toString()+username);
                    
                    try{//see if it's just an ack, then it's an ACK to something i sent, just check it off using ACKManager
                        if (msgJ.has("msg") && msgJ.get("msg").equals("ack") ){
//                            System.out.println("listenThread||msg=ack="+msgJ.get("ack")+" "+username);
                            int ACK = Integer.parseInt((String) msgJ.get("ack"));
                            ACKManager.confirmACK(ACK);
                            continue;
                        }
                    }catch(JSONException e){//if it didnt work then just skip
                        System.out.println("JSONException: "+e);
                    }
                    //else:
                    //it's either a message that I already got before
                    //or a message I should put on the BlockingQueue to process
                    //in both cases I want to reply back with {"msg":"ack","ack":$ack}
                    int ACK = Integer.parseInt((String)msgJ.get("ack"));
                    JSONObject msg = new JSONObject();
                    msg.put("msg", "ack");
                    msg.put("ack",ACK);
                    _send(msg.toString());
//                    System.out.println("ClientUDP||listenThread||sent back ack="+ACK);
                    
                    if(recvACKS.contains(ACK)){//looks like we already had that, do nothing
                        continue;
                    }
                    else{//we didnt have this before, put it in BlockingQueue so it can be processed, and put it in the list of acks
                        recvACKS.add(ACK);
                        msgsBQ.put(messageReceived);
                        continue;
                    }
                }catch(Exception e){
                    System.out.println("listenThread||ERROR4||"+e.toString()+" "+username);
                }
            }//end while true
        }});
        listenThread.start();
    }//end constructor
    
    private void _send(String sendThisString){
        int msg_length = sendThisString.length();
        DatagramPacket p = new DatagramPacket(sendThisString.getBytes(), msg_length, local,serverPort);
        try {
            socket.send(p);
        } catch (IOException e) {
            System.out.println("_send||ERROR||"+e.toString());
        }
    }
    
    public void send(JSONObject msg) {
        //I should make a class that extends Thread, then make an object of it right here, in the constructor pass the message to send, then when I .run() it, it would just take this message
        this.messageForThread=msg;
        Thread setUpSocketThread=new Thread(new Runnable(){public void run(){
            try{
//            	System.out.println(username+ " just started send thread");
                int ACK = ACKManager.getACK();
                messageForThread.put("ack",Integer.toString(ACK));
                String msgToSend = messageForThread.toString();
                
                int msg_length = msgToSend.length();
//                System.out.println("ClientUDP||send||going to send then wait for 0.5:"+msgToSend);

                DatagramPacket p = new DatagramPacket(msgToSend.getBytes(), msg_length, local,serverPort);
                socket.send(p);
                
                //now the ack part
                while(true){
                    Thread.sleep(500);
                    if (ACKManager.isACKConfirmed(ACK)){
//                        System.out.println("send||got ACK="+ACK+username);
                        break;
                    }
                    else{
//                        System.out.println("send||didn't get ACK="+ACK+" sending again"+username);
                        socket.send(p);
                    }
                }//end while
            }catch(Exception e){
                System.out.println("ERROR3||"+e.toString());
            }
        }});
//        System.out.println(username+ "starting send thread now");
        setUpSocketThread.start();
//        try {
//          setUpSocketThread.join();
//      } catch (InterruptedException e) {
//          System.out.println("could not join setUpSocketThread, error:"+e);
//      }
    }//end of send
    
    public JSONObject receive(){//should run in a thread
        JSONObject msg = null;
        try {
            msg =new JSONObject(msgsBQ.take());
        } catch (InterruptedException e) {
            System.out.println("ClientUDP||receive||ERROR"+e.toString());
        } catch (JSONException e){
            System.out.println("ClientUDP||receive||ERROR"+e.toString());
        }
        return msg;
    }
       
    
}