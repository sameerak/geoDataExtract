package org.sameera.geo;


import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.*;

public class App
{
    public static void main( String[] args )
    {
        String fileName = "/home/sameera/repos/au_geo_data/AUgeo.json";
        String processedfileName = "/home/sameera/repos/au_geo_data/summary_AUgeo_test.json";
        try {

            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(processedfileName)));
            String line;
            int count = 0;

            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
            Date startDate;

            while ((line = br.readLine()) != null/* && count < 6*/) {
                JSONObject obj = new JSONObject(line);
                StringBuilder sb = new StringBuilder();

                String startDateString = obj.getString("created_at");
                startDate = df.parse(startDateString);

                sb.append("{\"coordinates\":");
                sb.append(obj.getJSONObject("coordinates").getJSONArray("coordinates"));
                sb.append(",\"tweet_id\":");
                sb.append(obj.getLong("id"));
                sb.append(",\"screen_name\":\"");
                sb.append(obj.getJSONObject("user").getString("screen_name"));
                sb.append("\",\"userid\":");
                sb.append(obj.getJSONObject("user").getLong("id"));
                sb.append(",\"text\":\"");
                sb.append(StringEscapeUtils.escapeJson(obj.getString("text"))/*.replace("\"", "\\\"")*/);
                sb.append("\",\"created_at\":\"");
                sb.append(startDateString);
                sb.append("\",\"timestamp\":");
                sb.append(startDate.getTime());
                sb.append("}");
//                System.out.println("created_at = " + startDateString + " formatted = " + df.format(startDate));
                String data = sb.toString();
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
