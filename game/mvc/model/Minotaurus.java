package edu.uchicago.gerber._08final.mvc.model;


import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.*;
import java.util.Map;
import java.util.Random;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.GameOp;
import edu.uchicago.gerber._08final.mvc.controller.Sound;


//Minotaurus Class
public class Minotaurus extends Sprite {
    // shooting
    private static final Random random = new Random();
    private static final double SHOOTING_PROBABILITY = 0.03; // Adjust as needed

    //shoot the bullet

    public enum ImageState {

        MINOTAU_MOVE,

        MINOTAU_RIGHT,

        MINOTAU_LEFT,


    }

    // Let it attack chicken by this method
    public void attemptToShoot() {
        if (random.nextDouble() < SHOOTING_PROBABILITY) {
            Axe axe = new Axe(this);
            CommandCenter.getInstance().getOpsQueue().enqueue(axe, GameOp.Action.ADD);
        }
    }
    public Minotaurus(int size){
        int LARGE_RADIUS = 80;
        setRadius(LARGE_RADIUS);

        final int SAFE_DISTANCE = 150; // Adjust this value as needed

        // Obtain the player character's position
        Point playerPosition = CommandCenter.getInstance().getChicken().getCenter();
        Point spawnPosition;

        do {
            // Generate a random spawn position within the game boundaries
            spawnPosition = new Point(random.nextInt(1100), random.nextInt(900));

            // Repeat until the spawn position is at a safe distance from the player
        } while (spawnPosition.distance(playerPosition) < SAFE_DISTANCE);

        // Set the spawn position of the Minotaurus
        setCenter(spawnPosition);


        setTeam(Team.FOE);
        setColor(Color.RED);

        setDeltaX(somePosNegValue(5));
        setDeltaY(somePosNegValue(5));


        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.MINOTAU_MOVE, loadGraphic("/imgs/minotau/tile000.png"));
        setRasterMap(rasterMap);


    }

    public Minotaurus(Minotaurus astExploded){
        this(astExploded.getSize() + 1);
        setCenter(astExploded.getCenter());
        int newSmallerSize = astExploded.getSize() + 1;
        //random delta-x : inertia + the smaller the asteroid, the faster its possible speed
        setDeltaX(astExploded.getDeltaX() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));
        //random delta-y : inertia + the smaller the asteroid, the faster its possible speed
        setDeltaY(astExploded.getDeltaY() / 1.5 + somePosNegValue( 5 + newSmallerSize * 2));

    }

    //converts the radius to integer representing the size of the Asteroid:
    //0 = large, 1 = medium, 2 = small
    public int getSize(){
        return 0;
    }

    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, getRasterMap().get(Minotaurus.ImageState.MINOTAU_MOVE));
    }

    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        spawnSmallerMonsterOrDebris(this);
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
        Sound.playSound("kapow.wav");

    }
    //After Minotautus Died it will spawn monster or Debris
    private void spawnSmallerMonsterOrDebris(Minotaurus originalMinotaurus) {

        int size = originalMinotaurus.getSize();
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalMinotaurus), GameOp.Action.ADD);
        }
        else {
            size += 1;
            while (size-- > 0) {
                CommandCenter.getInstance().getOpsQueue().enqueue(new Monster(size), GameOp.Action.ADD);
            }
        }

    }

}
