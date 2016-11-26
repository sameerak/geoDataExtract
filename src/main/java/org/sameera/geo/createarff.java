package org.sameera.geo;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sameera on 10/18/16.
 */
public class createarff {

    public static void main( String[] args )
    {
        String fileName = "/home/sameera/repos/au_geo_data/AUgeo.json";
        String processedfileName = "/home/sameera/repos/au_geo_data/summary_AUgeo_weka_full.arff";
        try {

            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));
            String line;
            int count = 0;

            //last = EEE MMM dd hh:mm:ss Z yyyy
            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy"); //EEE MMM dd HH:mm:ss ZZZZZ yyyy
            Date startDate;

            StringBuilder sb = new StringBuilder();

            sb.append("@relation tweets\n" +
                    "\n" +
                    "@attribute lat real\n" +
                    "@attribute long real\n" +
                    "@attribute tweetid real\n" +
                    "@attribute userid real\n" +
                    "@attribute screen_name string \n" +
                    "@attribute text string \n" +
                    "@attribute vectortext string \n" +
                    "@attribute created_at string \n" + //date 'EEE MMM dd HH:mm:ss ZZZZZ yyyy'
                    "@attribute timestamp real\n" +
                    "\n" +
                    "@data");
            String data = sb.toString();
            bw.write(data);
            bw.newLine();

            while ((line = br.readLine()) != null/* && count < 100*/) {
                JSONObject obj = new JSONObject(line);
                sb = new StringBuilder();

                String startDateString = obj.getString("created_at");
                startDate = df.parse(startDateString);

                sb.append(obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(0));
                sb.append(",");
                sb.append(obj.getJSONObject("coordinates").getJSONArray("coordinates").getDouble(1));
                sb.append(",");
                sb.append(obj.getDouble("id"));
                sb.append(",");
                sb.append(obj.getJSONObject("user").getLong("id"));
                sb.append(",");
                sb.append(obj.getJSONObject("user").getString("screen_name"));
                sb.append(",\"");
                sb.append(StringEscapeUtils.escapeJson(obj.getString("text").replace(",", "."))/*.replace("\"", "\\\"")*/);
                sb.append("\",\"");
                sb.append(StringEscapeUtils.escapeJson(obj.getString("text").replace(",", "."))/*.replace("\"", "\\\"")*/);
                sb.append("\",\"");
                sb.append(startDateString);
                sb.append("\",");
                sb.append(startDate.getTime());
                data = sb.toString();
//                System.out.println(data);
                System.out.println(count);
                bw.write(data);
                bw.newLine();
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
}
