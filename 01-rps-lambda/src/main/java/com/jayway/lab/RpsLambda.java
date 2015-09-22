package com.jayway.lab;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RpsLambda {

    // your code goes here
    public Map<String, String> helloWorldHandler(Map<String,String> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + input);
        Map<String, String> message = new HashMap<String, String>();
        message.put("greet", String.format("hello %s", input.get("name")));
        return message;
    }

    public Map<String, String> createGame(Map<String,String> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + input);
        UUID gameId = UUID.randomUUID();
        Map<String, String> message = new HashMap<String, String>();
        message.put("gameid", gameId.toString());
        return message;
    }

}

