package com.jayway.lab;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class HelloLambda {

    // your code goes here
    public String myHandler(String input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + input);
        return String.format("Hello %s.", input);
    }
}

