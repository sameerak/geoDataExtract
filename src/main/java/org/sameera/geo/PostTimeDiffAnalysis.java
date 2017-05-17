package org.sameera.geo;

import com.mongodb.*;
import com.vividsolutions.jts.geom.Coordinate;
import org.word.analysis.ExtractedWord;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PostTimeDiffAnalysis {
    public static void main(String[] args) throws ParseException, IOException {
        HashMap<Integer, ArrayList<String>> users = new HashMap<Integer, ArrayList<String>>();
        HashMap<String, ExtractedWord> extractedWordSet = new HashMap<String, ExtractedWord>();
        int count = 0, errorcount = 0;
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        Date startDate = df.parse("Sat Mar 06 00:00:00 +0000 2012");
        Date endDate = df.parse("Mon Apr 23 00:00:00 +0000 2012");
//        Date endDate = df.parse("Sat Mar 07 00:00:00 +0000 2012");

        List<Integer> list = new ArrayList<Integer>();
        list.add(427657635);
        list.add(425795784);
        list.add(427675375);
        list.add(464843705);
        list.add(464866980);
        list.add(467427303);
        list.add(467478489);
        list.add(467483686);
        list.add(467487202);
        list.add(467491344);
        list.add(61043461);
        list.add(61043172);
        list.add(416661318);
        list.add(85636450);

        //after creating MOBILITY measure
        list.add(379712969);
        list.add(88054194);
        list.add(108435619);
        list.add(25251712);
        list.add(198412791);
        list.add(123791748);
        list.add(124026965);
        list.add(203345009);
        list.add(194494717);
        list.add(114681958);

        //temp remove
        list.add(96142209);

        //connecting mongoDB on local machine.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        //connecting to database named test
        DB db = mongoClient.getDB("test");
        // getting collection of all files
        DBCollection collection = db.getCollection("correctTweetId");

        BasicDBObject query = new BasicDBObject();
        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime())
                .add("$lte", endDate.getTime()).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("userid", 1).append("timestamp", 1));

        long previousTimestamp = 0;
        int previousUserId = 0;
        int segmentNumber;
        long timeThreshold = 3600000 * 5; //5 hours
        long timeSegmentSize = 10 * 60 * 1000; //10 minutes
        int[] segmentCounts = new int[(int) (timeThreshold / timeSegmentSize) + 1];

        while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {

            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            int userid = basicObject.getInt("userid");
            long timestamp = basicObject.getLong("timestamp");

            if (previousUserId == userid
                    && timestamp - previousTimestamp <= timeThreshold) {
                //calculate time diff
                segmentNumber = (int) ((timestamp - previousTimestamp) / timeSegmentSize);
                //persist for further processing
                ++segmentCounts[segmentNumber];
            }
            previousTimestamp = timestamp;
            previousUserId = userid;
            count++;
        }

        String processedFileName = "." + File.separator + "SegmentCounts_" + timeSegmentSize/60000 + ".csv";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedFileName)));
        String data = "segmentNo,count";
        bw.write(data);
        bw.newLine();

        for (int i = 0; i < segmentCounts.length; i++) {
            data = i + "," + segmentCounts[i];
            bw.write(data);
            bw.newLine();
        }

        bw.close();
    }
}
