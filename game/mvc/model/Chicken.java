package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.Sound;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Data
public class Chicken extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================

	//static fields
	@Getter
	private int hitCount = 0; // Field to track number of hits
	private boolean isMarkedForRemoval = false; // Flag to indicate removal status

	private static final int TOTAL_HITS_REQUIRED = 80;
	private static final int BAR_HEIGHT = 8;

	//number of degrees the falcon will turn at each animation cycle if the turnState is LEFT or RIGHT
	public final static int TURN_STEP = 90;
	//number of frames that the falcon will be protected after a spawn
	public static final int INITIAL_SPAWN_TIME = 60;
	//number of frames falcon will be protected after consuming a NewShieldFloater
	public static final int MAX_SHIELD = 200;
	public static final int MAX_NUKE = 600;

	public static final int MIN_RADIUS = 28;
	public static final int MOVEMENT_SPEED = 20; // You can adjust this value as needed

	private static final int ORIENTATION_UP = 270;
	private static final int ORIENTATION_DOWN = 90;
	private static final int ORIENTATION_LEFT = 180;
	private static final int ORIENTATION_RIGHT = 0;


	//Let the Character only shoot toward Right or Left
	private int lastDirection = 0; //Default Direction

	public int getLastDirection(){
		return this.lastDirection;
	}

	public void setLastDirection(int lastDirection){
		this.lastDirection = lastDirection;
	}



	//images states
	//images follow these states to show diff pic
	public enum ImageState {
		CHICKEN_INVISIBLE, //for pre-spawning

		CHICKEN_LEFT,

		//UP,
		CHICKEN_RIGHT,

		//RIGHT,
		CHICKEN_DEATH,

		CHICKEN_PROTECTED;


	}

	private ImageState currentImageState;


	//instance fields (getters/setters provided by Lombok @Data above)
	private int shield;

	private int nukeMeter;
	private int invisible;
	private boolean maxSpeedAttained;

	private int showLevel;
	//enum used for turnState field
	public enum TurnState {IDLE, LEFT, RIGHT, DOWN, UP}
	private TurnState turnState = TurnState.IDLE;
	public enum WEAPON_STATE {BULLET, SPEAR}
	private WEAPON_STATE weapon = WEAPON_STATE.BULLET;

	public WEAPON_STATE getLastWeapon(){
		return this.weapon;
	}


	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================

	public Chicken() {

		setTeam(Team.FRIEND);

		setRadius(MIN_RADIUS);


		//We use HashMap which has a seek-time of O(1)
		//See the resources directory in the root of this project for pngs.
		//Using enums as keys is safer b/c we know the value exists when we reference the consts later in code.
		Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.CHICKEN_INVISIBLE, null );
		rasterMap.put(ImageState.CHICKEN_LEFT, loadGraphic("/imgs/chicken/tile000.png"));
		rasterMap.put(ImageState.CHICKEN_RIGHT, loadGraphic("/imgs/chicken/tile001.png"));
		rasterMap.put(ImageState.CHICKEN_DEATH, loadGraphic("/imgs/chicken/tile003.png"));
		rasterMap.put(ImageState.CHICKEN_PROTECTED, loadGraphic("/imgs/chicken/tile002.png"));

		setRasterMap(rasterMap);


	}




	// ==============================================================
	// METHODS 
	// ==============================================================
	@Override
	public void move() {
		super.move();

		if (invisible > 0) invisible--;
		if (showLevel > 0) showLevel--;

        //adjust the orientation given turnState
		int adjustOr = getLastDirection();
		switch (turnState) {
			case LEFT:
				setCenter(new Point(getCenter().x - MOVEMENT_SPEED, getCenter().y));
				setOrientation(ORIENTATION_LEFT);
				currentImageState = ImageState.CHICKEN_LEFT;
				break;
			case RIGHT:
				setCenter(new Point(getCenter().x + MOVEMENT_SPEED, getCenter().y));
				setOrientation(ORIENTATION_RIGHT);
				currentImageState = ImageState.CHICKEN_RIGHT;
				break;
			case DOWN:
				setCenter(new Point(getCenter().x, getCenter().y + MOVEMENT_SPEED));
				setOrientation(ORIENTATION_DOWN);
				break;
			case UP:
				setCenter(new Point(getCenter().x, getCenter().y - MOVEMENT_SPEED));
				setOrientation(ORIENTATION_UP);
				break;
			case IDLE:
			default:
				// No movement or orientation change
				//break;
		}
		setOrientation(adjustOr);

	}

	//Draw the chicken and show the amount of life we have with horizontal bar plot
	@Override
	public void draw(Graphics g) {

		ImageState imageState = currentImageState;

		renderRaster((Graphics2D) g, getRasterMap().get(imageState));

		// Draw the hit count bar
		int barWidth = (int) ((TOTAL_HITS_REQUIRED - hitCount) * (double) getRadius() * 2 / TOTAL_HITS_REQUIRED);
		int barX = getCenter().x - getRadius();
		int barY = getCenter().y - getRadius() - BAR_HEIGHT - 5; // Positioning the bar above the Chicken

		g.setColor(Color.GREEN);
		g.fillRect(barX, barY, barWidth, BAR_HEIGHT);

	}
	public void incrementHitCount() {
		hitCount++;
		if (hitCount >= 50) {
			markForRemoval();
		}
	}

	public void setHitCount(int num) {
		hitCount = num;
	}

	// Flag this BigBoss for removal
	private void markForRemoval() {
		isMarkedForRemoval = true;
	}


	@Override
	public void remove(LinkedList<Movable> list) {
		if ( shield == 0 && isMarkedForRemoval)
		{
			currentImageState = ImageState.CHICKEN_DEATH;
		}
		decrementFalconNumAndSpawn();
		//become chicken
	}


	public void decrementFalconNumAndSpawn(){

		CommandCenter.getInstance().setNumChickens(CommandCenter.getInstance().getNumChickens() -1);
		currentImageState = ImageState.CHICKEN_DEATH;
		if (CommandCenter.getInstance().isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		setShield(Chicken.INITIAL_SPAWN_TIME);
		currentImageState = ImageState.CHICKEN_DEATH; //Removable
		setInvisible(Chicken.INITIAL_SPAWN_TIME/4);
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		setDeltaX(0);
		setDeltaY(0);
		setRadius(Chicken.MIN_RADIUS);
		setMaxSpeedAttained(false);
		setNukeMeter(0);
		setHitCount(0);
	}

} //end class
