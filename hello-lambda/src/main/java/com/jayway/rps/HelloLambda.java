package com.jayway.rps;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class HelloLambda {

    public String helloWorld(String input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + input);
        return String.format("Hello %s.", input);
    }
}
