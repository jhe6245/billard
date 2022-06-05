package at.fhv.sysarch.lab4.game;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.HalfEllipse;
import org.dyn4j.geometry.MassType;

public class Cue {
    private final Body body;

    private final HalfEllipse shape;

    public Cue() {

        this.body = new Body();

        this.shape = Geometry.createHalfEllipse(
                Constants.TIP_DIAMETER,
                Constants.TIP_THICKNESS
        );
        this.shape.rotate(-Math.PI * .5);
        this.body.addFixture(shape, Constants.DENSITY);

        this.body.translateToOrigin();
        this.body.setMass(MassType.FIXED_ANGULAR_VELOCITY);
    }

    public Body getBody() {
        return this.body;
    }

    public HalfEllipse getShape() {
        return shape;
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
