package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import at.fhv.sysarch.lab4.game.Cue;
import at.fhv.sysarch.lab4.game.Table;
import org.dyn4j.collision.Fixture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.*;

import java.util.Set;

public class ContactDispatcher extends ContactAdapter {

    private final Cue cue;
    private final Set<Ball> balls;

    private final BallStrikeListener strikeListener;
    private final BallPocketedListener pocketedListener;

    public ContactDispatcher(Cue cue, Set<Ball> balls, BallStrikeListener strikeListener, BallPocketedListener pocketedListener) {
        this.cue = cue;
        this.balls = balls;

        this.strikeListener = strikeListener;
        this.pocketedListener = pocketedListener;
    }

    private boolean isCue(Body body) {
        return cue.getBody().equals(body);
    }

    private static boolean isPocket(Fixture fixture) {
        return Table.TablePart.POCKET.equals(fixture.getUserData());
    }

    // called while objects touch
    // used to detect pocketing balls if they overlap enough
    @Override
    public boolean persist(PersistedContactPoint point) {
        var b1 = point.getBody1();
        var b2 = point.getBody2();

        var f1 = point.getFixture1();
        var f2 = point.getFixture2();

        var struckBallO = balls
                .stream()
                .filter(b -> b.getBody().equals(b1) || b.getBody().equals(b2)).findAny();

        if(struckBallO.isEmpty())
            return true;

        var struckBall = struckBallO.get();

        if(isPocket(f1) || isPocket(f2)) {

            var ballRadius = struckBall.getBody().getRotationDiscRadius();

            if(point.getDepth() > ballRadius) {

                pocketedListener.onBallPocketed(struckBall);
            }
        }
        return true;
    }

    // called when objects stop touching
    // used to detect hitting balls with cue
    @Override
    public void end(ContactPoint point) {
        var b1 = point.getBody1();
        var b2 = point.getBody2();

        if(isCue(b1) || isCue(b2)) {
            balls.stream()
                    .filter(b -> b.getBody().equals(b1) || b.getBody().equals(b2))
                    .findAny()
                    .ifPresent(strikeListener::onBallStrike);
        }
    }
}
