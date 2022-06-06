package at.fhv.sysarch.lab4.physics;

import org.dyn4j.dynamics.*;

public class StepDispatcher extends StepAdapter {

    private final ObjectsRestListener onObjectsRestListener;

    private boolean atRestLastStep = true;

    public StepDispatcher(ObjectsRestListener onObjectsRestListener) {
        this.onObjectsRestListener = onObjectsRestListener;
    }

    @Override
    public void end(Step step, World world) {

        var objectsAtRest = world.getBodies().stream().allMatch(b -> b.getLinearVelocity().getMagnitude() < .01);

        // todo ???
        var bodies = world.getBodies().size();
        var sleeping = world.getBodies().stream().filter(Body::isAsleep).count();
        System.out.println(100.0 * sleeping / bodies + "% sleeping");

        if(objectsAtRest && !atRestLastStep) {
            onObjectsRestListener.onObjectsAtRest();
        }

        atRestLastStep = objectsAtRest;
    }
}
