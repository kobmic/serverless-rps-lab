#!/usr/bin/env bash

set -exu

MEMORY=512
TIMEOUT=20
PREFIX="ulsa"
ROOT="$(cd -P -- $(dirname -- "$0") && pwd -P)"

. $ROOT/functions.sh

function camelcase () {
    echo $1|perl -pe 's/[^-]+/\u$&/g;' -pe 's/-//g;'
}

time for f in $functions; do
    function_name="${PREFIX}-$f"
    aws --profile=jayway-devops-ulrik lambda create-function \
        --function-name $function_name \
        --handler $(camelcase $f) \
        --runtime java8 --memory $MEMORY --timeout $TIMEOUT \
        --role arn:aws:iam::554360467205:role/lambda_basic_execution \
        --zip-file fileb://./target/serverless-rps-clj-0.1.0-SNAPSHOT-standalone.jar
done