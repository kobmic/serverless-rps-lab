#!/usr/bin/env bash

set -eu

STAGE="ulsa_rps"
URL="https://5ikia5f4v9.execute-api.eu-west-1.amazonaws.com/$STAGE/games"
HEADERS="-H 'Content-Type: application/json'"
PROFILE="jayway-devops-ulrik"
PREFIX="ulsa"
PLAYER1="daphne@example.com"
PLAYER2="scooby@example.com"
ROOT="$(cd -P -- $(dirname -- "$0") && pwd -P)"
DIR=$(mktemp -t serverless-rps-clj -d)
what_to_test=api    # {api|lambda}

if [ $# -gt 0 ]; then
    what_to_test="$1"
fi

if [ "$what_to_test" = "lambda" ]; then
    echo "Testing the AWS Lambda functions"
elif [ "$what_to_test" = "api" ]; then
    echo "Testing the Amazon API Gateway API"
else
    echo "Error: expected {api|lambda}, got $what_to_test"
    exit 1
fi

echo "Result files will be located in $DIR"

# create game

echo; echo "Testing create game..."

file=$DIR/create-game

if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-create-game \
        --payload "{\"email\":\"$PLAYER1\"}" \
        $file > $out
    
    # validate status code
    statusCode=$(jq -r .StatusCode $out)
    if [ "$statusCode" != "200" ]; then
        echo "Failure: status code 200 expected, but was $statusCode"
        exit 1
    fi
else
    statusCode=$(curl -XPOST "$HEADERS" -d "{\"email\":\"$PLAYER1\"}" -o "$file" -w "%{http_code}" -s "$URL")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XPOST "$HEADERS" -d "{\"email\":\"$PLAYER1\"}" -o "$file" -w "%{http_code}" -s "$URL")
    fi

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

echo "Player1 ($PLAYER1) created game $gameId"

# join game

echo; echo "Testing join game..."

file=$DIR/join-game
if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-join-game \
        --payload "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER2\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    statusCode=$(curl -XPUT "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER2\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XPUT "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER2\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    fi
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

echo "Player2 ($PLAYER2) joined game $gameId"

# first move

echo; echo "Testing make move..."

moves=("rock" "paper" "scissors")
player1_move=${moves[$RANDOM % ${#moves[@]}]}

file=$DIR/first-move
if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-make-move \
        --payload "{\"gameId\":\"$gameId\", \
                    \"email\": \"$PLAYER1\", \"move\":\"$player1_move\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    statusCode=$(curl -XPOST "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER1\", \"move\":\"$player1_move\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XPOST "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER1\", \"move\":\"$player1_move\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    fi
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
echo "Player1 ($PLAYER1) played: $player1Move"

# second move

file=$DIR/second-move
player2_move=${moves[$RANDOM % ${#moves[@]}]}
if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-make-move \
        --payload "{\"gameId\":\"$gameId\", \
                    \"email\": \"$PLAYER2\", \"move\":\"$player2_move\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    statusCode=$(curl -XPOST "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER2\", \"move\":\"$player2_move\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XPOST "$HEADERS" -d "{\"gameId\":\"$gameId\", \"email\": \"$PLAYER2\", \"move\":\"$player2_move\"}" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    fi
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
echo "Player2 ($PLAYER2) played: $player2Move"
winner=$(jq -r .winner $file)
if [ "$winner" = "tie" ]; then
    echo "The game was a tie"
else
    echo "The winner is: $winner"
fi

# get game

echo; echo "Testing get game..."

file=$DIR/get-game
if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-get-game \
        --payload "{\"gameId\":\"$gameId\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    statusCode=$(curl -XGET "$HEADERS" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XGET "$HEADERS" -o "$file" -w "%{http_code}" -s "$URL/$gameId")
    fi
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
if [ "$player1" != "$PLAYER1" ]; then
    echo "Failure: player1 expected to be '$PLAYER1', but was '$player1'"
    exit 1
fi
player2=$(jq -r .player2 $file)
if [ "$player2" != "$PLAYER2" ]; then
    echo "Failure: player2 expected to be '$PLAYER2', but was '$player2'"
    exit 1
fi

echo "The game $gameId looks fine"

# get games

state="ended"
echo; echo "Testing get games (with state=$state)..."

file=$DIR/get-games
if [ "$what_to_test" = "lambda" ]; then
    out=$file.out
    aws --profile=$PROFILE lambda invoke \
        --function-name ${PREFIX}-get-games \
        --payload "{\"state\":\"$state\"}" \
        $file > $out
    statusCode=$(jq -r .StatusCode $out)
else
    statusCode=$(curl -XGET "$HEADERS" -o "$file" -w "%{http_code}" -s "$URL?state=$state")
    if [ "$statusCode" = "504" ]; then
        echo "Warning: status code was 504 (Gateway Timeout), retrying once..."
        statusCode=$(curl -XGET "$HEADERS" -o "$file" -w "%{http_code}" -s "$URL?state=$state")
    fi
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
