package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;

@FunctionalInterface
public interface BallStrikeListener {
    void onBallStrike(Ball b);
}