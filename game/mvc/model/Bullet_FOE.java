package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

// Build FOE's Bullet Image State
public class Bullet_FOE extends Sprite {
    public enum ImageState {
        Bullet_FIRE,
        Bullet_MOVE
    }


    public Bullet_FOE(Karbi karbi) {

        setTeam(Team.FOE);
        setColor(Color.RED);

        //a bullet expires after 20 frames.
        setExpiry(10);
        setRadius(20); //set from 6 to 2q0

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(Bullet_FOE.ImageState.Bullet_FIRE, loadGraphic("/imgs/weapon/arrow.png"));
        rasterMap.put(Bullet_FOE.ImageState.Bullet_MOVE, loadGraphic("/imgs/weapon/arrow.png"));

        setRasterMap(rasterMap);


        //everything is relative to the Karbi that fired the bullet
        setCenter(karbi.getCenter());

        //set the bullet orientation to the Karbi's orientation
        setOrientation(karbi.getOrientation());

        final double FIRE_POWER = 10.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        setDeltaX(karbi.getDeltaX() + vectorX);
        setDeltaY(karbi.getDeltaY() + vectorY);

        //we have a reference to the Karbi passed into the constructor. Let's create some kick-back.
        //fire kick-back on the falcon: inertia - fire-vector / some arbitrary divisor
        final double KICK_BACK_DIVISOR = 18.0;
        karbi.setDeltaX(karbi.getDeltaX() - vectorX / KICK_BACK_DIVISOR);
        karbi.setDeltaY(karbi.getDeltaY() - vectorY / KICK_BACK_DIVISOR);


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
        renderRaster((Graphics2D) g, getRasterMap().get(Bullet_FOE.ImageState.Bullet_MOVE));

    }

    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("karbi.wav");

    }
}
