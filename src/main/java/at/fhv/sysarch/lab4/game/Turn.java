package at.fhv.sysarch.lab4.game;

public class Turn {

    private boolean missed = true;
    private boolean struckNonWhite = false;
    private boolean pocketedWhite = false;
    private int pocketed = 0;

    public boolean isWhitePocketed() {
        return pocketedWhite;
    }

    public void cueStrike(Ball b) {
        if(!b.isWhite()) {
            struckNonWhite = true;
        }
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

}
