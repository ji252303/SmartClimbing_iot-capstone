package helloworld;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<Map<String, String>, String> {

    private static final String TABLE_NAME = "nano33_3";

    private AmazonDynamoDB dynamoDBClient;

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        // 입력 받은 데이터 로그에 기록
        context.getLogger().log("Input: " + input);

        // 입력 데이터에서 deviceId 추출
        String deviceId = input.get("deviceId");
        if (deviceId == null || deviceId.isEmpty()) {
            return "Error: deviceId is required";
        }

        // AmazonDynamoDB 클라이언트 초기화
        this.initDynamoDbClient();

        try {
            // 특정 deviceId에 해당하는 항목들을 스캔하여 검색
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("deviceId = :deviceId")
                    .withValueMap(new HashMap<String, Object>() {{
                        put(":deviceId", deviceId);
                    }});

            DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
            Table table = dynamoDB.getTable(TABLE_NAME);
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);

            // 검색된 각 항목을 삭제
            for (Item item : items) {
                DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                        .withPrimaryKey("deviceId", deviceId, "time", item.getLong("time"));
                table.deleteItem(deleteItemSpec);
            }

            return "Items with deviceId " + deviceId + " deleted successfully";
        } catch (Exception e) {
            // 오류 발생 시 로그에 오류 메시지 기록하고 에러 반환
            context.getLogger().log("Error during batch delete: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private void initDynamoDbClient() {
        this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion("ap-northeast-2")
                .build();
    }
}
