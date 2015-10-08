# Serverless rock-paper-scissors lab using AWS Lambda & API Gateway


## Prereqs
* AWS account
* Java 8
* gradle

Use repository

	git clone git@github.com:kobmic/serverless-rps-lab.git
	cd serverless-rps-lab
	
If you get stuck you can check the branch **solution**.	
	
## 0. Naming
When doing this lab in teams, to avoid name conflicts choose a team name and use it when naming lambda functions etc, i.e. "awesomeTeam-helloWorld" instead of "helloWorld".
In orer to use the automatic test script described below also make sure that you follow a naming convention for all of your lambda methods. Use a prefix, such as "awesomeTeam-" and then name your methods such as "awesomeTeam-getGames", "awesomeTeam-makeMove" and so on.

## 1. Hello AWS Lambda
You can skip this step if you're already familiar with AWS lamba. In this step you will use a simple Java handler to create a Lambda function.


	cd hello-lambda

	
### AWS Lambda functions in Java	
* must be stateless to enable scaling
* expect local file system access, child processes, and similar artifacts to be limited to the lifetime of the request
* AWS Lambda provides 2 libraries
	* aws-lambda-java-core: provides the Context object, RequestStreamHandler, and the RequestHandler interfaces
	* aws-lambda-java-events:  predefined types that you can use when writing Lambda functions to process events published by i.e. Amazon S3, Amazon Kinesis, Amazon SNS
	
### Add dependencies
Add compile dependencies to your build.gradle:

	'com.amazonaws:aws-lambda-java-core:1.0.0',
    'com.amazonaws:aws-lambda-java-events:1.0.0' 
    
Build

	gradle build
	
### Implement a function handler
Implement a simple function handler in your *HelloLambda* class. Lambda supports two approaches for creating a handler: 

* Loading handler method directly without having to implement an interface
* Implementing standard interfaces **RequestStreamHandler** or **RequestHandler**

First parameter of the handler function is **input**, second parameter the **context**.
Supported input/output types for a handler are: simple Java types (String, Integer, Boolean, Map, and List types) or POJO or Stream type. AWS Lambda serializes based on standard bean naming conventions (use mutable POJOs with public getters and setters).

