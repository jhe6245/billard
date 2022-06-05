package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final World world;
    private final Renderer renderer;

    private final Cue cue;

    private Vector2 dragStart = null;
    private Vector2 dragCurrent = null;

    public Game(Renderer renderer) {
        this.renderer = renderer;
        this.world = new World();

        this.cue = new Cue();
        this.cue.getBody().setActive(false);

        this.initWorld();

        this.renderer.setFrameListener(world::update);
    }

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        dragStart = new Vector2(pX, pY);
    }

    public void onMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        dragCurrent = new Vector2(pX, pY);

        var dragVector = new Vector2(dragCurrent);
        dragVector.subtract(dragStart);

        var cueTf = this.renderer.getCue().getBody().getTransform();

        cueTf.setTranslation(dragStart);
        cueTf.setRotation(dragVector.getDirection());

        var cueBody = this.renderer.getCue().getBody();

        cueBody.setLinearVelocity(new Vector2());
        cueBody.setAngularVelocity(0);

        cueBody.setActive(true);
    }

    public void onMouseReleased(MouseEvent e) {

        if(this.dragStart == null || this.dragCurrent == null)
            return;

        var impulse = new Vector2(dragCurrent);
        impulse.subtract(dragStart);
        impulse.multiply(-1);

        var cb = this.renderer.getCue().getBody();
        cb.applyImpulse(impulse);

        // System.out.println("drag from " + dragStart + " to " + dragCurrent + " applying " + impulse + " to cue");


        this.dragStart = this.dragCurrent = null;
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
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        this.world.setGravity(new Vector2());

        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }

        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        
        renderer.addBall(Ball.WHITE);
        
        Table table = new Table();
        renderer.setTable(table);

        renderer.setCue(this.cue);

        for (Ball b : Ball.values()) {
            world.addBody(b.getBody());
        }
        world.addBody(table.getBody());
        world.addBody(cue.getBody());

    }
}