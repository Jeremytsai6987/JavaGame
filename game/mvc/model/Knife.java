package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

// Generate Knife For BigBoss
public class Knife extends Sprite {
    public enum ImageState {

        KNIFE_MOVE,
    }


    public Knife(BigBoss bigboss) {

        setTeam(Team.FOE);
        setColor(Color.YELLOW);

        setExpiry(10);
        setRadius(70);

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(Knife.ImageState.KNIFE_MOVE, loadGraphic("/imgs/weapon/fire.png") );
        setRasterMap(rasterMap);


        setCenter(bigboss.getCenter());

        setOrientation(bigboss.getOrientation());

        final double FIRE_POWER = 20.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(bigboss.getDeltaX() + vectorX);
        setDeltaY(bigboss.getDeltaY() + vectorY);


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

        renderRaster((Graphics2D) g, getRasterMap().get(Knife.ImageState.KNIFE_MOVE));
    }


    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("getout.wav");

    }
}
