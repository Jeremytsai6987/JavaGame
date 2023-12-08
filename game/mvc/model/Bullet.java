package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Sound;
import edu.uchicago.gerber._08final.mvc.controller.Utils;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
// Build the Bullet Image State
public class Bullet extends Sprite {

    public enum ImageState {
        Bullet_FIRE,
        BULLET_MOVE
    }



    public Bullet(Chicken chicken, int orientation) {

        setTeam(Team.FRIEND);
        setColor(Color.ORANGE);

        //a bullet expires after 20 frames.
        setExpiry(20);
        setRadius(60); //set from 6 to 60

        //HashMap for drawing
        Map<Bullet.ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(ImageState.Bullet_FIRE, loadGraphic("/imgs/weapon/javelin attack 1 sheet.png"));
        rasterMap.put(ImageState.BULLET_MOVE, loadGraphic("/imgs/weapon/javelin attack 1.gif") );
        setRasterMap(rasterMap);

        setCenter(chicken.getCenter());

        //set the bullet orientation to the chicken orientation
        if (chicken.getLastDirection() == 180){
            setOrientation(180);

        }else {
            setOrientation(0);
        }

        final double FIRE_POWER = 35.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: chicken inertia + fire-vector
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
        renderRaster((Graphics2D) g, getRasterMap().get(ImageState.BULLET_MOVE));

    }

    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("oowa.wav");

    }
}
