package at.fhv.sysarch.lab4.game;

import java.util.*;
import java.util.stream.Collectors;

import at.fhv.sysarch.lab4.physics.dispatchers.BoundsDispatcher;
import at.fhv.sysarch.lab4.physics.dispatchers.ContactDispatcher;
import at.fhv.sysarch.lab4.physics.dispatchers.StepDispatcher;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final World world;
    private final Renderer renderer;

    private final Table table;
    private final Cue cue;

    private Vector2 dragStart = null;
    private Vector2 dragCurrent = null;
    private Turn turn;


    public Game(Renderer renderer) {
        this.renderer = renderer;

        this.world = new World();
        this.cue = new Cue();
        this.table = new Table();

        this.renderer.setFrameListener(world::update);

        this.initWorld();

        this.turn = new Turn(Player.PLAYER_ONE, Ball.WHITE.getPosition());
    }

    public void onMousePressed(MouseEvent e) {
        if(!turn.canStrike())
            return;

        double pX = this.renderer.screenToPhysicsX(e.getX());
        double pY = this.renderer.screenToPhysicsY(e.getY());

        dragStart = new Vector2(pX, pY);

        cue.deactivateCollision();
        cue.stop();
    }

    public void onMouseDragged(MouseEvent e) {
        if(!turn.canStrike() || dragStart == null)
            return;

        double pX = this.renderer.screenToPhysicsX(e.getX());
        double pY = this.renderer.screenToPhysicsY(e.getY());

        dragCurrent = new Vector2(pX, pY);

        var cueDirection = new Vector2(dragCurrent).subtract(dragStart).getDirection();

        cue.setPosition(dragStart, cueDirection);
    }

    public void onMouseReleased(MouseEvent e) {

        if(!turn.canStrike() || this.dragStart == null || this.dragCurrent == null)
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

        world.getBodies().forEach(b -> b.setAsleep(true));

        world.addListener(new ContactDispatcher(cue, Set.of(Ball.values()), this::onCueBallContact, this::onBallsCollided, this::onBallPocketed));
        world.addListener(new StepDispatcher(this::onObjectsRest));
        world.addListener(new BoundsDispatcher(cue, this::onCueMissed));
    }

    private void onCueBallContact(Ball b) {
        if(!turn.canStrike()) {
            return;
        }

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

        b.getBody().setActive(false);
    }

    private void onObjectsRest() {
        if(turn.canStrike())
            return;

        System.out.println("at rest");

        var pocketedObjectBalls = Arrays.stream(Ball.values())
                .filter(b -> b != Ball.WHITE)
                .filter(b -> !b.getBody().isActive())
                .collect(Collectors.toList());

        if(pocketedObjectBalls.size() >= 14) {
            placeBalls(pocketedObjectBalls);
            pocketedObjectBalls.forEach(b -> b.getBody().setActive(true));
        }

        if(turn.isWhitePocketed()) {

            Ball.WHITE.setPosition(turn.getWhiteBallInitialPosition().x, turn.getWhiteBallInitialPosition().y);
            Ball.WHITE.getBody().setLinearVelocity(0, 0);

            Ball.WHITE.getBody().setActive(true);
        }

        if(turn.isFoul()) {
            renderer.setFoulMessage(turn.getPlayer() + " fouled: " + turn.getFoulInformation());
        } else {
            renderer.setFoulMessage("");
        }

        renderer.setActionMessage(turn.getPlayer() + " " + turn.getMessage());

        var score = turn.getScore();

        if(turn.getPlayer() == Player.PLAYER_ONE) {
            renderer.incrementPlayer1Score(score);
        } else {
            renderer.incrementPlayer2Score(score);
        }

        renderer.setStrikeMessage("Next strike: " + turn.getNextPlayer());

        cue.reset();

        this.turn = new Turn(turn.getNextPlayer(), Ball.WHITE.getPosition());
    }


}