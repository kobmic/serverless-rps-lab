#!/usr/bin/env bash

set -eu

ROOT="$(cd -P -- $(dirname -- "$0") && pwd -P)"

prg=$(basename $0)
usage="Usage: $prg --profile=<profile> --prefix=<prefix>
        --api-id=<api id> --stage=<stage>
        [--test={api|lambda}] [--region=<region>]
        [--player1=<email>] [--player2=<email>]
        [--debug] [--help]

where:
  profile     aws-cli profile, eg 'jayway-devops-mike'
  prefix      lambda function prefix, eg 'mike' if lambda is 'mike-get-game'
  api-id      id of the Amazon API Gateway API, eg '5ikia5f4v9'
  stage       name of API Gateway stage, eg 'mike_rps'
  test        what to test, {api|lambda} (default: api)
  region      name of AWS region (default: eu-west-1)
  player1     email of player1 (default: daphne@example.com)
  player2     email of player2 (default: scooby@example.com)

Lambda functions are expected to have dash-separated lower-case prefixed names,
like 'mike-create-game', 'mike-join-game', 'mike-make-move', 'mike-get-game',
and 'mike-get-games'."

profile=
prefix=
api_id=
stage=
test="api"
region="eu-west-1"
player1="daphne@example.com"
player2="scooby@example.com"

headers="-H 'Content-Type: application/json'"

while getopts ':-' 'OPTION' ; do
  case $OPTION in
    - ) [ $OPTIND -ge 1 ] && optind=$(expr $OPTIND - 1 ) || optind=$OPTIND
        eval OPTION="\$$optind"
        OPTARG=$(echo $OPTION | cut -d'=' -f2)
        OPTION=$(echo $OPTION | cut -d'=' -f1)
        case $OPTION in
            --profile )    profile=$OPTARG;;
            --prefix )     prefix=$OPTARG;;
            --api-id )     api_id=$OPTARG;;
            --stage )      stage=$OPTARG;;
            --test )       test=$OPTARG;;
            --region )     region=$OPTARG;;
            --player1 )    player1=$OPTARG;;
            --player2 )    player2=$OPTARG;;
            --debug )      set -x;;
            --help )       echo "$usage" ; exit 0;;
            * ) echo "Error: Unsupported argument '$OPTION'.

$usage" 1>&2 ; exit 64;;
         esac
       OPTIND=1
       shift
      ;;
    ? ) echo "Error: Only long arguments, like --profile=jayway-devops-mike, are supported.

$usage" 1>&2 ; exit 64;;
  esac
done

# validate args

if [ -z "$profile" ]; then
    echo "Error: --profile is required.

$usage" ; exit 64
fi

if [ -z "$prefix" ]; then
    echo "Error: --prefix is required.

$usage" ; exit 64
fi

if [ -z "$api_id" ]; then
    echo "Error: --api-id is required.

$usage" ; exit 64
fi

if [ -z "$stage" ]; then
    echo "Error: --stage is required.

$usage" ; exit 64
fi

api_url="https://$api_id.execute-api.$region.amazonaws.com/$stage/games"

if [ "$test" = "lambda" ]; then
    echo "Testing the AWS Lambda functions"
elif [ "$test" = "api" ]; then
    echo "Testing the Amazon API Gateway API; URL is: $api_url"
else
    echo "Error: expected {api|lambda}, got $test

$usage" ; exit 64
fi

logdir=$(mktemp -t serverless-rps -d)
echo "Result files will be located in $logdir"

# create game

echo; echo "Testing create game..."

file=$logdir/create-game

if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-create-game \
        --payload "{\"email\":\"$player1\"}" \
        $file > $out
    
    # validate status code
    statusCode=$(jq -r .StatusCode $out)
    if [ "$statusCode" != "200" ]; then
        echo "Failure: status code 200 expected, but was $statusCode"
        exit 1
    fi
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XPOST "$headers" -d "{\"email\":\"$player1\"}" -o "$file" -w "%{http_code}" -s "$api_url")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done

    # validate status code
    if [ "$statusCode" != "201" ]; then
        echo "Failure: status code 201 expected, but was $statusCode"
        exit 1
    fi
fi

# validate result
gameId=$(jq -r .gameId $file)
if [ -z "$gameId" ]; then
    echo "Failure: gameId expected, but was empty"
    exit 1
fi

echo "Player1 ($player1) created game $gameId"

# join game

echo; echo "Testing join game..."

file=$logdir/join-game
if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-join-game \
        --payload "{\"gameId\":\"$gameId\", \"email\": \"$player2\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XPUT "$headers" -d "{\"gameId\":\"$gameId\", \"email\": \"$player2\"}" -o "$file" -w "%{http_code}" -s "$api_url/$gameId")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done
