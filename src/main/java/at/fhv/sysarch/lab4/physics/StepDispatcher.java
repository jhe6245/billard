package at.fhv.sysarch.lab4.physics;

import org.dyn4j.dynamics.*;

import java.util.stream.Collectors;

public class StepDispatcher extends StepAdapter {

    private final ObjectsRestListener onObjectsRestListener;

    private boolean atRestLastStep = true;

    public StepDispatcher(ObjectsRestListener onObjectsRestListener) {
        this.onObjectsRestListener = onObjectsRestListener;
    }

    @Override
    public void end(Step step, World world) {

        var objectsAtRest = world.getBodies().stream()
                .filter(Body::isActive)
                .allMatch(Body::isAsleep);

        // var moving = world.getBodies().stream().filter(Body::isActive).filter(b -> !b.isAsleep()).collect(Collectors.toList());

        if(objectsAtRest && !atRestLastStep) {
            onObjectsRestListener.onObjectsAtRest();
        }

        atRestLastStep = objectsAtRest;
    }
}
