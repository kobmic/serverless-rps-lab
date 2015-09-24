package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.jayway.rps.domain.Game;

import java.util.List;

/**
 * Created by michaelkober on 2015-09-24.
 */
public class GetGamesLambda {

    /**
     * Lambda handler for query games by state.
     * @param request
     * @param context
     * @return
     */
    public List<Game> getGames(GameQuery request , Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + request.getState());
        List<Game>games = GameStore.getGames(request.getState());
        return games;
    }

    public static class GameQuery {
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
