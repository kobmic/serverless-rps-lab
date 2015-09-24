package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.jayway.rps.domain.Game;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class GetGamesLambda {

    /**
     * Lambda handler to query existing games.
     * @param request
     * @param context
     * @return
     */
    public Game getGames(GameQuery request , Context context) {

        // your code goes here

    }
}
