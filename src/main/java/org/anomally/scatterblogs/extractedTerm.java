package org.anomally.scatterblogs;

import java.util.Date;
import java.util.StringTokenizer;

public class extractedTerm {

    private String term;
    private String text;
    private String created_at;
    private double[] location;
    private Date timestamp;
    private int userID;
    private double tweetID;

    public extractedTerm (String term, String text, String created_at, double[] location, Date timestamp, int userID, double tweetID) {
        this.term = term;
        this.text = text;
        this.location = location;
        this.timestamp = timestamp;
        this.userID = userID;
        this.tweetID = tweetID;
        this.created_at = created_at;
    }

    public String getText() {
        return text;
    }

    public double[] getLocation() {
        return location;
    }

    public String getPrintableLocation() {
        return "[" + location[0] + "," + location[1] + "]";
    }

//    public void setLocation(int[] location) {
//        this.location = location;
//    }

    public String getTerm() {
        return term;
    }

//    public void setTerm(String term) {
//        this.term = term;
//    }

    public Date getTimestamp() {
        return timestamp;
    }

    public double getTweetID() {
        return tweetID;
    }

    public int getUserID() {
        return userID;
    }

    public String getCreated_at() {
        return created_at;
    }

//    public void setTimestamp(Date timestamp) {
//        this.timestamp = timestamp;
//    }
}