fi

# validate status code
if [ "$statusCode" != "200" ]; then
    echo "Failure: status code 200 expected, but was $statusCode"
    exit 1
fi

# validate result
state=$(jq -r .state $file)
if [ "$state" != "ready" ]; then
    echo "Failure: state expected to be 'ready', but was '$state'"
    exit 1
fi

echo "Player2 ($player2) joined game $gameId"

# first move

echo; echo "Testing make move..."

moves=("rock" "paper" "scissors")
player1_move=${moves[$RANDOM % ${#moves[@]}]}

file=$logdir/first-move
if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-make-move \
        --payload "{\"gameId\":\"$gameId\", \
                    \"email\": \"$player1\", \"move\":\"$player1_move\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XPOST "$headers" -d "{\"gameId\":\"$gameId\", \"email\": \"$player1\", \"move\":\"$player1_move\"}" -o "$file" -w "%{http_code}" -s "$api_url/$gameId")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done
fi

# validate status code
if [ "$statusCode" != "200" ]; then
    echo "Failure: status code 200 expected, but was $statusCode"
    exit 1
fi

# validate result
state=$(jq -r .state $file)
if [ "$state" != "waiting" ]; then
    echo "Failure: state expected to be 'waiting', but was '$state'"
    exit 1
fi

player1Move=$(jq -r .player1Move $file)
echo "Player1 ($player1) played: $player1Move"

# second move

file=$logdir/second-move
player2_move=${moves[$RANDOM % ${#moves[@]}]}
if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-make-move \
        --payload "{\"gameId\":\"$gameId\", \
                    \"email\": \"$player2\", \"move\":\"$player2_move\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XPOST "$headers" -d "{\"gameId\":\"$gameId\", \"email\": \"$player2\", \"move\":\"$player2_move\"}" -o "$file" -w "%{http_code}" -s "$api_url/$gameId")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done
fi

# validate status code
if [ "$statusCode" != "200" ]; then
    echo "Failure: status code 200 expected, but was $statusCode"
    exit 1
fi

# validate result
state=$(jq -r .state $file)
if [ "$state" != "ended" ]; then
    echo "Failure: state expected to be 'ended', but was '$state'"
    exit 1
fi

player2Move=$(jq -r .player2Move $file)
echo "Player2 ($player2) played: $player2Move"
winner=$(jq -r .winner $file)
if [ "$winner" = "tie" ]; then
    echo "The game was a tie"
else
    echo "The winner is: $winner"
fi

# get game

echo; echo "Testing get game..."

file=$logdir/get-game
if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-get-game \
        --payload "{\"gameId\":\"$gameId\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XGET "$headers" -o "$file" -w "%{http_code}" -s "$api_url/$gameId")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done
fi

# validate status code
if [ "$statusCode" != "200" ]; then
    echo "Failure: status code 200 expected, but was $statusCode"
    exit 1
fi

# validate result
newGameId=$(jq -r .gameId $file)
if [ "$newGameId" != "$gameId" ]; then
    echo "Failure: gameId expected to be '$gameId', but was '$newGameId'"
    exit 1
fi
state=$(jq -r .state $file)
if [ "$state" != "ended" ]; then
    echo "Failure: state expected to be 'ended', but was '$state'"
    exit 1
fi
player1=$(jq -r .player1 $file)
if [ "$player1" != "$player1" ]; then
    echo "Failure: player1 expected to be '$player1', but was '$player1'"
    exit 1
fi
player2=$(jq -r .player2 $file)
if [ "$player2" != "$player2" ]; then
    echo "Failure: player2 expected to be '$player2', but was '$player2'"
    exit 1
fi

echo "The game $gameId looks fine"

# get games

state="ended"
echo; echo "Testing get games (with state=$state)..."

file=$logdir/get-games
if [ "$test" = "lambda" ]; then
    out=$file.out
    aws --profile=$profile lambda invoke \
        --function-name ${prefix}-get-games \
        --payload "{\"state\":\"$state\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    n=0
    until [ $n -ge 5 ]
    do
        statusCode=$(curl -XGET "$headers" -o "$file" -w "%{http_code}" -s "$api_url?state=$state")
        if [ "$statusCode" = "504" ]; then
            echo "Warning: status code was 504 (Gateway Timeout), retrying..."
            n=$[$n+1]
            sleep 5
        else
            break
        fi
    done
fi

# validate status code
if [ "$statusCode" != "200" ]; then
    echo "Failure: status code 200 expected, but was $statusCode"
    exit 1
fi

# validate result
ngames=$(jq '.|length' $file)
echo "There are $ngames games in state $state"

echo; echo "Tests are done"
