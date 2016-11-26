package org.anomally.scatterblogs;

import java.util.Date;
import java.util.StringTokenizer;

public class extractedTerm {

    private String term;
    private double[] location;
    private Date timestamp;
    private int userID;
    private double tweetID;

    public extractedTerm (String term, double[] location, Date timestamp, int userID, double tweetID) {
        this.term = term;
        this.location = location;
        this.timestamp = timestamp;
        this.userID = userID;
        this.tweetID = tweetID;
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

//    public void setTimestamp(Date timestamp) {
//        this.timestamp = timestamp;
//    }
}
