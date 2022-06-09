package at.fhv.sysarch.lab4.game;

import org.dyn4j.geometry.Vector2;

// turn holds the state of each time a player strikes the ball
// (NOT each time players are switched, it is a new turn each time the cue hits anything and the balls settle)
public class Turn {

    private final Player player;
    private final Vector2 whiteBallInitialPosition;

    private boolean struckWhiteButMissed = true; // hitting nothing with the cue is not considered a foul
    private boolean struckNonWhite = false;
    private boolean pocketedWhite = false;

    private int pocketed = 0;
    private boolean struckAny = false;

    public Turn(Player player, Vector2 whiteBallInitialPosition) {
        this.player = player;
        this.whiteBallInitialPosition = whiteBallInitialPosition;
    }

    public Player getPlayer() {
        return player;
    }

    public Vector2 getWhiteBallInitialPosition() {
        return whiteBallInitialPosition;
    }

    public Player getNextPlayer() {

        if (getAwardedScore() > 0)
            return player;

        return player == Player.PLAYER_ONE ? Player.PLAYER_TWO : Player.PLAYER_ONE;
    }

    public boolean isWhitePocketed() {
        return pocketedWhite;
    }

    public void cueStrike(Ball b) {
        if(struckAny)
            throw new RuntimeException("hit twice");

        struckAny = true;
        if(!b.isWhite()) {
            struckNonWhite = true;
        }
    }

    public boolean canStrike() {
        return !struckAny;
    }

    public void ballCollision(Ball a, Ball b) {
        if(a == b) {
            throw new RuntimeException();
        }

        if(a.isWhite() || b.isWhite()) {
            struckWhiteButMissed = false;
        }
    }

    public void ballPocketed(Ball b) {
        if(b.isWhite()) {
            pocketedWhite = true;
        }
        else {
            pocketed++;
        }
    }


    public boolean isFoul() {
        return struckWhiteButMissed || struckNonWhite || pocketedWhite;
    }

    public int getAwardedScore() {
        return isFoul() ? -1 : pocketed;
    }

    public String getMessage() {

        int score = getAwardedScore();

        switch (Integer.signum(score)) {
            case -1:
                return player + " fouled";
            case 0:
                return player + " played a neutral turn";
            case 1:
                return player + " scored " + score + " point(s)";
        }

        return "";
    }

    public String getFoulInformation() {
        if(struckNonWhite) {
            return player + " struck an object (non-white) ball";
        }
        if(pocketedWhite) {
            return player + " pocketed the white ball";
        }
        if(struckWhiteButMissed) {
            return player + " missed with the white ball";
        }
        return "";
    }
}
