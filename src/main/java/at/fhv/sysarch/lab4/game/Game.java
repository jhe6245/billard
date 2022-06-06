package at.fhv.sysarch.lab4.game;

import java.util.*;

import at.fhv.sysarch.lab4.physics.BoundsDispatcher;
import at.fhv.sysarch.lab4.physics.ContactDispatcher;
import at.fhv.sysarch.lab4.physics.StepDispatcher;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final World world;
    private final Renderer renderer;

    private final Table table;
    private final Cue cue;

    private Vector2 dragStart = null;
    private Vector2 dragCurrent = null;

    private boolean playerOne = true;

    private Turn turn;
    private Vector2 originalWhitePosition;


    public Game(Renderer renderer) {
        this.renderer = renderer;

        this.world = new World();
        this.cue = new Cue();
        this.table = new Table();

        this.renderer.setFrameListener(world::update);

        this.initWorld();

        this.turn = new Turn();
        this.originalWhitePosition = Ball.WHITE.getPosition();
    }

    public void onMousePressed(MouseEvent e) {
        double pX = this.renderer.screenToPhysicsX(e.getX());
        double pY = this.renderer.screenToPhysicsY(e.getY());

        dragStart = new Vector2(pX, pY);

        cue.deactivateCollision();
        cue.stop();
    }

    public void onMouseDragged(MouseEvent e) {
        double pX = this.renderer.screenToPhysicsX(e.getX());
        double pY = this.renderer.screenToPhysicsY(e.getY());

        dragCurrent = new Vector2(pX, pY);

        var cueDirection = new Vector2(dragCurrent).subtract(dragStart).getDirection();

        cue.setPosition(dragStart, cueDirection);
    }

    public void onMouseReleased(MouseEvent e) {

        if(this.dragStart == null || this.dragCurrent == null)
            return;

        var impulse = new Vector2(dragStart).subtract(dragCurrent).multiply(3);
        impulse.setMagnitude(Math.min(impulse.getMagnitude(), 3));

        cue.activateCollision();
        cue.setVelocity(impulse, 0);

        this.dragStart = this.dragCurrent = null;
    }

    private static void placeWhite(Ball b) {
        b.setPosition(Table.Constants.WIDTH * 0.25, 0);
        b.getBody().setLinearVelocity(0, 0);
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billiard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {

            if(b.isWhite()) {
                placeWhite(b);
                continue;
            }

            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {

        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.setBounds(new AxisAlignedBounds(10, 10));

        var balls = new ArrayList<>(List.of(Ball.values()));
        placeBalls(balls);

        balls.forEach(renderer::addBall);
        renderer.setTable(table);
        renderer.setCue(this.cue);

        balls.forEach(b -> world.addBody(b.getBody()));
        world.addBody(table.getBody());
        world.addBody(cue.getBody());

        world.addListener(new ContactDispatcher(cue, Set.of(Ball.values()), this::onBallStrike, this::onBallsCollided, this::onBallPocketed));
        world.addListener(new StepDispatcher(this::onObjectsRest));
        world.addListener(new BoundsDispatcher(cue, this::onCueMissed));
    }

    private String currentPlayer() {
        return "Player " + (playerOne ? 1 : 2);
    }

    private void onBallStrike(Ball b) {
        System.out.println(b + " hit");

        cue.stop();
        cue.deactivateCollision();

        turn.cueStrike(b);
    }

    private void onCueMissed() {
        System.out.println("cue missed");

        cue.reset();
    }

    private void onBallsCollided(Ball a, Ball b) {
        turn.ballCollision(a, b);
    }

    private void onBallPocketed(Ball b) {
        System.out.println(b + " pocketed");

        turn.ballPocketed(b);


        world.removeBody(b.getBody());
        renderer.removeBall(b);
    }

    private void onObjectsRest() {
        System.out.println("at rest");

        if(turn.isWhitePocketed()) {
            Ball.WHITE.setPosition(originalWhitePosition.x, originalWhitePosition.y);
            renderer.addBall(Ball.WHITE);
            world.addBody(Ball.WHITE.getBody());
        }

        if(turn.isFoul()) {
            renderer.setFoulMessage(currentPlayer() + " fouled: " + turn.getFoulInformation());
        } else {
            renderer.setFoulMessage("");
        }

        renderer.setActionMessage(currentPlayer() + " " + turn.getMessage());

        var score = turn.getScore();

        if(playerOne) {
            renderer.incrementPlayer1Score(score);
        } else {
            renderer.incrementPlayer2Score(score);
        }

        playerOne ^= true;

        renderer.setStrikeMessage("Next strike: " + currentPlayer());

        cue.reset();

        this.turn = new Turn();
        this.originalWhitePosition = Ball.WHITE.getPosition();
    }


}