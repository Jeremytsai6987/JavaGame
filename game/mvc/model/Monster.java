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

// Monster Class(Spawn After Minotaurus Died)
public class Monster extends Sprite {
    // shooting
    private static final Random random = new Random();
    private static final double SHOOTING_PROBABILITY = 0.04; // Adjust as needed

    //shoot the bullet

    public enum ImageState {

        MONSTER_MOVE,

    }
    //Let it attack Chicken by this method
    public void attemptToShoot() {
        if (random.nextDouble() < SHOOTING_PROBABILITY) {
            Thunder thunder = new Thunder(this);
            CommandCenter.getInstance().getOpsQueue().enqueue(thunder, GameOp.Action.ADD);
        }
    }
    public Monster(int size){
        int LARGE_RADIUS = 40;
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

        // Set the spawn position of the Monster
        setCenter(spawnPosition);



        setTeam(Team.FOE);
        setColor(Color.RED);


        setDeltaX(somePosNegValue(5));
        setDeltaY(somePosNegValue(5));


        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.MONSTER_MOVE, loadGraphic("/imgs/monster/monster.gif"));
        setRasterMap(rasterMap);


    }

    public int getSize(){
        return 0;
    }

    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, getRasterMap().get(ImageState.MONSTER_MOVE));
    }

    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        spawnSmallerMonsterOrDebris(this);
        //give the user some points for destroying the asteroid
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
        Sound.playSound("kapow.wav");

    }


    private void spawnSmallerMonsterOrDebris(Monster originalMonster) {

        int size = originalMonster.getSize();
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalMonster), GameOp.Action.ADD);
        }


    }

}
