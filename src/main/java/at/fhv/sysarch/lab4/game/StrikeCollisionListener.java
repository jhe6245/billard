package at.fhv.sysarch.lab4.game;

import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionListener;
import org.dyn4j.dynamics.contact.ContactConstraint;

import java.util.Set;

public class StrikeCollisionListener implements CollisionListener {

    private final Cue cue;
    private final Set<Ball> balls;
    private final BallStrikeListener listener;


    public StrikeCollisionListener(Cue cue, Set<Ball> balls, BallStrikeListener listener) {
        this.cue = cue;
        this.balls = balls;
        this.listener = listener;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2) {

        if(cue.getBody().equals(body1) || cue.getBody().equals(body2)) {

            var struckBall = balls
                    .stream()
                    .filter(b -> b.getBody().equals(body1) || b.getBody().equals(body2)).findAny();

            struckBall.ifPresent(listener::onBallStrike);
        }

        return false;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {
        return collision(body1, fixture1, body2, fixture2);
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Manifold manifold) {
        return collision(body1, fixture1, body2, fixture2);

    }

    @Override
    public boolean collision(ContactConstraint contactConstraint) {
        return collision(
                contactConstraint.getBody1(),
                contactConstraint.getFixture1(),
                contactConstraint.getBody2(),
                contactConstraint.getFixture2()
        );
    }
}
