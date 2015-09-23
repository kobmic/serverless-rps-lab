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

        // your code goes here

    }
}
