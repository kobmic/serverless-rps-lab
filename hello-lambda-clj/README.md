# hello-lambda-clj

A Clojure library designed to say hello, and some other test stuff.

## Usage

### hello lambda

    % lein uberjar
    % aws --profile=jayway-devops-ulrik lambda create-function \
        --function-name ulsa-hello-lambda-clj \
        --handler hello_lambda_clj.core::handler \
        --runtime java8 --memory 512 --timeout 10 \
        --role arn:aws:iam::554360467205:role/lambda_exec_role \
        --zip-file fileb://./target/hello-lambda-clj-0.1.0-SNAPSHOT-standalone.jar
    % aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-hello-lambda-clj \
        --payload '"John Doe"' \
        /tmp/lambda_result
    {
        "StatusCode": 200
    }
    % cat /tmp/lambda_result
    "Hello John Doe!"

### hello pojo

    % aws --profile=jayway-devops-ulrik lambda create-function \
        --function-name ulsa-hello-lambda-clj-pojo \
        --handler PojoHandler::handlepojo \
        --runtime java8 --memory 512 --timeout 10 \
        --role arn:aws:iam::554360467205:role/lambda_exec_role \
        --zip-file fileb://./target/hello-lambda-clj-0.1.0-SNAPSHOT-standalone.jar
        % aws --profile=jayway-devops-ulrik lambda invoke \
            --function-name ulsa-hello-lambda-clj-pojo \
            --payload '{"name":"John Doe","email":"johndoe@example.com"}' \
            /tmp/lambda_result
        {
          "StatusCode": 200
        }
        % cat /tmp/lambda_result
        "{\"gameid\":\"93c7a847-dc78-4005-b9a4-3563aeca0424\",\"name\":\"John Doe\",\"email\":\"johndoe@example.com\"}"

## License

Copyright © 2015 Ulrik Sandberg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
