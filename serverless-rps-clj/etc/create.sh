#!/usr/bin/env bash

set -exu

memory=512
timeout=20
prefix="ulsa"
. functions.sh

function camelcase () {
    echo $1|perl -pe 's/[^-]+/\u$&/g;' -pe 's/-//g;'
}

for f in $functions; do
    function_name="${prefix}-$f"
    aws --profile=jayway-devops-ulrik lambda create-function \
        --function-name $function_name \
        --handler $(camelcase $f) \
        --runtime java8 --memory $memory --timeout $timeout \
        --role arn:aws:iam::554360467205:role/lambda_basic_execution \
        --zip-file fileb://./target/serverless-rps-clj-0.1.0-SNAPSHOT-standalone.jar
done