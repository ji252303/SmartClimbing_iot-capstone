package helloworld;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = "map_db";
    private static final String REGION = "ap-northeast-2";
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
            .withRegion(REGION).build();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            List<Map<String, Object>> items = getAllItemsFromDynamoDB();
            String responseBody = convertItemsToJson(items);

            //response.setStatusCode(200);
            response.setBody(responseBody);
            //response.setHeaders(Map.of("Content-Type", "application/json"));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"Error occurred: " + e.getMessage() + "\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
        }
        return response;
    }

    private List<Map<String, Object>> getAllItemsFromDynamoDB() {
        List<Map<String, Object>> items = new ArrayList<>();
        Table table = dynamoDB.getTable(TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec();
        try {
            ItemCollection<ScanOutcome> outcome = table.scan(scanSpec);
            for (Item item : outcome) {
                items.add(item.asMap());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan DynamoDB table: " + e.getMessage());
        }
        return items;
    }

    private String convertItemsToJson(List<Map<String, Object>> items) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(items);
    }
}
