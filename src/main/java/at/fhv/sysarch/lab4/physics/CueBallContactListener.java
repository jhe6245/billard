package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;

@FunctionalInterface
public interface CueBallContactListener {
    void onCueBallContact(Ball b);
}