package org.trajectory.clustering;

import com.vividsolutions.jts.awt.PointShapeFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;
import java.util.Collection;

public class TrajectoryPoint implements Comparable<TrajectoryPoint> {
    private double x,y;
    private long tweetID;
    private long timestamp;
    private ArrayList<Line> connections = null; // in order to calculate shortest path between 2 points
    private ArrayList<Line> shortestPath;
    private boolean visited = false;
    private double distanceToTarget;

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

    public ArrayList<Line> getConnections() {
        return connections;
    }

    public void addConnection(Line connection) {
        if (connections == null) {
            connections = new ArrayList<Line>();
        }
        connections.add(connection);
    }

    public void removeConnections() {
        connections = null;
    }

    public ArrayList<Line> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(ArrayList<Line> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Point getJTSpoint() {
        Coordinate userCoord = new Coordinate(x, y);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point JTSpoint = geometryFactory.createPoint(userCoord);
        return JTSpoint;
    }

    /**
     * compare 2 points based on their distance to target
     * @param point
     * @return
     */
    public int compareTo(TrajectoryPoint point) {
        return distanceToTarget < point.getDistanceToTarget() ? -1 :
                distanceToTarget > point.getDistanceToTarget() ? 1 : 0;
    }

    public double getDistanceToTarget() {
        return distanceToTarget;
    }

    public void setDistanceToTarget(double distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }

    public Coordinate getCoordinate() {
        return new Coordinate(x, y);
    }

    public double getSquared() {
        return (Math.pow(x, 2) + Math.pow(y, 2));
    }
}
