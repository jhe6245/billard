package at.fhv.sysarch.lab4.game;

public enum Player {
    PLAYER_ONE("Player One"),
    PLAYER_TWO("Player Two");

    private final String userString;
    
    Player(String userString) {
        this.userString = userString;
    }

    @Override
    public String toString() {
        return userString;
    }
}
