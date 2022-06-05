package at.fhv.sysarch.lab4.game;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.HalfEllipse;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

public class Cue {

    // Cue collides with Balls
    public static class CollisionFilter extends Ball.CollisionFilter { }

    private final Body body;

    private final HalfEllipse tip;



    private final Rectangle shaft;

    public Cue() {

        this.body = new Body();

        this.tip = Geometry.createHalfEllipse(
                Constants.TIP_DIAMETER,
                Constants.TIP_THICKNESS
        );
        this.tip.rotate(-Math.PI * .5);
        this.body.addFixture(tip, Constants.DENSITY);

        this.shaft = Geometry.createRectangle(Constants.LENGTH, Constants.TIP_DIAMETER);
        this.shaft.translate(Constants.LENGTH / 2, 0);
        this.body.addFixture(shaft, Constants.DENSITY);

        this.body.translateToOrigin();
        this.body.setMass(MassType.NORMAL);

        var collisionFilter = new CollisionFilter();
        this.body.getFixtures().forEach(f -> f.setFilter(collisionFilter));
    }

    public Body getBody() {
        return this.body;
    }

    public HalfEllipse getTip() {
        return tip;
    }

    public Rectangle getShaft() {
        return shaft;
    }

    public static class Constants {
        // SI units - meters, kg

        public static final double LENGTH = 1.45;
        public static final double MASS = 0.54;
        public static final double TIP_DIAMETER = 0.013;
        public static final double TIP_THICKNESS = 0.01;
        public static final double VOLUME = LENGTH * TIP_DIAMETER ;
        public static final double DENSITY = MASS / VOLUME;
    }
}
