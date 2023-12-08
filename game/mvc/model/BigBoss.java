package edu.uchicago.gerber._08final.mvc.model;


import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.*;
import java.util.Map;
import java.util.Random;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.GameOp;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

// Set the Class for the Monster BigBoss
public class BigBoss extends Sprite {
    // shooting
    private int hitCount = 0; // Field to track number of hits
    private boolean isMarkedForRemoval = false; // Flag to indicate removal status


    private static  int TOTAL_HITS_REQUIRED = 60;
    private static final int BAR_HEIGHT = 8;
    private static final Random random = new Random();
    private static final double SHOOTING_PROBABILITY = 0.02; // Adjust as needed


    // Set the image state
    public enum ImageState {
        BIGBOSS_MOVE,
        BIGBOSS_RIGHT,
        BIGBOSS_LEFT,

    }

    // Let the BigBoss can attack.
    public void attemptToShoot() {
        if (random.nextDouble() < SHOOTING_PROBABILITY) {
            Knife knife = new Knife(this);
            CommandCenter.getInstance().getOpsQueue().enqueue(knife, GameOp.Action.ADD);
        }
    }

    public BigBoss(int size){

        int LARGE_RADIUS = 100;
        setRadius(LARGE_RADIUS);

        // Let the BigBoss not spawn near the Chicken
        final int SAFE_DISTANCE = 150; // Adjust this value as needed

        // Obtain the player character's position
        Point playerPosition = CommandCenter.getInstance().getChicken().getCenter();
        Point spawnPosition;

        do {
            // Generate a random spawn position within the game boundaries
            spawnPosition = new Point(random.nextInt(1100), random.nextInt(900));

            // Repeat until the spawn position is at a safe distance from the player
        } while (spawnPosition.distance(playerPosition) < SAFE_DISTANCE);

        // Set the spawn position of the BigBoss
        setCenter(spawnPosition);

        //BigBoss is FOE
        setTeam(Team.FOE);
        setColor(Color.RED);

        //the spin will be either plus or minus 0-ㄓ
        //setSpin(somePosNegValue(10));
        //random delta-x
        setDeltaX(somePosNegValue(5));
        //random delta-y
        setDeltaY(somePosNegValue(5));

        //setCartesians(generateVertices());

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.BIGBOSS_MOVE, loadGraphic("/imgs/bigboss/tile001.png"));
        setRasterMap(rasterMap);


    }

    public int getSize(){
        return 0;
    }

    @Override
    //Draw a horizontal bar to show the amount of life that the boss left
    public void draw(Graphics g) {
        // Draw BigBoss sprite
        renderRaster((Graphics2D) g, getRasterMap().get(BigBoss.ImageState.BIGBOSS_MOVE));
        // Calculate the proportion of hits remaining
        double hitProportion = (double) (TOTAL_HITS_REQUIRED - hitCount) / TOTAL_HITS_REQUIRED;

        // Bar width is fixed to twice the radius of the BigBoss
        int barTotalWidth = 2 * getRadius();
        // Calculate the current width of the bar based on the hit proportion
        int currentBarWidth = (int) (barTotalWidth * hitProportion);

        // Position the bar above the BigBoss
        int barX = getCenter().x - getRadius();
        int barY = getCenter().y - getRadius() - BAR_HEIGHT - 5;

        g.setColor(Color.RED);
        g.fillRect(barX, barY, currentBarWidth, BAR_HEIGHT);
    }

    //increase the hitcount
    public void incrementHitCount() {
        hitCount++;
        if (hitCount >= TOTAL_HITS_REQUIRED) {
            markForRemoval();
        }
    }

    // Flag this BigBoss for removal
    private void markForRemoval() {
        isMarkedForRemoval = true;
    }




    public int getHitCount() {
        return this.hitCount;
    }

    public int setHitCount(int num) {return this.hitCount = num;}

    // Override the remove method to include the removal logic
    @Override
    public void remove(LinkedList<Movable> list) {
        if (isMarkedForRemoval) {
            super.remove(list);
            spawnSmallerBigbossOrDebris(this);
            // Award points for destroying the BigBoss
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
            Sound.playSound("kapow.wav");
        }
    }
    private void spawnSmallerBigbossOrDebris(BigBoss originalBigboss) {

        int size = originalBigboss.getSize();
        //small asteroids
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalBigboss), GameOp.Action.ADD);
            setHitCount(0);
        }


    }

}
