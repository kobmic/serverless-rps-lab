package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.jayway.rps.domain.Game;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class JoinGameLambda {

    /**
     * Lambda handler for join existing game.
     * @param request
     * @param context
     * @return
     */
    public Game joinGame(JoinGameRequest request , Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + request);
        Game updatedGame = GameStore.joinGame(request.getGameId(), request.getEmail());
        logger.log("updated : " + updatedGame);
        return updatedGame;
    }

    public static class JoinGameRequest {
        private String gameId;
        private String email;

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
        @Override
        public String toString() {
            return String.format("JoinGameRequest{ gameId: '%s' email: '%s' }", gameId, email);
        }
    }
}
