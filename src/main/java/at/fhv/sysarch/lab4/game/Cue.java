package at.fhv.sysarch.lab4.game;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

public class Cue {

    // Cue collides with Balls
    public static class CollisionFilter extends Ball.CollisionFilter { }

    private final Body body;

    private final Rectangle geometry;

    public Cue() {

        this.body = new Body();

        this.geometry = Geometry.createRectangle(Constants.LENGTH, Constants.TIP_DIAMETER);
        this.geometry.translate(Constants.LENGTH / 2, 0);
        this.body.addFixture(geometry);

        // mass is infinite because striking the ball with the cue is not the same as throwing the cue at it.
        // this way the ball just takes on the same velocity as the cue when hit
        this.body.setMass(MassType.INFINITE);

        var collisionFilter = new CollisionFilter();
        this.body.getFixtures().forEach(f -> f.setFilter(collisionFilter));

        this.reset();
    }

    public Body getBody() {
        return this.body;
    }

    public Rectangle getGeometry() {
        return geometry;
    }

    public void setPosition(Vector2 tipPosition, double rotation) {
        body.getTransform().setTranslation(tipPosition);
        body.getTransform().setRotation(rotation);
    }

    public void setVelocity(Vector2 linear, double angular) {
        body.setLinearVelocity(linear);
        body.setAngularVelocity(angular);
    }

    public void stop() {
        setVelocity(new Vector2(), 0);
    }

    public void activateCollision() {
        body.setActive(true);
    }

    public void deactivateCollision() {
        body.setActive(false);
    }

    public void reset() {
        deactivateCollision();
        stop();

        setPosition(new Vector2(Table.Constants.WIDTH * .3, 0), 0);
    }



    public static class Constants {
        // meters, kg
        public static final double LENGTH = 1.45;
        public static final double TIP_DIAMETER = 0.013;
        public static final double TIP_THICKNESS = 0.01;
    }
}
