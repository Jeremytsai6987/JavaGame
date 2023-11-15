package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Sound;
import edu.uchicago.gerber._08final.mvc.controller.Utils;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Bullet_FOE extends Sprite {



    public Bullet_FOE(Asteroid asteroid) {

        setTeam(Team.FOE);
        setColor(Color.RED);

        //a bullet expires after 20 frames.
        setExpiry(20);
        setRadius(20); //set from 6 to 2q0


        //everything is relative to the falcon ship that fired the bullet
        setCenter(asteroid.getCenter());

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(asteroid.getOrientation());

        final double FIRE_POWER = 35.0;
        double vectorX =
                Math.cos(Math.toRadians(getOrientation())) * FIRE_POWER; //effective shooting range
        double vectorY =
                Math.sin(Math.toRadians(getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(asteroid.getDeltaX() + vectorX);
        setDeltaY(asteroid.getDeltaY() + vectorY);

        //we have a reference to the falcon passed into the constructor. Let's create some kick-back.
        //fire kick-back on the falcon: inertia - fire-vector / some arbitrary divisor
        final double KICK_BACK_DIVISOR = 18.0; //from 36 to 18
        asteroid.setDeltaX(asteroid.getDeltaX() - vectorX / KICK_BACK_DIVISOR);
        asteroid.setDeltaY(asteroid.getDeltaY() - vectorY / KICK_BACK_DIVISOR);


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
        renderVector(g);
    }

    @Override
    public void add(LinkedList<Movable> list) {
        super.add(list);
        Sound.playSound("thump.wav");

    }
}
