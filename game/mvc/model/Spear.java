package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

// Image State of Spear
public class Spear extends Sprite {

    public enum ImageState {
        SPEAR_FIRE,
        SPEAR_MOVE
    }



    public Spear(Chicken chicken, int orientation) {

        setTeam(Team.FRIEND);
        setColor(Color.black);

        //a bullet expires after 20 frames.
        setExpiry(20);
        setRadius(45); //set from 6 to 60

        //HashMap for drawing
        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.SPEAR_FIRE, loadGraphic("/imgs/weapon/egg.png"));
        rasterMap.put(ImageState.SPEAR_MOVE, loadGraphic("/imgs/weapon/egg.png") );
        setRasterMap(rasterMap);

        setCenter(chicken.getCenter());

        //set the spear orientation to the chicken orientation
        if (chicken.getLastDirection() == 180){
            setOrientation(180);

        }else {
            setOrientation(0);
        }

        final double FIRE_POWER = 50.0; //from 45 to 60
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(chicken.getDeltaX() + vectorX);
        setDeltaY(chicken.getDeltaY() + vectorY);


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
        renderRaster((Graphics2D) g, getRasterMap().get(ImageState.SPEAR_MOVE));

    }

    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("spear.wav");

    }
}
