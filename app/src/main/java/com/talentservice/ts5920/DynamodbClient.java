package com.talentservice.ts5920;

/**
 * Created by doug on 9/3/15.
 */

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.TimeZone;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;




public class DynamodbClient{


    static BasicAWSCredentials bac = new BasicAWSCredentials("AKIAJZSJBGVWJOFLZQGQ","zbdE/pczscs8xFDRWU4aGJ0Z4JWzSmGlrzp2A9In");
    static DynamoDBMapper mapper = new DynamoDBMapper(new AmazonDynamoDBClient(bac));

	public static void println(Object msg){
		System.out.println(msg);

	}

	public void modifyItem(TS5920Item item){
		mapper.save(item);
	}

	public void new5920(String uuid, String target, String desc, String form) {
	}

	public boolean existingUID(String uid){

		DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression();

		Map<String, Condition> queryFilter = new HashMap<String, Condition>();

		// Condition1: DeviceId
		Condition scanCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(uid));
		queryFilter.put("uid", scanCondition);
		scanExpression.setQueryFilter(queryFilter);

		TS5920Item result = mapper.load(TS5920Item.class, uid);
		if (result != null ){
			return true;
		}else{
			return false;
		}


	}

	public TS5920Item getItemByUid(String uid){
		DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression();

		Map<String, Condition> queryFilter = new HashMap<String, Condition>();

		// Condition1: DeviceId
		Condition scanCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(uid));
		queryFilter.put("uid", scanCondition);
		scanExpression.setQueryFilter(queryFilter);
        return mapper.load(TS5920Item.class,uid);
		//List<TS5920Item> results = mapper.query(TS5920Item.class, scanExpression);
		//if (results.size() > 0){
		//	return results.get(0);
		//}else{
		//	return null;
		//}

	}
	public boolean existingTarget(String target){
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

		Map<String, Condition> scanFilter = new HashMap<String, Condition>();

		// Condition1: DeviceId
		Condition scanCondition = new Condition()
				.withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(target));
		scanFilter.put("target", scanCondition);
		scanExpression.setScanFilter(scanFilter);

		List<TS5920Item> results = mapper.scan(TS5920Item.class, scanExpression);
		if (results.size() > 0){
			return true;
		}else{
			return false;
		}

	}

        public void putItem(String uid, String target, String desc, String from){

		TS5920Item item = new TS5920Item();
                long timestamp = (long) (System.currentTimeMillis()/1000);
		item.setUid(uid);
		item.setFrom(from);
		item.setTimestamp(timestamp);
		item.setTarget(target);
		item.setDesc(desc);
			mapper.save(item);
        }

 @DynamoDBTable(tableName="ts_5920")
    public static class TS5920Item {
        private String uid;
        private long timestamp;
        private String from;
        private String desc;
        private String target;

        @DynamoDBHashKey(attributeName="uid")
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }

        @DynamoDBAttribute(attributeName="timestamp")
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        @DynamoDBAttribute(attributeName="from")
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        @DynamoDBAttribute(attributeName="desc")
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc;}

        @DynamoDBAttribute(attributeName="target")
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target;}

        @Override
        public String toString() {
            return "ts5920 [uid=" + uid + ", from=" + from
            + ", desc=" + desc + ", target=" + target + "]";
        }
    }
        
/**
	public static void main(String argv[]){

   		println("==== dynamodbClient === ");

 		DynamodbClient dc = new DynamodbClient();

		println(dc.existingTarget("謝小姍"));
		println(dc.existingTarget("不存在"));
		println(dc.existingUID("不存在"));
		println(dc.existingUID("uuid12312312313213"));
 	        dc.putItem("uuid12312312313213","謝小姍","我愛你~","老公");
       
	}*/
}
