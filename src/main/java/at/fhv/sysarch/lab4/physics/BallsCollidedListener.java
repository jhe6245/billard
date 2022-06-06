package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;

@FunctionalInterface
public interface BallsCollidedListener {
    void onBallsCollided(Ball a, Ball b);
}
