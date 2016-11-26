package org.sameera.geo;

import com.mongodb.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class mongoAddLocationType {
    private static final String USER_AGENT = "Mozilla/5.0";

    // 3 does not work ;-)
    public static int noOfThreads = 2;

    public static void main( String[] args )
    {
        Worker[] wrkrArray = new Worker[noOfThreads];

        for (int i = 0; i < noOfThreads; i++) {
            wrkrArray[i] = new Worker(i);
            wrkrArray[i].start();
        }

    }


    public static class Worker extends Thread {

        private int divider = 0;

        public Worker (int divider) {
            this.divider = divider;
        }


        @Override
        public void run() {

            HashMap<String, DBObject> knownLocaitons = new HashMap<String, DBObject>();
            int count = 0;

            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");


            BasicDBObject query = new BasicDBObject();
            query.put("address", null);

            for (int i = 0; i < 100000; i++) {

                DBCursor dbCursor = collection.find(query).skip(100 * (divider + (noOfThreads * i))).limit(100).addOption(Bytes.QUERYOPTION_NOTIMEOUT).sort(new BasicDBObject("timestamp", 1));

                System.out.println("remaining data count = " + dbCursor.count());
                if (dbCursor.count() == 0) {
                    break;
                }

                try {

                    while (dbCursor.hasNext()/* && count < 1*/) {
                        BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                        long tweetid = basicObject.getLong("tweet_id");

//                        if (count % noOfThreads != divider) {
//                            count++;
//                            continue;
//                        }

                        String tmp = basicObject.get("coordinates").toString();
                        tmp = tmp.substring(2, tmp.length() - 1);
    //            System.out.println("" + tmp);
                        String[] coordinates = tmp.split(",");
                        System.out.println("thread = " + divider + ", tweet_id = " + tweetid);

                        if (basicObject.containsField("address")){
                            System.out.println("alreday fetched");
                            continue;
                        }

                        DBObject dbObj;

                        if (knownLocaitons.containsKey(tmp)) {
                            dbObj = knownLocaitons.get(tmp);
                        }
                        else {
                            String url = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" + Double.parseDouble(coordinates[1]) + "&lon=" + Double.parseDouble(coordinates[0]) + "&addressdetails=1";

                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                            // optional default is GET
                            con.setRequestMethod("GET");

                            //add request header
                            con.setRequestProperty("User-Agent", USER_AGENT);

                            int responseCode = con.getResponseCode();
    //                System.out.println("\nSending 'GET' request to URL : " + url);
                            System.out.println("Response Code : " + responseCode);

                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

    //                Object o = com.mongodb.util.JSON.parse("{\"house_number\":\"137\",\"road\":\"Pilkington Avenue\",\"suburb\":\"Sutton Coldfield\",\"hamlet\":\"Maney\",\"city\":\"Birmingham\",\"state_district\":\"West Midlands\",\"state\":\"England\",\"postcode\":\"B72 1LH\",\"country\":\"UK\",\"country_code\":\"gb\"}");
                            Object o = com.mongodb.util.JSON.parse(response.toString());
                            dbObj = (DBObject) o;
                            knownLocaitons.put(tmp, dbObj);
                        }

                        BasicDBObject newDocument = new BasicDBObject();
                        newDocument.append("$set", new BasicDBObject().append("address", dbObj));

                        BasicDBObject searchQuery = new BasicDBObject().append("coordinates", basicObject.get("coordinates"));

                        BasicDBObject query1 = new BasicDBObject();
                        query1.put("coordinates.1", BasicDBObjectBuilder.start("$gte", dbObj.get("boundingbox.0")).add("$lte", dbObj.get("boundingbox.1")).get());
                        query1.put("coordinates.0", BasicDBObjectBuilder.start("$gte", dbObj.get("boundingbox.2")).add("$lte", dbObj.get("boundingbox.3")).get());

                        collection.updateMulti(searchQuery, newDocument);

                        System.out.println("thread = " + divider + ", count = " + count);
//                        if (count % noOfThreads == divider) {
                            //broke in 500
                            Thread.sleep(600);
//                        }

                        count++;
                    }


                } catch (Exception e) {
                    System.out.println("cause = " + e.getCause());
                    System.out.println("message = " + e.getMessage());
                    System.out.println(e.getStackTrace());

                }
            }
        }

    }

}
