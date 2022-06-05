package at.fhv.sysarch.lab4.game;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

import java.util.Set;

public class StrikeCollisionListener implements ContactListener {

    private final Cue cue;
    private final Set<Ball> balls;
    private final BallStrikeListener listener;


    public StrikeCollisionListener(Cue cue, Set<Ball> balls, BallStrikeListener listener) {
        this.cue = cue;
        this.balls = balls;
        this.listener = listener;
    }

    @Override
    public boolean begin(ContactPoint point) { return true; }


    @Override
    public void sensed(ContactPoint point) { }

    @Override
    public void end(ContactPoint point) {

        var b1 = point.getBody1();
        var b2 = point.getBody2();

        if(cue.getBody().equals(b1) || cue.getBody().equals(b2)) {

            var struckBall = balls
                    .stream()
                    .filter(b -> b.getBody().equals(b1) || b.getBody().equals(b2)).findAny();

            struckBall.ifPresent(listener::onBallStrike);
        }
    }

    @Override
    public boolean persist(PersistedContactPoint point) { return true; }

    @Override
    public boolean preSolve(ContactPoint point) { return true; }

    @Override
    public void postSolve(SolvedContactPoint point) { }
}
