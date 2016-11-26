package org.sameera.geo;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class createNoe4jDB {
    public static void main( String[] args )
    {
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        GraphDatabaseService db= dbFactory.newEmbeddedDatabase(
                new File("/home/sameera/installations/neo4j-community-3.0.6/data/databases/graph.db"));



        String fileName = "/home/sameera/repos/au_geo_data/AUgeo.json";
        String processedfileName = "/home/sameera/repos/au_geo_data/summary_AUgeo_weka.csv";
        try {

            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));
            String line;
            int count = 0;

            //last = EEE MMM dd hh:mm:ss Z yyyy
            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy"); //EEE MMM dd HH:mm:ss ZZZZZ yyyy
            Date startDate;

            StringBuilder sb = new StringBuilder();

            sb.append("location,tweetid,userid,screen_name,text,created_at,timestamp");
            String data = sb.toString();
            bw.write(data);
            bw.newLine();

            while ((line = br.readLine()) != null && count < 100) {
                JSONObject obj = new JSONObject(line);
                sb = new StringBuilder();

                String startDateString = obj.getString("created_at");
                startDate = df.parse(startDateString);

                sb.append(obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(0));
                sb.append("#");
                sb.append(obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(1));
                sb.append(",");
                sb.append(obj.getDouble("id"));
                sb.append(",");
                sb.append(obj.getJSONObject("user").getLong("id"));
                sb.append(",");
                sb.append(obj.getJSONObject("user").getString("screen_name"));
                sb.append(",");
                sb.append(StringEscapeUtils.escapeJson(obj.getString("text").replace(",", "."))/*.replace("\"", "\\\"")*/);
                sb.append(",");
                sb.append(startDateString);
                sb.append(",");
                sb.append(startDate.getTime());
                data = sb.toString();
//                System.out.println(data);
                System.out.println(count);
                bw.write(data);
                bw.newLine();

                Transaction tx = db.beginTx();

                    Node javaNode = db.createNode(Tutorials.TWEET);
                    javaNode.setProperty("lat", obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(0));
                    javaNode.setProperty("lon", obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(1));
                    javaNode.setProperty("tweetid", obj.getDouble("id"));
                    javaNode.setProperty("userid", obj.getJSONObject("user").getLong("id"));
                    javaNode.setProperty("screen_name", obj.getJSONObject("user").getString("screen_name"));
                    javaNode.setProperty("text", StringEscapeUtils.escapeJson(obj.getString("text").replace(",", ".")));
                    javaNode.setProperty("created_at", startDateString);
                    javaNode.setProperty("timestamp", startDate.getTime());

//                    db.



                    tx.success();
                tx.close();


                count++;
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        db.shutdown();
        System.out.println( "Hello World!" );
        /*
        * tmp.coordinates = json.coordinates.coordinates;
        * tmp.userid = json.user.id;
        * tmp.text = json.text;
        * tmp.created_at = json.created_at;
        *
        * {"coordinates":[115.80792686,-31.71070802],"userid":495099853,"text":"So so so so so excited to go and watch Hunger games!!","created_at":"Sat Mar 31 11:47:08 +0000 2012"}
        */
    }

    public enum Tutorials implements Label {
        TWEET, JAVA,SCALA,SQL,NEO4J;
    }
}
