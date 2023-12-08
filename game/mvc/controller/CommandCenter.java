package edu.uchicago.gerber._08final.mvc.controller;



import edu.uchicago.gerber._08final.mvc.model.*;
import lombok.Data;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numChickens; // This is used for showing existing life of chicken
	private  int level; // Indicate the level at that time
	private  long score; // Recording the Score
	private  boolean paused; // flag of paused

	private  boolean muted;

	//this value is used to count the number of frames (full animation cycles) in the game
	private long frame;

	//the chicken is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the chicken ref always points to the single chicken object on heap.
	//Lombok will not provide setter methods on final memberskeep
	private final Chicken chicken  = new Chicken(); // Build the Chicken object

	//lists containing our movables subdivided by team
	private final LinkedList<Movable> movDebris = new LinkedList<>();
	private final LinkedList<Movable> movFriends = new LinkedList<>();
	private final LinkedList<Movable> movFoes = new LinkedList<>();
	private final LinkedList<Movable> movFloaters = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//for sound playing. Limit the number of threads to 5 at a time.
	private final ThreadPoolExecutor soundExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {}

    //this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}



	public void initGame(){
		clearAll();
		generateStarField();
		setLevel(0);
		setScore(0);
		setPaused(false);
		//set to one greater than number of Chickens lives in your game as initChickenAndDecrementNum() also decrements
		//set the
		setNumChickens(4);
		chicken.decrementFalconNumAndSpawn();
		//add the falcon to the movFriends list
		opsQueue.enqueue(chicken, GameOp.Action.ADD);
	}




	private void generateStarField(){

		int count = 100;
		while (count-- > 0){
			opsQueue.enqueue(new Star(), GameOp.Action.ADD);
		}

	}



	public void incrementFrame(){
		//use of ternary expression to simplify the logic to one line
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;
	}

	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
	}

	public boolean isGameOver() {		//if the number of Chicken is zero, then game over
		return numChickens < 1;
	}






}
