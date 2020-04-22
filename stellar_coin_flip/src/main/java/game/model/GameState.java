package game.model;

public enum GameState {
    AWAITING_BANKER ("Awaiting Banker"),
    AWAITING_USERS ("Awaiting Users"),
    AWAIT_HASH_AND_BET ("Awaiting Hashes and Bets"),
    AWAIT_REAL_VALUE ("Awaiting Real Value"),
    AWAITING_WINNER ("Awaiting Winner");

    private final String name;

    private GameState(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}