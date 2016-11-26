package org.sameera.geo;


import com.mongodb.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class mongoTimestamp {

    public static void main( String[] args )
    {
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
        DBCollection collection = db.getCollection("tweetTimestamps");
        DBCursor dbCursor = collection.find().sort(new BasicDBObject("timestamp", 1));

        Date oldDate = null,checkdate;

        try {

            while (dbCursor.hasNext() /*&& count < 6*/) {
                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                checkdate = new Date(basicObject.getLong("timestamp"));

                if (oldDate == null || (oldDate != null && oldDate.getDate() != checkdate.getDate())) {
                    if (bw != null) {
                        bw.close();
                    }
                    processedfileName = processedfileNamePrefix + df.format(checkdate);
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));
                }
                StringBuilder sb = new StringBuilder();

                sb.append("{\"coordinates\":");
                sb.append(basicObject.get("coordinates"));
                sb.append(",\"userid\":");
                sb.append(basicObject.getLong("userid"));
                sb.append(",\"text\":\"");
                sb.append(StringEscapeUtils.escapeJson(basicObject.getString("text"))/*.replace("\"", "\\\"")*/);
                sb.append("\",\"created_at\":\"");
                sb.append(basicObject.getString("created_at"));
                sb.append("\",\"timestamp\":");
                sb.append(checkdate.getTime());
                sb.append("}");
                String data = sb.toString();

                bw.write(data);
                bw.newLine();
//                System.out.println(data);
                System.out.println(count);

                oldDate = checkdate;
                count++;
            }

            if (bw != null) {
                bw.close();
            }

        } catch (Exception e) {

        }
    }
}
