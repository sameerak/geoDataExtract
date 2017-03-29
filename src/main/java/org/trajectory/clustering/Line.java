package org.trajectory.clustering;

import com.vividsolutions.jts.geom.Coordinate;

public class Line implements LineInterface {
    private final int ID;
    private TrajectoryPoint[] endPoints = new TrajectoryPoint[2];
    private long timeDiff;
    private double length, a, b, c;
    private boolean visited = false;
    private int clusterID;
    private double theta, directionalTheta; // holds the angle between x axis and line
    private TrajectoryPoint centerPoint;

    public Line(int ID, TrajectoryPoint point1, TrajectoryPoint point2){
        endPoints[0] = point1;
        endPoints[1] = point2;
        this.ID = ID;

        Init(point1, point2);
    }

    private void Init(TrajectoryPoint point1, TrajectoryPoint point2) {
        setMidPoint();
        //calculate length
        double dx = point1.getX() - point2.getX();
        double dy = point1.getY() - point2.getY();
        //TODO need to make length over sphere
        length = Math.sqrt(dx * dx + dy * dy);

        theta = Math.atan2(dx, dy);
        directionalTheta = theta;

        if (theta < 0) {
            theta = -1 * theta;
            theta = Math.PI - theta;
        }

        //calculate time diff
        timeDiff = Math.abs(point1.getTimestamp() - point2.getTimestamp());

        a = point1.getY() - point2.getY();
        b = point1.getX() - point2.getX();
        c = (point2.getX() - point1.getX()) * point1.getY() + (point2.getY() - point1.getY()) * point1.getX();
    }

    public TrajectoryPoint[] getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(TrajectoryPoint[] endPoints) {
        this.endPoints = endPoints;

        Init(endPoints[0], endPoints[1]);
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public double getLength() {
        return length;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public void setVisited() {
        this.visited = true;
    }

    public void resetVisited() {
        this.visited = false;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public int getClusterID() {
        return clusterID;
    }

    public boolean isVisited() {
        return visited;
    }

    public int getID() {
        return ID;
    }

    public Coordinate[] getCoordinates() {
        Coordinate[] coords =  {new Coordinate(endPoints[0].getX(), endPoints[0].getY()),
                                new Coordinate(endPoints[1].getX(), endPoints[1].getY())};
        return coords;
    }

    public TrajectoryPoint getCenterPoint() {
        return centerPoint;
    }

    /**
     * from http://stackoverflow.com/questions/4656802/midpoint-between-two-latitude-and-longitude
     */
    private void setMidPoint(){

        double dLon = Math.toRadians(endPoints[1].getX() - endPoints[0].getX());

        //convert to radians
        double lat1 = Math.toRadians(endPoints[0].getY());
        double lat2 = Math.toRadians(endPoints[1].getY());
        double lon1 = Math.toRadians(endPoints[0].getX());

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        long timestamp = (endPoints[0].getTimestamp() + endPoints[1].getTimestamp()) / 2;

        centerPoint = new TrajectoryPoint(Math.toDegrees(lon3), Math.toDegrees(lat3), 1, timestamp);
    }

    public double getTheta() {
        return theta;
    }

    public double getDirectionalTheta() {
        return directionalTheta;
    }
}