To learn more about function handlers see [docs.](http://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-handler-types.html)

	public String helloWorld(String input, Context context) {
    	LambdaLogger logger = context.getLogger();
	    logger.log("received : " + input);
        return String.format("Hello %s.", input);
	}

### Invocation types
* RequestResponse invocation type: synchronous, used i.e when testing from AWS Lambda console
* Event invocation type:  asynchronous, used with event sources such as Amazon S3, Amazon Kinesis, and Amazon SNS

### Create a deployment package	
Now you package and upload your code to create your Lambda function. You will specify the **com.jayway.rps.HelloLambda::helloWorld** method reference as the handler.Your deployment package can be a **.zip** file or a standalone **.jar**. The gradle project contains a task for creating a zip:

	gradle buildZip

### Create Lambda Function
* Login to the AWS Lambda console.	
* Choose **Create a Lambda function**
* In step 1 **Select blueprint**, choose the **hello-world** blueprint.
* In step 2 **Configure function** specify *Java* runtime, your handler, upload your zip, and select or create execution role (see below)
* In step 3 **Create function**

### Execution and invocation permissions
You must grant permissions for your Lambda function to access AWS resources like S3, DynamoDB or others. These are granted via an IAM role, called **execution role**. The entity invoking your Lambda function must have permission to do so. I.e. S3 or API Gateway needs permission to invoke your lambda function. See [docs](https://docs.aws.amazon.com/lambda/latest/dg/intro-permission-model.html#lambda-intro-execution-role) 

### Test
Configure a sample event in the console and test your lambda function.

### Troubleshooting and monitoring
AWS Lambda automatically monitors Lambda functions, reporting metrics through Amazon CloudWatch. You can insert logging statements into your code that will be pushed to a CloudWatch Logs group associated with your Lambda function (Lambda/<function name>). In Java use the **LambdaLogger**:

	LambdaLogger logger = context.getLogger();
    logger.log("some useful log");


## 2. Create a lambda function that consumes JSON
Write a new lambda function **createGame** that consumes and produces JSON. Upload and test.

example JSON in:
	
	{"name": "Player1", "email": "player1@gmail.com" }

example JSON out:	

	{"gameid": "unique-gameid-could-be-uuid"}

	

## 3. Hello API Gateway
You can skip this step if you're already familiar with AWS API Gateway. In this step, you will see how to use API Gateway to create a custom API, connect your custom API to a AWS Lambda function, and then call the Lambda function from a client through API Gateway.

### Basic Concepts
* REST API defined as set of **resources** and **methods** 
* HTTP(s) endpoints for Lambda functions and other AWS Services
* For every resource specify one or more methods to invoke it
* Integration types:
	* Lambda Function 	
	* HTTP Proxy
	* Mock Integration
	* AWS Sevice Proxy

To learn more about API Gateway see [docs](http://docs.aws.amazon.com/apigateway/latest/developerguide/welcome.html)

### Create API
* in AWS API Gateway console **Create API**
* add resource **games** to your API
* create method **POST** for this resource, choose integration type **Lambda Function** and select region and function **createGame**

### Models and Mapping 
In API Gateway, you use models and mapping templatesto transform data from one schema to another. API Gateway uses the Velocity Template Language (VTL) and JSONPath expressions to define mapping templates. To learn more about models and mapping see [docs](http://docs.aws.amazon.com/apigateway/latest/developerguide/models-mappings.html#models-mappings-mappings)


### Test
* in AWS API Gateway console click **Test** and enter example request body
* if everything works deploy your API 
* the resulting URL will look like

	https://0fjidtcksb.execute-api.eu-west-1.amazonaws.com/your-stage-name
	
Test your endpoint with curl, i.e.

	curl -X POST -H "ContentType: application/json"
	 -d '{"name":"player1","email": "player1@gmail.com"}' 
	 https://0fjidtcksb.execute-api.eu-west-1.amazonaws.com/rpsDevStage/games
	 
## 4. Serverless Rock-Paper-Scissors

In this step you will implement the rock-paper-scissors game using API Gateway & Lambda functions. You will use Amazon Dynamo DB to store your data in table **rpslab-games** in region **eu-west1**. Specify a hash key on **gameId**. Make sure to add permissions to the execution role you used for your lambda function, i.e.

	{
    	"Effect": "Allow",
        "Action": "dynamodb:*",
        "Resource": "arn:aws:dynamodb:eu-west-1:554360467205:table/rpslab-games"
    } 

### Architecture
![Architecture](./architecture.png)

### Game states
![game states](./game-states.png)

### Create Game

* Write a lambda function that creates a new game, use **com.jayway.rps.infra.GameStore** utility class to store the new game in DynamoDB. You'll find a project template in directory **serverless-rps**.

Json in:

	{
  		"email": "player1@gmail.com"
	}

Json out:

	{
  		"gameId": "a7f7615c-c385-457c-93a5-1267dfe8787e",
  		"state": "created",
  		"player1": "mike@gmail.com"
	}

* reuse your API Gateway from above, or create a new one, as before you'll need a **games** resource with method **POST** that will use your lambda function **createGame**
* test your API and make sure the new game is persisted in DynamoDB table **rpslab-games**
* change message repsonse from 200 to 201

### Get Game

* Implement **GetGameLambda** 

Json in:

	{
  		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
	}
	
Json out:

	{
		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"state": "ended",
  		"player1": "player1@gmail.com",
		"player2": "player2@gmail.com",
		"player1Move": "rock",
		"player2Move": "rock",
		"winner": "tie""
	}	

* add a new resource **gameid** under the **games** resource. Path should be **{gameid}**	
* add method **GET** to resource **games/{gameid}** 
* add a mapping to map the path parameter {gameid} to Json
	* in resource view select your new **GET** method
	* choose integration request
	* add mapping template 'application/json' 
	* add template (change from "Input passthrough"): <code>{ "gameId" : "$input.params('gameid')" } </code>
	

### Join Game

* Implement **JoinGameLambda** 

Json in:

	{
  		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
		"email": "player2@gmail.com"
	}
	
Json out:

	{
		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"state": "ready",
  		"player1": "player1@gmail.com",
		"player2": "player2@gmail.com"
	}	


* add resource **games/{gameid}** with method **PUT**

Mapping for {gameid} and add back remaining JSON properties in request body:
	
	#set($inputRoot = $input.path('$'))
	{ 
  		"gameId" : "$input.params('gameid')",
  		"email": "$inputRoot.email"
	}



### Make Move

* Implement **MakeMoveLambda** 

Json in:

	{
  		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"email": "player1@gmail.com",
  		"move": "rock"
	}
	
Json out:

	{
		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"state": "waiting",
  		"player1": "player1@gmail.com",
		"player2": "player2@gmail.com",
		"player1Move": "rock",
	}	

* add method **POST** to resource **games/{gameid}** 

Mapping for {gameid} and add back remaining JSON properties in request body:
	
	#set($inputRoot = $input.path('$'))
	{ 
  		"gameId" : "$input.params('gameid')",
  		"email": "$inputRoot.email",
  		"move": "$inputRoot.move"
	}



### Get Games

* Implement **GetGamesLambda**

Json in:

	{
  		"state": "created"
	}
	
Json out:

	[
  	  {
    	"gameId": "a6e1efd2-952d-4ea2-982a-663a12ac3a24",
	    "state": "created",
    	"player1": "mike@gmail.com"
	  },
  	  {
    	"gameId": "b5c9be16-5633-40ae-950c-a8b33a171d48",
    	"state": "created",
    	"player1": "mike@gmail.com"
  	  }
    ]	

* add method **GET** to resource **games**
* use a query parameter *state* to query games by state, i.e. <code>/games?state=ended</code>
* add the query parameter in "Method request" step

Mapping for query parameter *state*:
	
	{ 
  		"state" : "$input.params('state')"
  	}

### Test
Test your API with curl, or use Ulriks script:

#### Usage

	$ cd serverless-rps
	$ etc/test-api.sh --help
	Usage: test-api.sh --profile=<profile> --prefix=<prefix>
            --api-id=<api id> --stage=<stage>
            [--test={api|lambda}] [--region=<region>]
            [--player1=<email>] [--player2=<email>]
            [--camelCase] [--debug] [--help]
    
    where:
      profile     aws-cli profile, eg 'jayway-devops-mike'
      prefix      lambda function prefix, eg 'mike' if lambda is 'mike-get-game'
      api-id      id of the Amazon API Gateway API, eg '5ikia5f4v9'
      stage       name of API Gateway stage, eg 'mike_rps'
      test        what to test, {api|lambda} (default: api)
      region      name of AWS region (default: eu-west-1)
      player1     email of player1 (default: daphne@example.com)
      player2     email of player2 (default: scooby@example.com)
      camel-case  expect 'mike-createGame' lambda names (default: 'mike-create-game')
    
    If your Lambda functions are not dash-separated lower-case prefixed names,
    like 'mike-create-game', 'mike-join-game', 'mike-make-move', 'mike-get-game',
    and 'mike-get-games', but rather 'mike-createGame', 'mike-joinGame',
    'mike-makeMove', 'mike-getGame', and 'mike-getGames', then you must use the
    flag '--camel-case'. If they have a naming-convention different than those two,
    you're on your own.

**Lambda**

Use the parameter `--test=lambda` to test all the Lambda functions:

	$ etc/test-api.sh --test=lambda --profile=jayway-devops-ulrik --prefix=ulsa --api-id=5ikia5f4v9 --stage=ulsa_rps
	Testing the AWS Lambda functions
	...

**API Gateway**
	
Use the parameter `--test=api` to test all the API resources and methods:

	$ etc/test-api.sh --test=api --profile=jayway-devops-ulrik --prefix=ulsa --api-id=5ikia5f4v9 --stage=ulsa_rps

	Testing the Amazon API Gateway API on https://5ikia5f4v9.execute-api.eu-west-1.amazonaws.com/ulsa_rps/games
	Result files will be located in /var/folders/gc/0skht0rj5nv53jzc5srk0ng80000gn/T/serverless-rps.Lw1V9oQ8

	Testing create game...
	Player1 (daphne@example.com) created game 5bccdbbb-b5a9-4244-9d6b-7f24d424e9ed

	Testing join game...
	Player2 (scooby@example.com) joined game 5bccdbbb-b5a9-4244-9d6b-7f24d424e9ed

	Testing make move...
	Player1 (daphne@example.com) played: paper
	Player2 (scooby@example.com) played: scissors
	The winner is: scooby@example.com

	Testing get game...
	The game 5bccdbbb-b5a9-4244-9d6b-7f24d424e9ed looks fine

	Testing get games (with state=ended)...
	Warning: status code was 504 (Gateway Timeout), retrying...
	There are 6 games in state ended

	Tests are done

The first API Gateway calls on cold instances often result in a timeout. As you
can see above, the test script handles retries in those cases (max 5).

The created game is not cleaned up, so you might want to:

* set `--player1` and/or `--player2` to something you recognize
* manually delete the games created by your tests from the DynamoDB console

## Where to go from here
If you want to code some more, here are some ideas.

### Error Handling
The current code template didn't include any error handling. Improve the code and map errors to HTTP status code in the API Gateway.

### Implement highscore
Implement a highscore feature for the rps game. Checkout the 'dynamo-process-stream' blueprint in AWS Lmabda. It's an DynamoDB trigger that logs updates to a table. Implement a lambda function that gets the updates, and when receiving an update for a game that's ended, update a **highscore** collection, i.e, "player, wins, losts, ties".

### Upload player image
Upload player image to s3, write a lambda function that listens to **s3:ObjectCreated:*** events, scale the upload image and displays it in the highscore list. See [AWS Lambda Walkthrough 1: Process S3 Events (Java)](http://docs.aws.amazon.com/lambda/latest/dg/java-wt-s3-log-event-data.html)

### Use API Gateway as Service Proxy to DynamoDB 
Instead of using AWS Lambda, try to use API Gateway as service proxy to DynamoDB. Check [Making HTTP Requests to DynamoDB](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/MakingHTTPRequests.html) and [Walkthrough: API Gateway and an AWS Service Proxy](http://docs.aws.amazon.com/apigateway/latest/developerguide/getting-started-aws-proxy.html)
 

