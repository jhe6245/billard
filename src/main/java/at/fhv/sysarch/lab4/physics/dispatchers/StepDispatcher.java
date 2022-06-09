package at.fhv.sysarch.lab4.physics.dispatchers;

import at.fhv.sysarch.lab4.game.Ball;
import at.fhv.sysarch.lab4.physics.BallsSettledListener;
import org.dyn4j.dynamics.*;

public class StepDispatcher extends StepAdapter {

    private final BallsSettledListener onBallsSettledListener;

    private boolean atRestLastStep = true;

    public StepDispatcher(BallsSettledListener settledListener) {
        this.onBallsSettledListener = settledListener;
    }

    @Override
    public void end(Step step, World world) {

        boolean objectsAtRest =  world.getBodies()
                .stream()
                .filter(b -> b.getUserData() instanceof Ball)
                .filter(Body::isActive)
                .allMatch(Body::isAsleep);

        if(objectsAtRest && !atRestLastStep) {
            onBallsSettledListener.onBallsSettled();
        }

        atRestLastStep = objectsAtRest;
    }
}
