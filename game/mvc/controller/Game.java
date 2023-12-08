package edu.uchicago.gerber._08final.mvc.controller;

import edu.uchicago.gerber._08final.mvc.model.*;
import edu.uchicago.gerber._08final.mvc.view.GamePanel;
import lombok.Getter;


import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import static edu.uchicago.gerber._08final.mvc.model.Movable.Team.DEBRIS;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension
            DIM = new Dimension(1100, 900); //the dimension of the game.
    private final GamePanel gamePanel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 40; // milliseconds between frames

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;


    //key-codes
    private static final int
            PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            UP = 38, // thrust; up arrow
            DOWN = 40,  // down; down arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m-key mute
            SWITCH = 65; //switch weapon A Key


    private final Clip soundThrust;
    private final Clip soundBackground;
    public static boolean isGamePass = false; //Gamepass Flag


    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this); //Game object implements KeyListener
        soundThrust = Sound.clipForLoopFactory("whitenoise.wav");
        soundBackground = Sound.clipForLoopFactory("music-background.wav");

        //fire up the animation thread
        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        animationThread.setDaemon(true);
        animationThread.start();


    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {
        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }

    // Game implements runnable, and must have run method
    @Override
    public void run() {

        // lower animation thread's priority, thereby yielding to the 'Event Dispatch Thread' or EDT
        // thread which listens to keystrokes
        boolean isGamePass = false;

        animationThread.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long startTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {


            //this call will cause all movables to move() and draw() themselves every ~40ms
            // see GamePanel class for details
            gamePanel.update(gamePanel.getGraphics());

            // if we get Karbi, Minotaurus, Monster and BigBoss in the queue, we make them invoke attemptToShoot function
            // to attack chicken.
            for (Movable mov : CommandCenter.getInstance().getMovFoes()) {
                if (mov instanceof Karbi) {
                    ((Karbi) mov).attemptToShoot();
                }

                if (mov instanceof Minotaurus) {
                    ((Minotaurus) mov).attemptToShoot();
                }

                if (mov instanceof Monster) {
                    ((Monster) mov).attemptToShoot();
                }

                if (mov instanceof BigBoss) {
                    ((BigBoss) mov).attemptToShoot();
                }
            }

            checkCollisions();
            checkGamePass(); // check if the game pass from the normal mode to hard mode
            checkNewLevel();
            checkBossDied(); // To check if the Boss died



            //keep track of the frame for development purposes
            CommandCenter.getInstance().incrementFrame();

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
                // between frames takes longer than ANIMATION_DELAY, then the difference between startTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;

                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run


    // Check the collisions and let the chicken and the bigboss to be removed after hitting multiple times
    // To use the hit_count in the object's instance to record the count.
    private void checkCollisions() {
        int level = CommandCenter.getInstance().getLevel();


        //This has order-of-growth of O(FOES * FRIENDS)
        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();

                // Detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
                    // Check if the foe is BigBoss
                    if (movFoe instanceof BigBoss) {
                        BigBoss bigBoss = (BigBoss) movFoe;
                        bigBoss.incrementHitCount();
                        // Only enqueue for removal if hit 80 times
                        if (bigBoss.getHitCount() >= 80) {
                            CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        }
                    } else {
                        // Enqueue other foes normally
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                    }
                    // Enqueue the friend
                    if (movFriend instanceof Chicken) {
                        Chicken chicken = (Chicken) movFriend;
                        chicken.incrementHitCount();
                        if (chicken.getHitCount() >= 60) { // remove the chicken after hitting 60 times
                            CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                        }
                    }
                }
            }
        }



        processGameOpsQueue();

    }//end meth


    //This method adds and removes movables to/from their respective linked-lists.
    private void processGameOpsQueue() {

        // deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {

            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();

            //given team, determine which linked-list this object will be added-to or removed-from
            LinkedList<Movable> list;
            Movable mov = gameOp.getMovable();
            switch (mov.getTeam()) {
                case FOE:
                    list = CommandCenter.getInstance().getMovFoes();
                    break;
                case FRIEND:
                    list = CommandCenter.getInstance().getMovFriends();
                    break;
                case DEBRIS:
                default:
                    list = CommandCenter.getInstance().getMovDebris();
            }

            //pass the appropriate linked-list from above
            //this block will execute the add() or remove() callbacks in the Movable models.
            GameOp.Action action = gameOp.getAction();
            if (action == GameOp.Action.ADD)
                mov.add(list);
            else //REMOVE
                mov.remove(list);

        }//end while
    }







    //this method spawns new Large (0) Karbi
    private void spawnBigKarbi(int num) {
        while (num-- > 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Karbi(0), GameOp.Action.ADD);
        }
    }

    //this method spawns new Minotaurus
    private void spawnMinotaurus(int num) {
        while (num-- > 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Minotaurus(0), GameOp.Action.ADD);
        }
    }

    //this method spawns new BigBoss
    private void spawnBigBoss() {
        CommandCenter.getInstance().getOpsQueue().enqueue(new BigBoss(0), GameOp.Action.ADD);
    }



    //To check if there is any BigBoss on queue
    private boolean isBigBossClear() {
        boolean bossFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Karbi || movFoe instanceof Monster || movFoe instanceof Minotaurus || movFoe instanceof BigBoss) {
                bossFree = false;
                break;
            }
        }
        return bossFree;
    }
    // To check if the Boss died
    private void checkBossDied(){
        int level = CommandCenter.getInstance().getLevel();
        if (level > 0 && isBigBossClear()){
            levelUp(level);
            spawnKarbiAndMinotaurusAfterBoss(level);
        } else if (level == 0 && isBigBossClear()) {
            levelUp(level);

        }

    }

    // To check if the Game passes the normal mode
    private void checkGamePass(){
        int level = CommandCenter.getInstance().getLevel();
        if(level == 4){
            setisGamePass();}

    }
    // To spawn the Karbi and Minotaurus
    private void spawnKarbiAndMinotaurusAfterBoss(int level) {
        // Spawn Karbi and Minotaurus based on the level or any other logic
        spawnBigKarbi(level);
        spawnMinotaurus(level);
    }

    // Level up the game after satisfied the conditions
    private void levelUp(int level) {
        // Award points for having cleared the previous level
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + (10_000L * level));
        // Bump the level up
        level = level + 1;
        CommandCenter.getInstance().setLevel(level);

        // Spawn some big new Karbis and Minotaurs
        spawnBigKarbi(level);
        spawnMinotaurus(level);

        // Make Chicken invincible momentarily
        CommandCenter.getInstance().getChicken().setShield(Chicken.INITIAL_SPAWN_TIME);
        // Show "Level X" in the middle of the screen
        CommandCenter.getInstance().getChicken().setShowLevel(Chicken.INITIAL_SPAWN_TIME);
    }


    // Set the Game Pass flag
    public void setisGamePass(){ this.isGamePass = true;}


    // To check the new level
    private void checkNewLevel() {
        int level = CommandCenter.getInstance().getLevel();
        System.out.println(level);
        if (level > 0){
            if (isKarbiAndMinotaurusandMonsterClear()) {
                spawnBigBoss();
            }
        }


    }

    // To check if there is any Karbi, Minotaurus or Monster in the Queue
    private boolean isKarbiAndMinotaurusandMonsterClear() {
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Karbi || movFoe instanceof Minotaurus || movFoe instanceof Monster || movFoe instanceof BigBoss) {
                return false;
            }
        }
        return true;
    }




    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        Arrays.stream(clpClips).forEach(clip -> clip.stop());
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        Chicken chicken = CommandCenter.getInstance().getChicken();
        int keyCode = e.getKeyCode();

        if (keyCode == START && CommandCenter.getInstance().isGameOver()) {
            CommandCenter.getInstance().initGame();
            return;
        }


        switch (keyCode) {
            case PAUSE:
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                if (CommandCenter.getInstance().isPaused()) stopLoopingSounds(soundBackground, soundThrust);
                break;
            case QUIT:
                System.exit(0);
                break;
            case UP:
                chicken.setTurnState(Chicken.TurnState.UP);
                break;
            case LEFT:
                chicken.setTurnState(Chicken.TurnState.LEFT);
                chicken.setLastDirection(180);
                break;
            case RIGHT:
                chicken.setTurnState(Chicken.TurnState.RIGHT);
                chicken.setLastDirection(0);
                break;
            case DOWN:
                chicken.setTurnState(Chicken.TurnState.DOWN);
                break;

            default:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        Chicken chicken = CommandCenter.getInstance().getChicken();
        int keyCode = e.getKeyCode();
        //show the key-code in the console
        System.out.println(keyCode);
        // To let the chicken fire toward right orientation and switch between the Bullet and Spear.
        switch (keyCode) {
            case FIRE:
                if (chicken.getLastWeapon() == Chicken.WEAPON_STATE.BULLET){

                    int chickenOrientation = CommandCenter.getInstance().getChicken().getOrientation();
                    Bullet bullet = new Bullet(chicken, chickenOrientation);
                    CommandCenter.getInstance().getOpsQueue().enqueue(bullet, GameOp.Action.ADD);
                    break;
                }else{
                    int chickenOrientation = CommandCenter.getInstance().getChicken().getOrientation();
                    Spear spear = new Spear(chicken, chickenOrientation);
                    CommandCenter.getInstance().getOpsQueue().enqueue(spear, GameOp.Action.ADD);

                }

            //releasing either the LEFT or RIGHT arrow key will set the TurnState to IDLE
            case LEFT:
            case DOWN:
            case RIGHT:
            case UP:
                if (chicken.getTurnState() != Chicken.TurnState.IDLE) {
                    chicken.setTurnState(Chicken.TurnState.IDLE);
                }
                break;

            case MUTE:
                CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

                if (!CommandCenter.getInstance().isMuted()) {
                    stopLoopingSounds(soundBackground);
                } else {
                    soundBackground.loop(Clip.LOOP_CONTINUOUSLY);
                }
                break;
            case SWITCH: // Switch the weapon
                if(chicken.getLastWeapon() == Chicken.WEAPON_STATE.BULLET){
                    chicken.setWeapon(Chicken.WEAPON_STATE.SPEAR);

                }else{
                    chicken.setWeapon(Chicken.WEAPON_STATE.BULLET);
                }
                break;

            default:
                break;
        }

    }

    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }

}



