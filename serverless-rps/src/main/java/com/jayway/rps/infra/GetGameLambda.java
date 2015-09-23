package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.jayway.rps.domain.Game;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class GetGameLambda {

    /**
     * Lambda handler for fetching a game, using pojos.
     * @param request
     * @param context
     * @return
     */
    public Game getGame(GameRequest request , Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + request.getGameId());
        Game game = GameStore.getGame(request.getGameId());
        logger.log("game : " + game);
        return game;
    }

    public static class GameRequest {
        private String gameId;

        public String getGameId() {
            return gameId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }
    }

}
