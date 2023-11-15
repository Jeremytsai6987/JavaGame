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


public class Minotaurus extends Sprite {
    // shooting
    private static final Random random = new Random();
    private static final double SHOOTING_PROBABILITY = 0.06; // Adjust as needed

    //shoot the bullet

    public enum ImageState {

        MINOTAU_MOVE,

        //UP,
        MINOTAU_RIGHT,

        MINOTAU_LEFT,


    }
    public enum TurnState {IDLE, LEFT, RIGHT, DOWN, UP}
    private Minotaurus.TurnState turnState = Minotaurus.TurnState.IDLE;
    public void attemptToShoot() {
        if (random.nextDouble() < SHOOTING_PROBABILITY) {
            Axe axe = new Axe(this);
            CommandCenter.getInstance().getOpsQueue().enqueue(axe, GameOp.Action.ADD);
        }
    }
    //size determines if the Asteroid is Large (0), Medium (1), or Small (2)
    public Minotaurus(int size){

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
        setDeltaX(somePosNegValue(20));
        //random delta-y
        setDeltaY(somePosNegValue(20));

        //setCartesians(generateVertices());

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.MINOTAU_LEFT, loadGraphic("/imgs/minotau/tile000.png"));
        rasterMap.put(ImageState.MINOTAU_RIGHT, loadGraphic("/imgs/minotau/tile001.png"));
        rasterMap.put(ImageState.MINOTAU_MOVE, loadGraphic("/imgs/minotau/tile000.png"));
        setRasterMap(rasterMap);


    }



    //overloaded constructor, so we can spawn smaller asteroids from an exploding one
    public Minotaurus(Minotaurus astExploded){
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
        renderRaster((Graphics2D) g, getRasterMap().get(Minotaurus.ImageState.MINOTAU_MOVE));
    }

    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        spawnSmallerMonsterOrDebris(this);
        //give the user some points for destroying the asteroid
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10L * (getSize() + 1));
        Sound.playSound("kapow.wav");

    }
    private void spawnSmallerMonsterOrDebris(Minotaurus originalMinotaurus) {

        int size = originalMinotaurus.getSize();
        //small asteroids
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalMinotaurus), GameOp.Action.ADD);
        }
        //med and large
        else {
            //for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller asteroids respectively
            //We can use the existing variable (size) to do this
            size += 2;
            while (size-- > 0) {
                CommandCenter.getInstance().getOpsQueue().enqueue(new Monster(size), GameOp.Action.ADD);
            }
        }

    }

}
