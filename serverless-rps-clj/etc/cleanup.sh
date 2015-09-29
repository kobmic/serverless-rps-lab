#!/usr/bin/env bash

set -exu

PREFIX="ulsa"
ROOT="$(cd -P -- $(dirname -- "$0") && pwd -P)"

. $ROOT/functions.sh

time for f in $functions; do
    function_name="${PREFIX}-$f"
    aws --profile=jayway-devops-ulrik lambda delete-function \
        --function-name $function_name
done