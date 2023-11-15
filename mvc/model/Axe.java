package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Axe extends Sprite {
    public enum ImageState {
        //AXE_LEFTFIRE,
        //AXE_RIGHTFIRE,
        AXE_MOVE,
    }


    public Axe(Minotaurus minotaurus) {

        setTeam(Team.FOE);
        setColor(Color.YELLOW);

        //a bullet expires after 20 frames.
        setExpiry(20);
        setRadius(70); //set from 6 to 2q0

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        //rasterMap.put(ImageState.AXE_LEFTFIRE, loadGraphic("/imgs/minotau/tile006.png"));
        //rasterMap.put(ImageState.AXE_RIGHTFIRE, loadGraphic("/imgs/minotau/tile001.png"));
        rasterMap.put(Axe.ImageState.AXE_MOVE, loadGraphic("/imgs/weapon/darkeffect.gif") ); //normal ship
        setRasterMap(rasterMap);


        //everything is relative to the falcon ship that fired the bullet
        setCenter(minotaurus.getCenter());

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(minotaurus.getOrientation());

        final double FIRE_POWER = 25.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(minotaurus.getDeltaX() + vectorX);
        setDeltaY(minotaurus.getDeltaY() + vectorY);

        //we have a reference to the falcon passed into the constructor. Let's create some kick-back.
        //fire kick-back on the falcon: inertia - fire-vector / some arbitrary divisor
        final double KICK_BACK_DIVISOR = 45.0; //from 36 to 18
        minotaurus.setDeltaX(minotaurus.getDeltaX() - vectorX / KICK_BACK_DIVISOR);
        minotaurus.setDeltaY(minotaurus.getDeltaY() - vectorY / KICK_BACK_DIVISOR);


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

        renderRaster((Graphics2D) g, getRasterMap().get(Axe.ImageState.AXE_MOVE));
    }


    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("axe.wav");

    }
}
