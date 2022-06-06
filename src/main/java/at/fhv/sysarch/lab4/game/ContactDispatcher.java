package at.fhv.sysarch.lab4.game;

import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

import java.util.Set;

public class ContactDispatcher implements ContactListener {

    private final Cue cue;
    private final Set<Ball> balls;
    private final BallStrikeListener strikeListener;
    private final Table table;
    private final BallPocketedListener pocketedListener;

    public ContactDispatcher(Cue cue, Set<Ball> balls, BallStrikeListener strikeListener, Table table, BallPocketedListener pocketedListener) {
        this.cue = cue;
        this.balls = balls;
        this.strikeListener = strikeListener;
        this.table = table;
        this.pocketedListener = pocketedListener;
    }

    @Override
    public boolean begin(ContactPoint point) {



        return true;
    }

    @Override
    public boolean persist(PersistedContactPoint point) {

        var b1 = point.getBody1();
        var b2 = point.getBody2();

        var struckBall = balls
                .stream()
                .filter(b -> b.getBody().equals(b1) || b.getBody().equals(b2)).findAny();
        if(struckBall.isEmpty())
            return true;


        if(cue.getBody().equals(b1) || cue.getBody().equals(b2)) {
            struckBall.ifPresent(strikeListener::onBallStrike);
        }
        else if((table.getBody().equals(b1) || table.getBody().equals(b2))) {
            if(point.getDepth() > struckBall.get().getBody().getRotationDiscRadius())
                if(Table.TablePart.POCKET.equals(point.getFixture1().getUserData()) || Table.TablePart.POCKET.equals(point.getFixture2().getUserData()))
                    pocketedListener.onBallPocketed(struckBall.get());
        }

        return true;
    }

    @Override
    public void end(ContactPoint point) { }
    @Override
    public void sensed(ContactPoint point) { }
    @Override
    public boolean preSolve(ContactPoint point) { return true; }
    @Override
    public void postSolve(SolvedContactPoint point) { }
}
