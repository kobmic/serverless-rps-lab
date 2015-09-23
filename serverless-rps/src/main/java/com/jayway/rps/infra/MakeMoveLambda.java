package com.jayway.rps.infra;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.jayway.rps.domain.Game;
import com.jayway.rps.domain.PlayerMove;

/**
 * Created by michaelkober on 2015-09-23.
 */
public class MakeMoveLambda {
    /**
     * Lambda handler for making a move.
     * @param moveRequest
     * @param context
     * @return
     */
    public Game makeMove(PlayerMove moveRequest , Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + moveRequest);
        Game updatedGame = GameStore.makeMove(moveRequest);
        logger.log("updated : " + updatedGame);
        return updatedGame;
    }



}
