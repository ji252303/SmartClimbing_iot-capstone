package helloworld;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;


import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Event, String> {
    private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "nano33_2";
    private String REGION = "ap-northeast-2";

    public String handleRequest(final Event input, final Context context) {

        this.initDynamoDbClient();
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);


        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("deviceId = :v_id")
                .withValueMap(new ValueMap().withString(":v_id",input.deviceId));

        ItemCollection<QueryOutcome> items=null;
        try {
            items = table.query(querySpec);
        }
        catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }
        String output = getResponse(items);

        return output;
    }

    private String getResponse(ItemCollection<QueryOutcome> items) {

        Iterator<Item> iter = items.iterator();
        String response = "{ \"data\": [";
        for (int i =0; iter.hasNext(); i++) {
            if (i!=0)
                response +=",";
            response += iter.next().toJSON();
        }
        response += "]}";
        return response;
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
        this.dynamoDb = new DynamoDB(client);
    }

}

class Event {
    public String deviceId;
}