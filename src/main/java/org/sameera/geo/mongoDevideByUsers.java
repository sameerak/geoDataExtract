package org.sameera.geo;

import com.mongodb.*;

import java.io.BufferedWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class mongoDevideByUsers {

    public static void main( String[] args ) {
        String processedfileNamePrefix = "/home/sameera/repos/au_geo_data/summary_AUgeo";
        String processedfileName;
        BufferedWriter bw = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        int count = 0;

        //connecting mongoDB on local machine.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        //connecting to database named test
        DB db = mongoClient.getDB("test");
        // getting collection of all files
        DBCollection collection = db.getCollection("timestamp");

        BasicDBObject query = new BasicDBObject();
//        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());
//        query.put("userid",  BasicDBObjectBuilder.start("$not", new BasicDBObject("$in", list)).get());


        DBCursor dbCursor = collection.find().sort(new BasicDBObject("timestamp", 1));

    }
}
