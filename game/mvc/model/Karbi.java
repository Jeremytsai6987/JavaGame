package edu.uchicago.gerber._08final.mvc.model;


import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.awt.*;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.GameOp;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

// Generate Karbi Class
public class Karbi extends Sprite {
	// shooting
	private static final Random random = new Random();
	private static final double SHOOTING_PROBABILITY = 0.01; // Adjust as needed
	//radius of a large asteroid
	private final int LARGE_RADIUS = 80;

	public enum ImageState {

		Karbi_MOVE,
	}

	//shoot the bullet
	// Use attemptToShoot to let it attack the chicken
	public void attemptToShoot() {
		if (random.nextDouble() < SHOOTING_PROBABILITY) {
			Bullet_FOE bullet = new Bullet_FOE(this);
			CommandCenter.getInstance().getOpsQueue().enqueue(bullet, GameOp.Action.ADD);
		}
	}
	public Karbi(int size){
		if (size == 0) setRadius(LARGE_RADIUS);
		else setRadius(LARGE_RADIUS/(size * 2));

		final int SAFE_DISTANCE = 150; // Adjust this value as needed

		// Obtain the player character's position
		Point playerPosition = CommandCenter.getInstance().getChicken().getCenter();
		Point spawnPosition;

		do {
			// Generate a random spawn position within the game boundaries
			spawnPosition = new Point(random.nextInt(1100), random.nextInt(900));

			// Repeat until the spawn position is at a safe distance from the player
		} while (spawnPosition.distance(playerPosition) < SAFE_DISTANCE);

		// Set the spawn position of the Karbi
		setCenter(spawnPosition);

		//Asteroid is FOE
		setTeam(Team.FOE);
		setColor(Color.RED);



		//the spin will be either plus or minus 0-9
		setSpin(somePosNegValue(10));
		//random delta-x
		setDeltaX(somePosNegValue(10));
		//random delta-y
		setDeltaY(somePosNegValue(10));


		Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.Karbi_MOVE, loadGraphic("/imgs/kirby/kirby.png") ); //normal ship
		setRasterMap(rasterMap);

	}


	// Call this when Karbi exploded
	public Karbi(Karbi astExploded){
		this(astExploded.getSize() + 1);
		setCenter(astExploded.getCenter());
		int newSmallerSize = astExploded.getSize() + 1;
		setDeltaX(astExploded.getDeltaX() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));
		setDeltaY(astExploded.getDeltaY() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));

		Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(Karbi.ImageState.Karbi_MOVE, loadGraphic("/imgs/kirby/kirby.png") ); //normal ship
		setRasterMap(rasterMap);

	}
	public int getSize(){
		switch (getRadius()) {
			case LARGE_RADIUS:
				return 0;
			case LARGE_RADIUS / 2:
				return 1;
			case LARGE_RADIUS / 4:
				return 2;
			default:
				return 0;
		}
	}


	@Override
	public void draw(Graphics g) {

		renderRaster((Graphics2D) g, getRasterMap().get(Karbi.ImageState.Karbi_MOVE));
	}

	@Override
	public void remove(LinkedList<Movable> list) {
		super.remove(list);
		spawnSmallerKarbisOrDebris(this);
		CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
		Sound.playSound("kapow.wav");

	}

	private void spawnSmallerKarbisOrDebris(Karbi originalKarbis) {

		int size = originalKarbis.getSize();
		if (size > 1) {
			CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalKarbis), GameOp.Action.ADD);
		}
		//med and large
		else {
			//for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller Karbi respectively
			//We can use the existing variable (size) to do this
			size += 1;
			while (size-- > 0) {
				CommandCenter.getInstance().getOpsQueue().enqueue(new Karbi(originalKarbis), GameOp.Action.ADD);
			}
		}

	}
}
