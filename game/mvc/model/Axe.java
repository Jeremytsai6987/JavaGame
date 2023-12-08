package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
// Class of Weapon Axe
public class Axe extends Sprite {
    // Set the Axe Image State
    public enum ImageState {
        AXE_MOVE,
    }

    // Set the Basic info about Axe
    public Axe(Minotaurus minotaurus) {

        setTeam(Team.FOE);
        setColor(Color.YELLOW);

        setExpiry(10);
        setRadius(70);

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(Axe.ImageState.AXE_MOVE, loadGraphic("/imgs/weapon/darkeffect.gif") );
        setRasterMap(rasterMap);


        //everything is relative to the Minotaurus that fired the Axe
        setCenter(minotaurus.getCenter());

        //set the axe orientation to the Minotaurus orientation
        setOrientation(minotaurus.getOrientation());

        final double FIRE_POWER = 20.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(minotaurus.getDeltaX() + vectorX);
        setDeltaY(minotaurus.getDeltaY() + vectorY);

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
        Sound.playSound("mooo.wav");

    }
}
