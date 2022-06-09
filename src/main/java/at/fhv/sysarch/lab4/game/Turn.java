package at.fhv.sysarch.lab4.game;

import org.dyn4j.geometry.Vector2;

public class Turn {

    private final Player player;
    private final Vector2 whiteBallInitialPosition;
    private boolean missed = true;
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

    public Player getNextPlayer() {
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
            missed = false;
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
        return missed || struckNonWhite || pocketedWhite;
    }

    public int getScore() {
        return isFoul() ? -1 : pocketed;
    }

    public String getMessage() {
        if(isFoul()) {
            return "fouled";
        }
        if(pocketed == 0) {
            return "neutral turn";
        }
        return "scored " + pocketed;
    }

    public String getFoulInformation() {
        if(struckNonWhite) {
            return "struck an object ball";
        }
        if(pocketedWhite) {
            return "pocketed the white ball";
        }
        if(missed) {
            return "white ball hit nothing";
        }
        return null;
    }

    public Vector2 getWhiteBallInitialPosition() {
        return whiteBallInitialPosition;
    }

}
