package com.serverless;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.*;
import java.io.*;
import org.json.*;
import org.json.simple.parser.*;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class Handler implements RequestHandler<Map<String,Object>, ApiGatewayResponse> {

	@Override
	public ApiGatewayResponse handleRequest(Map<String,Object> input, Context context) {
		int statusCode = 200;
		Response responseBody = new Response("Success");
		try {
			JSONObject event = new JSONObject((String)input.get("body")) ;
			AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.standard().build();
			String keyName = UUID.randomUUID().toString();
			String tableName = "dummy-details";
			
			Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
			attributes.put("uid",  new AttributeValue().withS(keyName));
			attributes.put("firstname",  new AttributeValue().withS(event.getString("firstname")));
			attributes.put("lastname",  new AttributeValue().withS(event.getString("lastname")));
			attributes.put("secret",  new AttributeValue().withS(event.getString("secret")));
			dynamoDb.putItem(new PutItemRequest()
							.withTableName(tableName)
							.withItem(attributes));
		} catch (Exception e) {
			statusCode = 500;
			System.out.println(e);
		}
		
		Map<String, String> headers = new HashMap<>();
		headers.put( "Content-Type", "application/json");
		headers.put( "Access-Control-Allow-Origin", "*");
		headers.put( "Cache-Control", "no-store");
		headers.put( "X-Powered-By", "AWS Lambda & serverless");
		return ApiGatewayResponse.builder()
				.setStatusCode(statusCode)
				.setObjectBody(responseBody)
				.setHeaders(headers)
				.build();
	}
	private Map<String, AttributeValue> convert(Map<String, String> map) {
        return Optional.ofNullable(map).orElseGet(HashMap::new).entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> new AttributeValue().withS(e.getValue())));
    }
}
