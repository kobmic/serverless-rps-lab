# Serverless rock-paper-scissors lab using AWS Lambda & API Gateway


## Prereqs
* AWS account
* Java 8
* gradle

Use repository

	git clone git@github.com:kobmic/serverless-rps-lab.git
	cd serverless-rps-lab
	


## 1. Hello AWS Lambda
You can skip this step if you're already familiar with AWS lamba. In this step you will use a simple Java handler to create a Lambda function.


	cd 01-rps-lambda

	
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
Implement a simple function handler in your *RpsLambda* class. Lambda supports two approaches for creating a handler: 

* Loading handler method directly without having to implement an interface
* Implementing standard interfaces **RequestStreamHandler** or **RequestHandler**

To learn more about function handlers see [docs.](http://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-handler-types.html)

	public String helloWorldHandler(String input, Context context) {
    	LambdaLogger logger = context.getLogger();
	    logger.log("received : " + input);
        return String.format("Hello %s.", input);
	}

### Invocation types
* RequestResponse invocation type: synchronous, used i.e when testing from AWS Lambda console
* Event invocation type:  asynchronous, used with event sources such as Amazon S3, Amazon Kinesis, and Amazon SNS

### Create a deployment package	
Now you package and upload your code to create your Lambda function. You will specify the **com.jayway.lab.RpsLambda::helloWorldHandler** method reference as the handler.Your deployment package can be a **.zip** file or a standalone **.jar**. The gradle project contains a task for creating a zip:

	gradle buildZip

### Create Lambda Function
* Login to the AWS Lambda console.	
* Choose **Create a Lambda function**
* In step 1 **Select blueprint**, choose the **hello-world** blueprint.
* In step 2 **Configure function** specify *Java* runtime and upload your zip, select or create execution role (see below)
* In step 3 **Create function**

### Execution and invocation permissions
You must grant permissions for your Lambda function to access AWS resources like S3, DynamoDB or others. These are granted via an IAM role, called **execution role**. The entity invoking your Lambda function must have permission to do so. I.e. S3 or API Gateway needs permission to invoke your lambda function. See [docs](https://docs.aws.amazon.com/lambda/latest/dg/intro-permission-model.html#lambda-intro-execution-role) 

### Test
* Configure a sample event in the console and test your lambda function.

### Create a new lambda function that consumes JSON
Write a new lambda function **createGame** that consumes and produces JSON. Upload and test.

example JSON in:
	
	{"name": "Player1", email: "player1@gmail.com" }

example JSON out:	

	{"gameid": "unique-gameid-could-be-uuid"}

	

## 2. Hello API Gateway
You can skip this step if you're already familiar with AWS API Gateway. In this step, you will create a custom API and connect it to a Lambda function, and then call the Lambda function from your API.

### Basic Concepts
* REST API defined as set of **resources** and **methods** 
* HTTP endpoints for Lambda functions and other AWS Services

To learn more about API Gateway see [docs](http://docs.aws.amazon.com/apigateway/latest/developerguide/welcome.html)

### Create API
* in AWS API Gateway console **Create API**
* add resource **games** to your API
* create method **POST** for this resource, choose integration type **Lambda Function** and select region and function **createGame**

### Test
* in AWS API Gateway console click **Test** and enter example request body
* if everything works deploy your API 
* the resulting URL will look like

	https://<some-id>.execute-api.eu-west-1.amazonaws.com/<your-stage-name>
	
Test your endpoint with curl, i.e.

	curl -X POST -H "ContentType: application/json"
	 -d '{"name":"player1","email": "player1@gmail.com"}' 
	 https://0fjidtcksb.execute-api.eu-west-1.amazonaws.com/rpsDevStage/games

	





	




 

