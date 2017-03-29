package org.dataonfocus.clustering.structures;

import com.mongodb.BasicDBObject;

import java.util.List;

public class DataPointImpl implements DataPoint {
    private double x,y;
    private String tweetID;
    private long timestamp;
    private int clusterID = -1;
    private List<DataPoint> neighbors = null;


    public DataPointImpl(BasicDBObject basicObject){
        this.tweetID = basicObject.getString("tweet_id");
        String tmp = basicObject.get("coordinates").toString();
        tmp = tmp.substring(2, tmp.length() - 1);
        String[] coordinates = tmp.split(",");
        this.x = Double.parseDouble(coordinates[0]);
        this.y = Double.parseDouble(coordinates[1]);
        this.timestamp = basicObject.getLong("timestamp");
    }

    public double distance(DataPoint datapoint) {
        double dx = this.x - datapoint.getX();
        double dy = this.y - datapoint.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceWithTime(DataPoint datapoint) {
        double dx = this.x - datapoint.getX();
        double dy = this.y - datapoint.getY();
        double dt = this.timestamp - datapoint.getTimestamp();
        return Math.sqrt(dx * dx + dy * dy + dt * dt);
    }

    public double distance(double x, double y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void setCluster(int id) {
        this.clusterID = id;
    }

    public int getCluster() {
        return clusterID;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getID() {
        return tweetID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<DataPoint> getNeighbors() {
        return neighbors;
    }

    public int getNeighborsSize() {
        return (neighbors != null) ? neighbors.size() : -1;
    }

    public void setNeighbors(List<DataPoint> neighbors) {
        this.neighbors = neighbors;
    }
}
