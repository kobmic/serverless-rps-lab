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
     * Lambda handler for creating a game.
     * @param request
     * @param context
     * @return
     */
    public Game createGame(NewGameRequest request, Context context) {

        // your code goes here

        // In this example a pojo NewGameRequest is used as input object,
        // you can use your own pojos if you like


        // you can make use of the LambdaLogger if you like logs, i.e.
        //
        //LambdaLogger logger = context.getLogger();
        //logger.log("received : " + request);


    }


}
