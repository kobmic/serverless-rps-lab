#!/usr/bin/env bash

set -exu

prefix="ulsa"
. functions.sh

for f in $functions; do
    function_name="${prefix}-$f"
    aws --profile=jayway-devops-ulrik lambda delete-function \
        --function-name $function_name
done