package com.jayway.rps.domain;

/**
 * Game states: created, ready, playing, ended
 */
public class Game {
    private String gameId;
    private String state;
    private String player1;
    private String player2;
    private String winner;
    private String player1Move;
    private String player2Move;
    //private List<PlayerMove> moves;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getPlayer1Move() {
        return player1Move;
    }

    public void setPlayer1Move(String player1Move) {
        this.player1Move = player1Move;
    }

    public String getPlayer2Move() {
        return player2Move;
    }

    public void setPlayer2Move(String player2Move) {
        this.player2Move = player2Move;
    }

    public void updateWinner() {
        if ((player1Move != null) && (this.player2Move != null)) {
            if (Move.valueOf(player1Move).defeats(Move.valueOf(player2Move))) {
                winner = player1;
            } else if (Move.valueOf(player2Move).defeats(Move.valueOf(player1Move))) {
                winner = player2;
            } else {
                winner = "tie";
            }
        }
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId='" + gameId + '\'' +
                ", state='" + state + '\'' +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", winner='" + winner + '\'' +
                ", player1Move='" + player1Move + '\'' +
                ", player2Move='" + player2Move + '\'' +
                '}';
    }
}
