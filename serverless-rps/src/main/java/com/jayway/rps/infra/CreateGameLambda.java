package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.jayway.rps.domain.Game;

import java.util.UUID;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class CreateGameLambda {

    /**
     * Lambda handler for creating a game, using pojos.
     * @param request
     * @param context
     * @return
     */
    public Game createGame(NewGameRequest request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + request);
        UUID gameId = UUID.randomUUID();
        Game game = GameStore.createGame(gameId.toString(), request.getEmail());
        logger.log("created : " + game);
        return game;
    }

    public static class NewGameRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return String.format("NewGameRequest{ email: '%s' }", email);
        }
    }
}
