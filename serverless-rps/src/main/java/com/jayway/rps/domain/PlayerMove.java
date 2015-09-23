package com.jayway.rps.domain;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class PlayerMove {
    private String gameId;
    private String email;
    private Move move;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return "PlayerMove{" +
                "gameId='" + gameId + '\'' +
                ", email='" + email + '\'' +
                ", move=" + move +
                '}';
    }
}
