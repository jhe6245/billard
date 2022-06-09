package at.fhv.sysarch.lab4.physics.dispatchers;

import at.fhv.sysarch.lab4.game.Cue;
import at.fhv.sysarch.lab4.physics.CueMissedListener;
import org.dyn4j.collision.BoundsAdapter;
import org.dyn4j.collision.Collidable;
import org.dyn4j.collision.Fixture;

public class BoundsDispatcher extends BoundsAdapter {

    private final Cue cue;
    private final CueMissedListener missedListener;

    public BoundsDispatcher(Cue cue, CueMissedListener missedListener) {
        this.cue = cue;
        this.missedListener = missedListener;
    }

    @Override
    public <E extends Collidable<T>, T extends Fixture> void outside(E collidable) {
        if(collidable.equals(this.cue.getBody()))
            missedListener.onCueMissed();
    }
}
