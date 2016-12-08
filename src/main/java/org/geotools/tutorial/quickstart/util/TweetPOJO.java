package org.geotools.tutorial.quickstart.util;

public class TweetPOJO {
    private double[] location;
    private String text;
    private int userid;
    private String screen_name;
    private String created_at;
    private long tweet_id;
    private long timestamp;

    public TweetPOJO(double[] location, String text, int userid, String screen_name,
                     String created_at, long tweet_id, long timestamp) {
        this.location = location;
        this.text = text;
        this.userid = userid;
        this.screen_name = screen_name;
        this.created_at = created_at;
        this.tweet_id = tweet_id;
        this.timestamp = timestamp;
    }


    public double[] getLocation() {
        return location;
    }

    public String getText() {
        return text;
    }

    public int getUserid() {
        return userid;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public long getTweet_id() {
        return tweet_id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
