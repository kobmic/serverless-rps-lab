#!/usr/bin/env bash

set -exu

PREFIX="ulsa"
ROOT="$(cd -P -- $(dirname -- "$0") && pwd -P)"

. $ROOT/functions.sh

# build
lein uberjar

# update function code
time for f in $functions; do
    aws --profile=jayway-devops-ulrik lambda update-function-code \
        --function-name ${PREFIX}-$f \
        --zip-file fileb://./target/serverless-rps-clj-0.1.0-SNAPSHOT-standalone.jar
done
