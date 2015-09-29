# serverless-rps-clj

A Clojure library designed to provide Lambda implementations for Rock-Paper-Scissors.

## Usage

### Create lambda functions

    ./create.sh
    
### Update code for lambda functions

    ./build.sh
    
### Delete lambda functions

    ./cleanup.sh
    
### Invoking functions

#### Create Game

    % aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-create-game \
        --payload '{"email":"johndoe@example.com"}' \
        /tmp/lambda_result && jq . /tmp/lambda_result
    {
        "StatusCode": 200
    }
    {
      "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c"
    }

#### Join Game

    $ aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-join-game \
        --payload '{"gameId":"e593db97-9ede-4f40-bb4e-dc81c8a5ab1c", "email": "scooby@example.com"}' \
        /tmp/lambda_result && jq . /tmp/lambda_result
    {
        "StatusCode": 200
    }
    {
      "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c",
      "player1": "johndoe@example.com",
      "state": "ready",
      "player2": "scooby@example.com"
    }

#### Make Move

##### First Move

    $ aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-make-move \
        --payload '{"gameId":"e593db97-9ede-4f40-bb4e-dc81c8a5ab1c", \
                    "email": "johndoe@example.com", "move":"scissors"}' \
        /tmp/lambda_result && jq . /tmp/lambda_result 
    {
        "StatusCode": 200
    }
    {
      "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c",
      "player1": "johndoe@example.com",
      "state": "waiting",
      "player2": "scooby@example.com",
      "player1Move": "scissors"
    }

##### Second Move

    $ aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-make-move \
        --payload '{"gameId":"e593db97-9ede-4f40-bb4e-dc81c8a5ab1c", \
                    "email": "scooby@example.com", "move":"paper"}' \
        /tmp/lambda_result && jq . /tmp/lambda_result 
    {
        "StatusCode": 200
    }
    {
      "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c",
      "player1": "johndoe@example.com",
      "state": "ended",
      "player2": "scooby@example.com",
      "player1Move": "scissors",
      "player2Move": "paper",
      "winner": "johndoe@example.com"
    }

#### Get Game

    $ aws --profile=jayway-devops-ulrik lambda invoke         --function-name ulsa-get-game         --payload '{"gameId":"e593db97-9ede-4f40-bb4e-dc81c8a5ab1c"}'         /tmp/lambda_result && jq . /tmp/lambda_result 
    {
        "StatusCode": 200
    }
    {
      "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c",
      "player1": "johndoe@example.com",
      "state": "ended",
      "player2": "scooby@example.com",
      "player1Move": "scissors",
      "player2Move": "paper",
      "winner": "johndoe@example.com"
    }

#### Get Games

    $ aws --profile=jayway-devops-ulrik lambda invoke \
        --function-name ulsa-get-games \
        --payload '{"state":"ended"}' \
        /tmp/lambda_result && jq . /tmp/lambda_result 
    {
        "StatusCode": 200
    }
    [
      {
        "gameId": "e593db97-9ede-4f40-bb4e-dc81c8a5ab1c",
        "player1": "johndoe@example.com",
        "player2": "scooby@example.com",
        "player1Move": "scissors",
        "player2Move": "paper",
        "state": "ended",
        "winner": "johndoe@example.com"
      },
      {
        "gameId": "fe51e42a-cc01-4352-bc0d-947d770ed971",
        "player1": "johndoe@example.com",
        "player2": "scooby@example.com",
        "player1Move": "rock",
        "player2Move": "paper",
        "state": "ended",
        "winner": "scooby@example.com"
      },
      {
        "gameId": "83b36b25-4b14-4e93-9dbf-088b8a63b163",
        "player1": "johndoe@example.com",
        "player2": "janedoe@example.com",
        "player1Move": "rock",
        "player2Move": "rock",
        "state": "ended",
        "winner": "the game was a tie"
      }
    ]

## License

Copyright © 2015 Ulrik Sandberg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
