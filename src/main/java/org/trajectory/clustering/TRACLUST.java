package org.trajectory.clustering;

import com.vividsolutions.jts.geom.Coordinate;
import jdk.nashorn.internal.ir.WhileNode;
import sun.rmi.runtime.Log;

import java.util.ArrayList;

/**
 * Developed based on the trajectory clustering algorithm presented in paper
 * "Trajectory clustering: A Partition-and-Group Framework"
 */
public class TRACLUST {

    public static ArrayList<ArrayList<Integer>> clusterTrajectories(ArrayList<Line> set, double e, double sineTheta, int minLn) {
        int clusterId = 0;
        //implemented inspectionQueue as a FIFO queue
        ArrayList<Line> inspectionQueue = new ArrayList<Line>();
        for (Line line: set) {

            if (line.isVisited()) {
                continue;
            }

            //calculate number of lines within the e neighbourhood
            ArrayList<Line> neighbours = inspectNeighbourhood(line, set, e, sineTheta);
            line.setVisited();
            //INFO currently visited means neighbourhood calculated
            //might need a way to associate neighbours with line

            if (neighbours.size() >= minLn) {
                line.setClusterID(clusterId);
                inspectionQueue.addAll(neighbours);
                expandCluster(inspectionQueue, clusterId, set, e, sineTheta, minLn);
                ++clusterId;
            }
            else {
                // mark line as noise
                line.setClusterID(-1);
            }

        }

        //creating cluster trajectory sets
        ArrayList<ArrayList<Integer>> clusterSet = new ArrayList<ArrayList<Integer>>();

        for (int i = 0; i <= clusterId ; i++) {
            clusterSet.add(new ArrayList<Integer>());
        }

        for (Line line: set) {
            if (line.getClusterID() == -1) {
                clusterSet.get(clusterId).add(line.getID());
                continue;
            }

            clusterSet.get(line.getClusterID()).add(line.getID());
        }

        return clusterSet;
    }

    private static void expandCluster(ArrayList<Line> inspectionQueue, int clusterId,
                                      ArrayList<Line> set, double e, double sineTheta, int minLn) {
        while (!inspectionQueue.isEmpty()) {
            Line line = inspectionQueue.get(0);
            ArrayList<Line> neighbours = inspectNeighbourhood(line, set, e);
//            ArrayList<Line> neighbours = inspectNeighbourhood(line, set, e, sineTheta);
            line.setVisited();
            //INFO currently visited means neighbourhood calculated
            //might need a way to associate neighbours with line

            if (neighbours.size() >= minLn) {
                for (Line neighbour :
                        neighbours) {
                    //if not visited or noise, set the clusterID to expanding cluster
                    if (!neighbour.isVisited() || neighbour.getClusterID() == -1) {
                        neighbour.setClusterID(clusterId);
                    }

                    //if this line is not visited, add it to inspection Queue
                    if (!neighbour.isVisited()) {
                        inspectionQueue.add(neighbour);
                    }
                }
            }
            else { //INFO this part was not presented in the paper, I added it for completeness
                line.setClusterID(-1);
            }

            //TODO not sure whether removing 0 element, sets index-1 element to 0 need to check
            inspectionQueue.remove(line);
        }
    }

    //neighbourhood inspection algorithm
    //returned neighbour set does not contain origin line (origin line is checked and removed)
    private static ArrayList<Line> inspectNeighbourhood(Line line, ArrayList<Line> set, double e) {
        ArrayList<Line> neighbourhood = new ArrayList<Line>();
        for (Line inspectingLine: set) {
            if (line.getID() == inspectingLine.getID()) {
                continue;
            }

            double distance = getDistance(line, inspectingLine);

            if (distance <= e) {
                neighbourhood.add(inspectingLine);
            }
        }

        return neighbourhood;
    }

    private static ArrayList<Line> inspectNeighbourhood(Line line, ArrayList<Line> set, double e, double sineTheta) {
        ArrayList<Line> neighbourhood = new ArrayList<Line>();
        for (Line inspectingLine: set) {
            if (line.getID() == inspectingLine.getID()) {
                continue;
            }

//            double distance = getDistance(line, inspectingLine);
            comparator compare = new comparator(line, inspectingLine);
            double distance = compare.getInsideDistance();
            double sine = compare.getComparatorSine();

            if (distance <= e && sine <= sineTheta){
                neighbourhood.add(inspectingLine);
            }
        }

        return neighbourhood;
    }

