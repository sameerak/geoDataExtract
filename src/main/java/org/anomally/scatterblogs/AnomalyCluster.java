package org.anomally.scatterblogs;


import com.mongodb.*;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.*;

public class AnomalyCluster {

    private static HashSet<String> stop_word_set = new HashSet<String>();
    private static HashMap<String, HashSet<termCluster>> termClusters = new HashMap<String, HashSet<termCluster>>();

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

//            List<String> res = Twokenize.tokenizeRawTweetText(tweet);
            List<String> res = tokenizeStopStem(tweet);
//            System.out.println(res);

            for (String token: res) {
//                System.out.println(token);
                double [] loc = {Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])};
                extractedTerm tmpTerm = new extractedTerm(token, loc, timestamp, userID, tweetID);
                if (!termClusters.containsKey(token)) {
                    termCluster newCluster = new termCluster(tmpTerm);
                    HashSet<termCluster> tmpClusterSet = new HashSet<termCluster>();
                    tmpClusterSet.add(newCluster);
                    termClusters.put(token, tmpClusterSet);
//                    System.out.println("term = " + token + ", FIRST tmpTerm loc = " + tmpTerm.getPrintableLocation());
                }
                else {
                    HashSet<termCluster> tmpClusterSet = termClusters.get(token);
                    termCluster tmpClosestCluster = null;
                    double distance = -1;
                    //checking for the closest termCluster
                    for (termCluster clust: tmpClusterSet ) {
                        double tmpDist = getDistance(clust.getCentroid(), tmpTerm.getLocation());
                        if (distance == -1 || distance > tmpDist) {
                            tmpClosestCluster = clust;
                            distance = tmpDist;
                        }
                    }

//                    System.out.println("tmpClosestCluster centroid before adding = " + tmpClosestCluster.getPrintableCentroid());
//                    System.out.println("tmpTerm loc = " + tmpTerm.getPrintableLocation());

                    tmpClosestCluster.addTerm(tmpTerm);
//                    System.out.println("tmpClosestCluster centroid AFTER adding = " + tmpClosestCluster.getPrintableCentroid());

                    //for updated termCluster if too much distortion break in to new clusters
//                    System.out.println("term = " + token + ", #tems = " + tmpClosestCluster.getReg().size() + ", distortion for updated cluster = " + getDistortion(tmpClosestCluster));
                    if (getDistortion(tmpClosestCluster) > 3) {


                        KMeans splitter = new KMeans(tmpClosestCluster);
                        List<termCluster> clusters = splitter.calculate();

//                    System.out.println("SPLITTED ---- term = " + token + ", #terms = " + clusters.get(0).getReg().size() + ", distortion for updated cluster = " + getDistortion(clusters.get(0)));
//                    System.out.println("SPLITTED ---- term = " + token + ", #terms = " + clusters.get(1).getReg().size() + ", distortion for updated cluster = " + getDistortion(clusters.get(1)));

                        for (termCluster splitted: clusters) {
                            if (splitted.getReg().size() == tmpClosestCluster.getReg().size() && getDistortion(tmpClosestCluster) != getDistortion(splitted)) {
                                System.out.println("NOT RIGHT!!!!!!!!!!!!!!!");
                            }
                        }

                        tmpClusterSet.remove(tmpClosestCluster);
                        tmpClusterSet.add(clusters.get(0));
                        tmpClusterSet.add(clusters.get(1));
                    }
                }
            }
