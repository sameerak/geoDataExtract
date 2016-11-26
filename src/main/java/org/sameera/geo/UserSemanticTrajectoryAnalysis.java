package org.sameera.geo;

import com.mongodb.*;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserSemanticTrajectoryAnalysis {

    public static void main( String[] args ) throws ParseException, IOException {

        String processedfileName = "/home/sameera/repos/au_geo_data/AUgeo_#users_#StopSeq.csv";
        HashMap<Integer,ArrayList<String>> users = new HashMap<Integer,ArrayList<String>>();
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

        BasicDBObject query = new BasicDBObject();
        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());
        query.put("userid",  BasicDBObjectBuilder.start("$not", new BasicDBObject("$in", list)).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));



        while (dbCursor.hasNext()/* && count < 1000*/) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            int userID = basicObject.getInt("userid");

            BasicDBObject addressObject = (BasicDBObject) basicObject.get("address");
            BasicDBObject internaladdressObject = (BasicDBObject) addressObject.get("address");

            if (internaladdressObject == null) {
                errorcount++;
                continue;
            }

            Object[] addressElements = internaladdressObject.keySet().toArray();

            if ("road".equals(addressElements[0])) {
                continue;
            }

//            System.out.println("location " + count + " = " + arr[0]);

            if (!users.containsKey(userID)){
                ArrayList<String> stops = new ArrayList<String>();
                stops.add((String) addressElements[0]);
                users.put(userID, stops);
            } else {
                users.get(userID).add((String) addressElements[0]);
//                ArrayList<String> num =  users.get(userID).add((String) addressElements[0]);
//                num++;
//                places.put((String) addressElements[0], num);
            }
            count++;
        }

        System.out.println("Total record count = " + count);
        System.out.println("Total ERROR  count = " + errorcount);
        int i = 0;

        HashMap<Integer, Integer> grouping = new HashMap<Integer, Integer>();

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));
        for (int userid : users.keySet()) {
//            String data = i + "," + userid + "," + users.get(userid).size();
//            System.out.println(data);
//            bw.write(data);
//            bw.newLine();
//            ++i;
            if (!grouping.containsKey(users.get(userid).size())){
                grouping.put(users.get(userid).size(), 1);
            } else {
                int num =  grouping.get(users.get(userid).size());
                num++;
                grouping.put(users.get(userid).size(), num);
            }

        }
//        bw.close();

        for (int Numloc: grouping.keySet()) {
            String data = i + "," + Numloc + "," + grouping.get(Numloc);
            System.out.println(data);
            bw.write(data);
            bw.newLine();
            ++i;
        }
        bw.close();
    }
}
