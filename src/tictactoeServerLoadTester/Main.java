package hi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Random;

public class Main {

	public static void main(String[]args){
    	OutputStream out = new OutputStream() {
    		@Override
    		public void write(int b) throws IOException {
    			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/Users/Sahmoud/Documents/err.txt", true)))) {
    			    out.print(Character.toString((char)b));
    			}catch (IOException e) {
    			    //exception handling left as an exercise for the reader
    			}
    		}
    	};
    	System.setErr(new PrintStream(out));
		
		
		final int NUMBER_OF_PLAYERS=500;
		Player[] players = new Player[NUMBER_OF_PLAYERS];
		int i;
		for(i=0;i<NUMBER_OF_PLAYERS;i++){
			players[i] = new Player(randUsername());
			Play play=new Play(players[i]); 
			new Thread(play).start();
			System.out.println("player number:"+i);
		}
			
	}
	public static void initailizeGame() {
		
	}

	public static String randUsername() {
	    Random rand = new Random();
	    return Double.toString(rand.nextDouble());
	}
}
class Play implements Runnable{
	Player player;
	public Play(Player player){
		this.player=player;
	}

	@Override
	public void run() {

		player.startGame();
	}
}