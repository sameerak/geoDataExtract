package org.trajectory.clustering;

import java.security.PublicKey;

public class TrajectoryPoint {
    private double x,y;
    private long tweetID;
    private long timestamp;

    public TrajectoryPoint(double x, double y, long tweetID, long timestamp){
        this.x = x;
        this.y = y;
        this.tweetID = tweetID;
        this.timestamp  = timestamp;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public long getTweetID() {
        return tweetID;
    }

    public void setTweetID(long tweetID) {
        this.tweetID = tweetID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
