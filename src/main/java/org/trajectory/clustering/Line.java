package org.trajectory.clustering;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class Line implements LineInterface, Comparable<Line> {
    private final int ID;
    private TrajectoryPoint[] endPoints = new TrajectoryPoint[2];
    private long timeDiff;
    private double length, a, b, c, c1, weight, orthodromicDistance;
    private boolean visited = false;
    private int flipCount;
    private int usageCount = 0;
    private int clusterID;
    private double theta, directionalTheta; // holds the angle between x axis and line
    private TrajectoryPoint centerPoint;
    private int[] adjacentNeighbours = new int[2];
    private int numOfNeighbours = 0;

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

        //line equation
        //ax + by + c = 0
        a = point1.getY() - point2.getY();
        b = point2.getX() - point1.getX();
        c = -b * point1.getY() + -a * point1.getX();

        double x = (endPoints[0].getX() + endPoints[1].getX()) / 2,
                y = (endPoints[0].getY() + endPoints[1].getY()) / 2;
        c1 = a * y - b * x;
        //perpendicular equation
        //bx - ay + c1 = 0
        flipCount = 0;
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

    public double getCPerpendicular() {
        return c1;
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

    /**
     * compare 2 lines based on their distances
     * @param line
     * @return
     */
    public int compareTo(Line line) {
        return orthodromicDistance < line.getOrthodromicDistance() ? -1 :
                orthodromicDistance > line.getOrthodromicDistance() ? 1 : 0;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addConnection() {
        endPoints[0].addConnection(this);
        endPoints[1].addConnection(this);
    }

    public void removeConnections() {
        endPoints[0].removeConnections();
        endPoints[1].removeConnections();
    }

    public void clearEndPointShortestPaths() {
        endPoints[0].setShortestPath(null);
        endPoints[0].setVisited(false);
        endPoints[1].setShortestPath(null);
        endPoints[1].setVisited(false);
    }

    public double getOrthodromicDistance() {
        return orthodromicDistance;
    }

    public void setOrthodromicDistance(double orthodromicDistance) {
        this.orthodromicDistance = orthodromicDistance;
    }

    public Coordinate getCenterPointCoordinate() {
        return new Coordinate(centerPoint.getX(), centerPoint.getY());
    }

    public void addNeighbour(int neighbourID) {
        adjacentNeighbours[numOfNeighbours] = neighbourID;
        ++numOfNeighbours;
    }

    public int[] getAdjacentNeighbours() {
        return adjacentNeighbours;
    }

    public int getNumOfNeighbours() {
        return numOfNeighbours;
    }

    public boolean replaceAdjacentNeighbour(int oldVal, int newVal) {
        if (adjacentNeighbours[0] == oldVal) {
            adjacentNeighbours[0] = newVal;
            return true;
        } else if (adjacentNeighbours[1] == oldVal) {
            adjacentNeighbours[1] = newVal;
            return true;
        }
        return false;
    }

    public void setOrthodromicDistance(CoordinateReferenceSystem sourceCRS) throws TransformException {
        this.orthodromicDistance = JTS.orthodromicDistance(endPoints[0].getCoordinate(),
                endPoints[1].getCoordinate(), sourceCRS) * 1000;
    }

    public void setFlipCount(Line previousLine) {
        this.flipCount = previousLine.getFlipCount() + 1;
    }

    public int getFlipCount() {
        return flipCount;
    }

    @Override
    public String toString() {
        return endPoints[0] + "->" + endPoints[1];
    }

    public void incrementUsageCount() {
        ++this.usageCount;
    }

    public void resetUsageCount() {
        this.usageCount = 0;
    }

    public int getUsageCount() {
        return usageCount;
    }
}
