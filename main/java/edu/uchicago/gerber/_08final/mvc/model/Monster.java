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


public class Monster extends Sprite {
    // shooting
    private static final Random random = new Random();
    private static final double SHOOTING_PROBABILITY = 0.04; // Adjust as needed

    //shoot the bullet

    public enum ImageState {

        MONSTER_MOVE,

        //UP,
        MONSTER_RIGHT,

        MONSTER_LEFT,


    }
    public enum TurnState {IDLE, LEFT, RIGHT, DOWN, UP}
    private Monster.TurnState turnState = Monster.TurnState.IDLE;
    public void attemptToShoot() {
        if (random.nextDouble() < SHOOTING_PROBABILITY) {
            Thunder thunder = new Thunder(this);
            CommandCenter.getInstance().getOpsQueue().enqueue(thunder, GameOp.Action.ADD);
        }
    }
    //size determines if the Asteroid is Large (0), Medium (1), or Small (2)
    public Monster(int size){

        //a size of zero is a big asteroid
        //a size of 1 or 2 is med or small asteroid respectively. See getSize() method.
        //if (size == 0) setRadius(LARGE_RADIUS);
        //else setRadius(LARGE_RADIUS/(size * 2));
        //radius of a large asteroid
        int LARGE_RADIUS = 140;
        setRadius(LARGE_RADIUS);

        //Asteroid is FOE
        setTeam(Team.FOE);
        setColor(Color.RED);

        //the spin will be either plus or minus 0-9
        //setSpin(somePosNegValue(10));
        //random delta-x
        setDeltaX(somePosNegValue(10));
        //random delta-y
        setDeltaY(somePosNegValue(10));

        //setCartesians(generateVertices());

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.MONSTER_MOVE, loadGraphic("/imgs/monster/monster.gif"));
        setRasterMap(rasterMap);


    }



    //overloaded constructor, so we can spawn smaller asteroids from an exploding one
    public Monster(Monster astExploded){
        //calls the other constructor: Asteroid(int size)
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
        //small asteroids
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalMonster), GameOp.Action.ADD);
        }
        //med and large


    }

}
