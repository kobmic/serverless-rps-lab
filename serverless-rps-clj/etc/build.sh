#!/usr/bin/env bash

set -exu

prefix="ulsa"

. functions.sh

# build
lein uberjar

# update function code
for f in $functions; do
    aws --profile=jayway-devops-ulrik lambda update-function-code \
        --function-name ${prefix}-$f \
        --zip-file fileb://./target/serverless-rps-clj-0.1.0-SNAPSHOT-standalone.jar
done
