package org.trajectory.clustering;

import java.util.ArrayList;

/**
 * based on INCREMENTAL CLUSTERING FOR TRAJECTORIES thesis report
 */
public class TCMM {

    public static ArrayList<TCMMmicroCluster> clusterTrajectories(ArrayList<Line> set, double d) {
        ArrayList<TCMMmicroCluster> microClusters = new ArrayList<TCMMmicroCluster>();
        int clusterID = 0;

        for (Line line: set) {
            if (!microClusters.isEmpty()) {
                int closestMicroClusterID = findClosestMicroCluster(line, microClusters);
                TCMMmicroCluster closestCluster = microClusters.get(closestMicroClusterID);

                double distance = getTCMMDistance(line, closestCluster);

                if (distance <= d) {
                    //Add the line to identified micro cluster
                    closestCluster.addLine(line);
                }else {
                    //Create a new cluster and add the line
                    createNewMicroCluster(line, microClusters, clusterID);
                    ++clusterID;
                }
            } else {
                //Create a new cluster and add the line
                createNewMicroCluster(line, microClusters, clusterID);
                ++clusterID;
            }

        }

        return microClusters;
    }

    private static TCMMmicroCluster createNewMicroCluster(Line line, ArrayList<TCMMmicroCluster> microClusters, int clusterID) {
        TCMMmicroCluster newCluster = new TCMMmicroCluster(line, clusterID);
        microClusters.add(clusterID, newCluster);
        return newCluster;
    }

    private static int findClosestMicroCluster(Line line, ArrayList<TCMMmicroCluster> microClusters) {
        //TODO implement logic
        int clusterID = -1;
        double minDistance = Double.MAX_VALUE;
        for (TCMMmicroCluster MC: microClusters) {
            double distance = getTCMMDistance(line, MC);
            if (distance < minDistance) {
                minDistance = distance;
                clusterID = MC.getID();
            }
        }
        return clusterID;
    }

    // e = 2.8, minLn = 2 good for TRACLUST distance function
    private static double getTCMMDistance(LineInterface line, LineInterface inspectingLine) {
        LineInterface shorter, longer;
        TrajectoryPoint[] shorterEndpoints;
        TrajectoryPoint cShort, cLong;
        double w1 = 1, w2 = 1, w3 = 1;

        //To make getDistance method symmetric
        //differentiate the order of parameters based on inherent property of line
        //In this case length of the line is selected as differentiating property
        if (line.getLength() < inspectingLine.getLength()) {
            shorter = line;
            longer = inspectingLine;
        } else {
            shorter = inspectingLine;
            longer = line;
        }

        /*shorterEndpoints = shorter.getEndPoints();

        double l1,l2; //to hold parallel distances from shorter line to longer line
        //pictorial explanation in Figure 5
        l1 = getDistance(shorterEndpoints[0], longer);
        l2 = getDistance(shorterEndpoints[1], longer);*/



        cShort = shorter.getCenterPoint();
        cLong = longer.getCenterPoint();

        double centerPointDistance = getDistance(cShort, cLong);
        //TODO this is my parallel distance function, need to implement the correct one
        double parallelDistance = Math.abs(longer.getLength() * Math.cos(longer.getTheta()) -
                shorter.getLength() * Math.cos(shorter.getTheta())) / 2;
        double angleDistance = shorter.getLength() * Math.sin(Math.abs(shorter.getTheta() - longer.getTheta()));

        double distance = w1 * centerPointDistance + w2 * parallelDistance + w3 * angleDistance;
        return distance;
    }

    private static double getDistance(TrajectoryPoint point, Line line) {
        double a, b, c; //to hold characteristics for longer line

        a = line.getA();
        b = line.getB();
        c = line.getC();

        //equation taken from
        // http://www.intmath.com/plane-analytic-geometry/perpendicular-distance-point-line.php
        return Math.abs(a * point.getX() + b * point.getY() + c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    private static double getDistance(TrajectoryPoint point, TrajectoryPoint point1) {
        //return euclidean distance
        double a = point.getX() - point1.getX(),
                b = point.getY() - point1.getY();

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    private static boolean isShortIntersectLong(Line longer, Line shorter) {
        TrajectoryPoint[] shorterEndpoints = shorter.getEndPoints();
        return isPointClockwiseFromLine(shorterEndpoints[0], longer)
                != isPointClockwiseFromLine(shorterEndpoints[1], longer);
    }

    public static boolean isPointClockwiseFromLine(TrajectoryPoint point, Line line) {
        double crossProduct = ((line.getEndPoints()[0].getX() - point.getX())
                * (line.getEndPoints()[1].getY() - point.getY()))
                - ((line.getEndPoints()[1].getX() - point.getX())
                * (line.getEndPoints()[0].getY() - point.getY()));

        return (crossProduct >= 0);
    }
}
