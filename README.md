# Serverless rock-paper-scissors lab using AWS Lambda & API Gateway


## Prereqs
* AWS account
* Java 8
* gradle

Use repository

	git clone git@github.com:kobmic/serverless-rps-lab.git
	cd serverless-rps-lab
	
## 0. Naming
When doing this lab in teams, to avoid name conflicts choose a team name and use it when naming lambda functions etc, i.e. "awesomeTeam-helloWorld" instead of "helloWorld" 

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
Now you package and upload your code to create your Lambda function. You will specify the **com.jayway.lab.HelloLambda::helloWorld** method reference as the handler.Your deployment package can be a **.zip** file or a standalone **.jar**. The gradle project contains a task for creating a zip:

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
* Configure a sample event in the console and test your lambda function.

## 2. Create a lambda function that consumes JSON
Write a new lambda function **createGame** that consumes and produces JSON. Upload and test.

example JSON in:
	
	{"name": "Player1", email: "player1@gmail.com" }

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

In this step you will implement the rock-paper-scissors game using API Gateway & Lambda functions. You will use Amazon Dynamo DB to store your data in table **rpslab-games** in region **eu-west1**. Make sure to add permissions to the execution role you used for your lambda function, i.e.

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
  		"gameId": "a7f7615c-c385-457c-93a5-1267dfe8787e"
	}

* reuse your API Gateway from above, or create a new one, as before you'll need a **games** resource with method **POST** that will use your lambda function **createGame**
* test your API and make sure the new game is persisted in DynamoDB table **rpslab-games**

### Join Game

* Implement **JoinGameLambda** 
* add resource **games/{gameId}** with method **PUT**



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



### Get Game

* Implement **GetGameLambda** 
* add method **GET** to resource **games/{gameId}** 


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



### Make Move

* Implement **MakeMoveLambda** 
* add method **POST** to resource **games/{gameId}** 


Json in:

	{
  		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"email": "player1@gmail.com"
	}
	
Json out:

	{
		"gameId": "c89dc950-141e-46ea-9f99-b1f54fb9c46d",
  		"state": "waiting",
  		"player1": "player1@gmail.com",
		"player2": "player2@gmail.com",
		"player1Move": "rock",
	}	







	




 