    // e = 2.8, minLn = 2 good for TRACLUST distance function
    private static double getDistance(Line line, Line inspectingLine) {
        Line shorter, longer;
        TrajectoryPoint[] shorterEndpoints;
        double w1 = 1, w2 = 1, w3 = 1;
        TrajectoryPoint cShort, cLong;

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

        shorterEndpoints = shorter.getEndPoints();

        double l1,l2; //to hold perpendicular distances from shorter line to longer line
        //pictorial explanation in Figure 5

        l1 = getDistance(shorterEndpoints[0], longer);
        l2 = getDistance(shorterEndpoints[1], longer);

        cShort = shorter.getCenterPoint();
        cLong = longer.getCenterPoint();

        double centerPointDistance = getDistance(cShort, cLong);
        double perpendicularDistance = (Math.pow(l1, 2) + Math.pow(l2, 2))/(l1 + l2);
        //TODO this is my parallel distance function, need to implement the correct one
        double parallelDistance = Math.abs(longer.getLength() * Math.cos(longer.getTheta()) -
                shorter.getLength() * Math.cos(shorter.getTheta())) / 2;
        double angleDistance = Math.abs(l1 - l2);

//        double distance = w1 * perpendicularDistance + w2 * parallelDistance + w3 * angleDistance;
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

    public static ArrayList<Line> partitionTrajectories(int ID, ArrayList<TrajectoryPoint> userPoints) {
        int pointIndex = 0, lineID = ID;
        ArrayList<Line> partitionedLines = new ArrayList<Line>();
        Line line_prev, line_next;

        if (userPoints.size() == 2) {
            line_next = new Line(lineID, userPoints.get(pointIndex), userPoints.get(pointIndex + 1));
            partitionedLines.add(line_next);
            return partitionedLines;
        }

        for (int i = 1; i < userPoints.size() - 1; i++) {
            line_next = new Line(lineID, userPoints.get(pointIndex), userPoints.get(i + 1));

            double perpendicularDistance = 0, angleDistance = 0;

            for (int j = pointIndex; j < i + 1; j++) {// j varies from point index to i
                //we calculate distances for lines from point index to j + 1
                line_prev = new Line(lineID, userPoints.get(j), userPoints.get(j + 1));
                MDLComparator mdlComparator = new MDLComparator(line_prev, line_next);

                perpendicularDistance += mdlComparator.getPerpendicularDistance();
                angleDistance += mdlComparator.getAngleDistance();
            }

//            double LH = Math.log(getDistance(userPoints.get(pointIndex), userPoints.get(i + 1))) / Math.log(2);
//            double LDH = Math.log(perpendicularDistance) / Math.log(2) + Math.log(angleDistance) / Math.log(2);
//
//            //MDLnopar = LH
//            double MDL = LH + LDH;
//
//            if (MDL > LH) {

            double LH = getDistance(userPoints.get(pointIndex), userPoints.get(i + 1));
            double LDH = perpendicularDistance + angleDistance;

            if (LDH > LH) {
                //partition at i
                partitionedLines.add(new Line(lineID, userPoints.get(pointIndex), userPoints.get(i)));
                ++lineID;
                pointIndex = i;
            }
        }

        if (pointIndex != userPoints.size() - 1){
            partitionedLines.add(new Line(lineID, userPoints.get(pointIndex), userPoints.get(userPoints.size() - 1)));
            ++lineID;
        }

        return partitionedLines;
    }

    public static ArrayList<Line> getLines(int ID, ArrayList<TrajectoryPoint> userPoints) {
        int lineID = ID;
        ArrayList<Line> Lines = new ArrayList<Line>();

        for (int i = 0; i < userPoints.size() - 1; i++) {
            Lines.add(new Line(lineID, userPoints.get(i), userPoints.get(i + 1)));
            ++lineID;
        }

        return Lines;
    }

    public static class MDLComparator {
        Line shorter, longer;
        TrajectoryPoint[] longerEndpoints, shorterEndpoints;
        double l1,l2;

        MDLComparator(Line line, Line inspectingLine) {
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
            longerEndpoints = longer.getEndPoints();
            shorterEndpoints = shorter.getEndPoints();

            l1 = getDistance(shorterEndpoints[0], longer);
            l2 = getDistance(shorterEndpoints[1], longer);
        }

        public double getAngleDistance() {
            double angleDiff = Math.abs(shorter.getDirectionalTheta() - longer.getDirectionalTheta());
            if (0 <= angleDiff && angleDiff < Math.PI / 2) {
                return shorter.getLength() *
                        Math.sin(angleDiff);
            } else {
                return shorter.getLength();
            }
        }

        public double getPerpendicularDistance() {
            return (Math.pow(l1, 2) + Math.pow(l2, 2))/(l1 + l2);
        }
    }

    public static class comparator {
        Line shorter, longer;
        TrajectoryPoint[] longerEndpoints, shorterEndpoints;
        double lenlon1sho1,lenlon2sho1,lenlon1sho2,lenlon2sho2,
                l1,l2; //to hold perpendicular distances from shorter line to longer line
        //pictorial explanation in Figure 5

        comparator(Line line, Line inspectingLine) {
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

            longerEndpoints = longer.getEndPoints();
            shorterEndpoints = shorter.getEndPoints();

            l1 = getDistance(shorterEndpoints[0], longer);
            l2 = getDistance(shorterEndpoints[1], longer);

            lenlon1sho1 = getDistance(longerEndpoints[0], shorterEndpoints[0]);
            lenlon1sho2 = getDistance(longerEndpoints[0], shorterEndpoints[1]);
            lenlon2sho1 = getDistance(longerEndpoints[1], shorterEndpoints[0]);
            lenlon2sho2 = getDistance(longerEndpoints[1], shorterEndpoints[1]);
        }

        public double getComparatorDistance() {
            double min;

            //first set min to be one distance
            min = lenlon1sho1;

            //get next distance and set up min correctly
            min = Math.min(min, lenlon1sho2);

            //Do the same min setting up calculation for next two distances
            min = Math.min(min, lenlon2sho1);

            min = Math.min(min, lenlon2sho2);

            return min;
        }

        public double getComparatorSine() {

            double perpendicularDistance;

            if (isShortIntersectLong(longer, shorter)) {
                perpendicularDistance = l1 + l2;
            } else {
                perpendicularDistance = Math.abs(l1 - l2);
            }

            double sine = perpendicularDistance / shorter.getLength();

            return sine;
        }

        public double getInsideDistance(){
            boolean isShort1inside = isLongerLongest(lenlon1sho1,lenlon2sho1);
            boolean isShort2inside = isLongerLongest(lenlon1sho2,lenlon2sho2);

            if (isShort1inside && isShort2inside) {
                return Math.min(l1, l2);
            } else if (isShort1inside) {
                return l1;
            } else if (isShort2inside) {
                return l2;
            } else {
                return getComparatorDistance();
            }
        }

        private boolean isLongerLongest(double len1, double len2) {
            double longest = longer.getLength();

            if (len1 > longest || len2 > longest) {
                return false;
            }
            return true;
        }

    }

    //TODO reinspect this method and remove as this is replaced by comparator class
    // e = 0.0001, minLn = 2 good for getMinDistance function
    private static double getMinDistance(Line line, Line inspectingLine) {
        Line shorter, longer;
        double w1 = 1, w2 = 10;

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

        TrajectoryPoint[] longerEndpoints, shorterEndpoints;
        longerEndpoints = longer.getEndPoints();
        shorterEndpoints = shorter.getEndPoints();

        double min, compare;

        //first set min to be one distance
        min = getDistance(longerEndpoints[0], shorterEndpoints[0]);

        //get next distance and set up min correctly
        compare = getDistance(longerEndpoints[0], shorterEndpoints[1]);
        min = Math.min(min, compare);

        //Do the same min setting up calculation for next two distances
        compare = getDistance(longerEndpoints[1], shorterEndpoints[0]);
        min = Math.min(min, compare);

        compare = getDistance(longerEndpoints[1], shorterEndpoints[1]);
        min = Math.min(min, compare);

        double l1,l2; //to hold perpendicular distances from shorter line to longer line
        //pictorial explanation in Figure 5

        l1 = getDistance(shorterEndpoints[0], longer);
        l2 = getDistance(shorterEndpoints[1], longer);

        double perpendicularDistance;

        if (isShortIntersectLong(longer, shorter)) {
            perpendicularDistance = l1 + l2;
        } else {
            perpendicularDistance = Math.abs(l1 - l2);
        }

        double sine = perpendicularDistance / shorter.getLength();

        double distance = w1 * min + w2 * perpendicularDistance;
        return distance;
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

    private static boolean isPointClockwiseFromLine(TrajectoryPoint point, Line line) {
        double crossProduct = ((line.getEndPoints()[0].getX() - point.getX())
                * (line.getEndPoints()[1].getY() - point.getY()))
                - ((line.getEndPoints()[1].getX() - point.getX())
                * (line.getEndPoints()[0].getY() - point.getY()));

        return (crossProduct >= 0);
    }
}
