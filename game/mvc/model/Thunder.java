package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

// Weapon Thunder Image State
public class Thunder extends Sprite {
    public enum ImageState {
        THUNDER_MOVE,
    }


    public Thunder(Monster monster) {

        setTeam(Team.FOE);
        setColor(Color.YELLOW);

        //a thunder expires after 10 frames.
        setExpiry(10);
        setRadius(30);

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(Thunder.ImageState.THUNDER_MOVE, loadGraphic("/imgs/weapon/thunder.png") ); //normal ship
        setRasterMap(rasterMap);


        //everything is relative to the monster that fired the bullet
        setCenter(monster.getCenter());

        //set the thunder orientation to the monster orientation
        setOrientation(monster.getOrientation());

        final double FIRE_POWER = 20.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: monster inertia + fire-vector
        setDeltaX(monster.getDeltaX() + vectorX);
        setDeltaY(monster.getDeltaY() + vectorY);



        //define the points on a cartesian grid
        List<Point> listPoints = new ArrayList<>();
        listPoints.add(new Point(0, 3)); //top point
        listPoints.add(new Point(1, -1)); //right bottom
        listPoints.add(new Point(0, 0));
        listPoints.add(new Point(-1, -1)); //left bottom

        setCartesians(listPoints.toArray(new Point[0]));




    }


    @Override
    public void draw(Graphics g) {

        renderRaster((Graphics2D) g, getRasterMap().get(ImageState.THUNDER_MOVE));
    }


    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("explosion.wav");

    }
}
