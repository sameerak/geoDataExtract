package org.anomally.scatterblogs;

import com.mongodb.*;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SemanticLocationCluster {

    private static HashSet<String> stop_word_set = new HashSet<String>();
    private static HashMap<String, HashMap<String, Integer>> locationClusters = new HashMap<String, HashMap<String, Integer>>();

    public static void main(String[] args) throws Exception {
        int count = 0;
        HashMap<String, HashSet<termCluster>> termClusters = new HashMap<String, HashSet<termCluster>>();


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

        int all = dbCursor.count();

        while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            String tmploc = basicObject.get("coordinates").toString();
            tmploc = tmploc.substring(2, tmploc.length() - 1);
            String[] coordinates = tmploc.split(",");
            String tweet = basicObject.get("text").toString();
            Date timestamp = new Date(Long.parseLong(basicObject.get("timestamp").toString()));
            int userID = basicObject.getInt("userid");
            double tweetID = basicObject.getDouble("tweet_id");


            BasicDBObject addressObject = (BasicDBObject) basicObject.get("address");
            BasicDBObject internaladdressObject = (BasicDBObject) addressObject.get("address");

            if (internaladdressObject == null) {
//                errorcount++;
                continue;
            }

            Object[] addressElements = internaladdressObject.keySet().toArray();

            if ("road".equals(addressElements[0])) {
                continue;
            }

//            System.out.println("location " + count + " = " + arr[0]);

            if (!locationClusters.containsKey(addressElements[0])){
                locationClusters.put((String) addressElements[0], new HashMap<String, Integer>());
            }

            HashMap<String, Integer> wordMap = locationClusters.get((String) addressElements[0]);

//            List<String> res = Twokenize.tokenizeRawTweetText(tweet);
            List<String> res = tokenizeStopStem(tweet);
//            System.out.println(res);

            for (int i = 0; i < res.size(); i++) {
                String token = res.get(i);

//                if (i != (res.size() - 1)) {
//                    token = res.get(i) + " " + res.get(i + 1);
//                }
//                System.out.println(token);
//                double [] loc = {Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])};
//                extractedTerm tmpTerm = new extractedTerm(token, tweet, loc, timestamp, userID, tweetID);
                if (!wordMap.containsKey(token)) {
                    wordMap.put(token, 1);
                }
                else {
                    int num = wordMap.get(token);
                    ++num;
                    wordMap.put(token, num);
                }
            }
//            System.out.println("" + tmp);
//            String[] coordinates = tmploc.split(",");
//            TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));

            count++;
        }

        System.out.println("processed record count = " + count);
        String processedfileName = "/home/sameera/repos/au_geo_data/AUgeo_location+wordCount.csv";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));


        System.out.println("Printing loc word counts@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        float maxnoOfDoc = all;
        String maxTerm = "";

        for (String location: locationClusters.keySet()) {
//            System.out.println("term = " + term);
            HashMap<String, Integer> wordMap  = locationClusters.get(location);
            for (String word: wordMap.keySet()) {
                int wordcount = wordMap.get(word);
//                System.out.println("location = " + location + ", word = " + word + ", count = " + wordcount);
                if (wordcount > 1) {
                    String data = location + "," + word + "," + wordcount;
                    bw.write(data);
                    bw.newLine();
                }
            }
        }

        bw.close();

    }


    private static List<String> tokenizeStopStem(String input) throws IOException {
        if (stop_word_set.isEmpty()) {
            String fileName = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" +
                    File.separator + "english.stop.txt";

            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null /*&& count < 10*/) {
                stop_word_set.add(line);
            }
        }

        input = input.toLowerCase();

        List<String> result = new ArrayList<String>();
//        ClassicTokenizer classicTokenizer = new ClassicTokenizer();

        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(input));
        tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, stop_word_set);
        tokenStream = new PorterStemFilter(tokenStream);

        StringBuilder sb = new StringBuilder();
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttr = tokenStream.getAttribute(CharTermAttribute.class);
        try{
            while (tokenStream.incrementToken()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
//                sb.append(charTermAttr.toString());
                String word = charTermAttr.toString();
//                word = word.replaceAll("(.)\\1{1,}", "$1");
                word = word.replaceAll("([a-z])\\1{1,}", "$1$1");
                result.add(word);
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
//        return sb.toString();
        return result;
    }
}
