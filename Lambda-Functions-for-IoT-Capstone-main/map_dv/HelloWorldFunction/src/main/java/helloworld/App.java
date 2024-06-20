package helloworld;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<Map, String> {
    private DynamoDB dynamoDb;
    private String TABLE_NAME = "map_db";
    private String REGION = "ap-northeast-2";

    @Override
    public String handleRequest(Map input, Context context) {
        this.initDynamoDbClient();

        putData(input);
        return "Saved Successfully!!";
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION).build();
        this.dynamoDb = new DynamoDB(client);
    }

    private PutItemOutcome putData(Map map)
            throws ConditionalCheckFailedException {
        return this.dynamoDb.getTable(TABLE_NAME)
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withPrimaryKey("root",map.root)
                                .withString("hold_numbering", map.hold_numbering)
                                .withFloat("x", map.x)
                                .withFloat("y", map.y)
                                .withFloat("rx", map.rx)
                                .withFloat("ry", map.ry)
                                .withString("uid", map.uid)
                        ));
    }
}

class Map {
    public String root;
    public String hold_numbering;
    public float x;
    public float y;
    public float rx;
    public float ry;
    public String uid;

}