//            System.out.println("" + tmp);
//            String[] coordinates = tmploc.split(",");
//            Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));


        }
        String processedfileName = "/home/sameera/repos/au_geo_data/AUgeo_important_words.csv";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));


        System.out.println("Calculating docFreq@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        float maxnoOfDoc = all;
        String maxTerm = "";

        for (String term: termClusters.keySet()) {
//            System.out.println("term = " + term);
            float noOfDoc = 0;
            for (termCluster clust: termClusters.get(term)) {
//                System.out.println("terms = " + clust.getReg().size());
//                System.out.println("terms = " + clust.getReg().size() + ", centroid = [" + clust.getCentroid()[0] + "," + clust.getCentroid()[1] + "]");
                noOfDoc += clust.getReg().size();
            }
            if (noOfDoc < maxnoOfDoc) {
                maxnoOfDoc = noOfDoc;
                maxTerm = term;
            }
//            float docFreq = noOfDoc / 16893;
            float docFreq = all / noOfDoc;
            if (100000 > docFreq && docFreq > 25) {
                System.out.println("term = " + term + ", docFreq = " + docFreq);
                String data = term + "," + docFreq;
                bw.write(data);
                bw.newLine();
            }
        }

        bw.close();
            float docFreq = all / maxnoOfDoc;
                System.out.println("max term = " + maxTerm + ", docFreq = " + docFreq);

    }

    public static HashMap<String, HashSet<termCluster>> getAnomalyset(Date start, Date end) throws IOException {
        HashMap<String, HashSet<termCluster>> termClusters = new HashMap<String, HashSet<termCluster>>();

        //connecting mongoDB on local machine.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        //connecting to database named test
        DB db = mongoClient.getDB("test");
        // getting collection of all files
        DBCollection collection = db.getCollection("correctTweetId");

        BasicDBObject query = new BasicDBObject();
        query.put("timestamp", BasicDBObjectBuilder.start("$gte", start.getTime()).add("$lte", end.getTime()).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

        while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            String tmploc = basicObject.get("coordinates").toString();
            tmploc = tmploc.substring(2, tmploc.length() - 1);
            String[] coordinates = tmploc.split(",");
            String tweet = basicObject.get("text").toString();
            Date timestamp = new Date(Long.parseLong(basicObject.get("timestamp").toString()));
            int userID = basicObject.getInt("userid");
            double tweetID = basicObject.getDouble("tweet_id");

//            List<String> res = Twokenize.tokenizeRawTweetText(tweet);
            List<String> res = tokenizeStopStem(tweet);
//            System.out.println(res);

            for (String token: res) {
//                System.out.println(token);
                double [] loc = {Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])};
                extractedTerm tmpTerm = new extractedTerm(token, loc, timestamp, userID, tweetID);
                if (!termClusters.containsKey(token)) {
                    termCluster newCluster = new termCluster(tmpTerm);
                    HashSet<termCluster> tmpClusterSet = new HashSet<termCluster>();
                    tmpClusterSet.add(newCluster);
                    termClusters.put(token, tmpClusterSet);
//                    System.out.println("term = " + token + ", FIRST tmpTerm loc = " + tmpTerm.getPrintableLocation());
                }
                else {
                    HashSet<termCluster> tmpClusterSet = termClusters.get(token);
                    termCluster tmpClosestCluster = null;
                    double distance = -1;
                    //checking for the closest termCluster
                    for (termCluster clust: tmpClusterSet ) {
                        double tmpDist = getDistance(clust.getCentroid(), tmpTerm.getLocation());
                        if (distance == -1 || distance > tmpDist) {
                            tmpClosestCluster = clust;
                            distance = tmpDist;
                        }
                    }

//                    System.out.println("tmpClosestCluster centroid before adding = " + tmpClosestCluster.getPrintableCentroid());
//                    System.out.println("tmpTerm loc = " + tmpTerm.getPrintableLocation());

                    tmpClosestCluster.addTerm(tmpTerm);
//                    System.out.println("tmpClosestCluster centroid AFTER adding = " + tmpClosestCluster.getPrintableCentroid());

//                    System.out.println("term = " + token + ", #tems = " + tmpClosestCluster.getReg().size() + ", distortion for updated cluster = " + getDistortion(tmpClosestCluster));
                    if (getDistortion(tmpClosestCluster) > 3) {


                        KMeans splitter = new KMeans(tmpClosestCluster);
                        List<termCluster> clusters = splitter.calculate();

//                        System.out.println("SPLITTED ---- term = " + token + ", #tems = " + clusters.get(0).getReg().size() + ", distortion for updated cluster = " + getDistortion(clusters.get(0)));
//                        System.out.println("SPLITTED ---- term = " + token + ", #tems = " + clusters.get(1).getReg().size() + ", distortion for updated cluster = " + getDistortion(clusters.get(1)));

                        for (termCluster splitted: clusters) {
                            if (splitted.getReg().size() == tmpClosestCluster.getReg().size() && getDistortion(tmpClosestCluster) != getDistortion(splitted)) {
                                System.out.println("############################## NOT RIGHT!!!!!!!!!!!!!!!");
                            }
                        }

                        tmpClusterSet.remove(tmpClosestCluster);
                        tmpClusterSet.add(clusters.get(0));
                        tmpClusterSet.add(clusters.get(1));
                    }
                }
            }
//            System.out.println("" + tmp);
//            String[] coordinates = tmploc.split(",");
//            Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));


        }



        System.out.println("Calculating docFreq@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        float maxnoOfDoc = 0;
        String maxTerm = "";

        for (String term: termClusters.keySet()) {
//            System.out.println("term = " + term);
            float noOfDoc = 0;
            for (termCluster clust: termClusters.get(term)) {
//                System.out.println("terms = " + clust.getReg().size());
//                System.out.println("terms = " + clust.getReg().size() + ", centroid = [" + clust.getCentroid()[0] + "," + clust.getCentroid()[1] + "]");
                noOfDoc += clust.getReg().size();
            }
            if (noOfDoc > maxnoOfDoc) {
                maxnoOfDoc = noOfDoc;
                maxTerm = term;
            }
//            float docFreq = noOfDoc / 16893;
            float docFreq = noOfDoc / 724651;
            if (docFreq > 0.05)
                System.out.println("term = " + term + ", docFreq = " + docFreq);
        }

        return termClusters;
    }

    private static double getDistortion(termCluster updatedCluster) {
        double[] centroid = updatedCluster.getCentroid();
        HashSet<extractedTerm> reg = updatedCluster.getReg();

        double sqrdDis = 0;
        for (extractedTerm term: reg) {
            sqrdDis += getDistance(centroid, term.getLocation());
        }

        return Math.sqrt(sqrdDis / (reg.size() * 3));
    }

    private static double getDistance(double[] p1, double[] p2) {
        double lat = p1[0] - p2[0];
        double longi = p1[1] - p2[1];

        return lat * lat + longi * longi;
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
