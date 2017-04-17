package org.word.analysis;

import com.mongodb.*;
import org.trajectory.clustering.Line;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.word.analysis.TweetAnalysis.tokenizeStopStem;

public class WordFrequency {

    public static void main(String[] args) throws ParseException, IOException {
        String processedFileName = "." + File.separator + "AUgeo_word_freq.csv";
        HashMap<Integer,ArrayList<String>> users = new HashMap<Integer,ArrayList<String>>();
        HashMap<String, ExtractedWord> extractedWordSet = new HashMap<String, ExtractedWord>();
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
        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime())
                .add("$lte", endDate.getTime()).get());
        query.put("userid",  BasicDBObjectBuilder.start("$not", new BasicDBObject("$in", list)).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

        while (dbCursor.hasNext()/* && count < 10*/) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            String tweet = basicObject.get("text").toString();
            Long timestamp = basicObject.getLong("timestamp");
            int userid = basicObject.getInt("userid");

            List<String> res = tokenizeStopStem(tweet);

            for (String token : res) {
                ExtractedWord extractedWord;

                if (extractedWordSet.containsKey(token)) {
                    extractedWord = extractedWordSet.get(token);
                    extractedWord.incrementCounter(timestamp, userid);
                } else {
                    extractedWord = new ExtractedWord(token, timestamp, userid);
                    extractedWordSet.put(token, extractedWord);
                }
            }
        }

        ArrayList<ExtractedWord> extractedWordList;
        extractedWordList = new ArrayList<ExtractedWord>();
        extractedWordList.addAll(extractedWordSet.values());

        for (ExtractedWord extractedWord : extractedWordList) {
            extractedWord.setFrequency();
        }

        Collections.sort(extractedWordList);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedFileName)));
        int recordCount = 0;

        for (ExtractedWord extractedWord : extractedWordList) {
            if (extractedWord.getUserIDCount() < 2 || extractedWord.getCount() > 700) {
                continue;
            }
            //order = term, frequency, count, startTime, endTime, usercount
            String data = extractedWord.getTerm().replace(',','.') + "," + extractedWord.getFrequency() + ","
                    + extractedWord.getCount() + "," + extractedWord.getStartTime()
                    + "," + extractedWord.getEndTime()+ "," + extractedWord.getUserIDCount();
            bw.write(data);
            bw.newLine();
            ++recordCount;

            if (recordCount > 1000) {
                break;
            }
        }

        bw.close();
    }
}
