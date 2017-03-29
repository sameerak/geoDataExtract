package org.trajectory.clustering;


import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;

public class TCMMmicroCluster implements LineInterface {
    //private Line representation; //representative line for micro cluster
    private int ID;
    private int N; //number of lines
    private double theta; // holds the angle between x axis and line
    private double length;
    private TrajectoryPoint centerPoint;
    private ArrayList<Integer> lineIDs = new ArrayList<Integer>();

    public TCMMmicroCluster (Line line, int ID) {
        this.ID = ID;
        this.length = line.getLength();
        this.theta = line.getTheta();
        this.centerPoint = line.getCenterPoint();
        N = 1;
        lineIDs.add(line.getID());
    }

    public void addLine(Line line) {
        length = getNewAverage(line.getLength(), length, N);
        double x = getNewAverage(line.getCenterPoint().getX(), centerPoint.getX(), N);
        double y = getNewAverage(line.getCenterPoint().getY(), centerPoint.getY(), N);
        centerPoint = new TrajectoryPoint(x, y,1, 1);
        theta = getNewAverage(line.getTheta(), theta, N);
        lineIDs.add(line.getID());
    }

    private double getNewAverage(double newVal, double oldVal, int n) {
        double val = ((n * oldVal) + newVal) / (n + 1);
        return val;
    }

    public double getTheta() {
        return theta;
    }

    public double getLength() {
        return length;
    }

    public TrajectoryPoint getCenterPoint() {
        return centerPoint;
    }

    public Coordinate[] getCoordinates() {
        double x1, y1, x2, y2;

        x1 = centerPoint.getX() + (length / 2) * Math.sin(theta);
        x2 = centerPoint.getX() - (length / 2) * Math.sin(theta);
        y1 = centerPoint.getY() + (length / 2) * Math.cos(theta);
        y2 = centerPoint.getY() - (length / 2) * Math.cos(theta);

        Coordinate[] coords =  {new Coordinate(x1, y1), new Coordinate(x2, y2)};
        return coords;
    }

    public int getID() {
        return ID;
    }

    public ArrayList<Integer> getLineIDs() {
        return lineIDs;
    }
}
