package org.sameera.geo;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StopTypeAnalysis {

    public static void main( String[] args ) throws ParseException {
        HashMap<String,Integer> places = new HashMap<String,Integer>();
        int count = 0,errorcount = 0;
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        Date startDate = df.parse("Sat Mar 06 00:00:00 +0000 2012");
        Date endDate = df.parse("Mon Apr 23 00:00:00 +0000 2012");
//        Date endDate = df.parse("Sat Mar 07 00:00:00 +0000 2012");

        //connecting mongoDB on local machine.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        //connecting to database named test
        DB db = mongoClient.getDB("test");
        // getting collection of all files
        DBCollection collection = db.getCollection("correctTweetId");

        BasicDBObject query = new BasicDBObject();
        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));



        while (dbCursor.hasNext() && count < 10) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            BasicDBObject addressObject = (BasicDBObject) basicObject.get("address");
            BasicDBObject internaladdressObject = (BasicDBObject) addressObject.get("address");

            if (internaladdressObject == null) {
                errorcount++;
                continue;
            }

            Object[] addressElements = internaladdressObject.keySet().toArray();

//            System.out.println("location " + count + " = " + arr[0]);

            if (!places.containsKey(addressElements[0])){
                places.put((String) addressElements[0], 1);
            } else {
                int num =  places.get(addressElements[0]);
                num++;
                places.put((String) addressElements[0], num);
            }
            count++;
        }

        System.out.println("Total record count = " + count);
        System.out.println("Total ERROR  count = " + errorcount);
        int i = 0;
        for (String place : places.keySet()) {
            System.out.println(i + "," + place + "," + places.get(place));
            ++i;
        }
    }
}
