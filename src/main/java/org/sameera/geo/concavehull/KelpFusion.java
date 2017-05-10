package org.sameera.geo.concavehull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.trajectory.clustering.Line;
import org.trajectory.clustering.TrajectoryPoint;

import java.util.*;

import static org.trajectory.clustering.TCMM.isPointClockwiseFromLine;

/**
 * from paper - KelpFusion: A hybrid set visualization technique
 */
public class KelpFusion {
//    public static CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
    private PreparedGeometryIndex preparedGeometryIndex;
    GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
    ArrayList<Line> G = null;
    ArrayList<Line> SPG = null;
    private String area;
    private double previousT;
    CoordinateReferenceSystem sourceCRS;
    public HashMap<String, Line> lineSet = new HashMap<String, Line>();
    public ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
    private ArrayList<TrajectoryPoint> pointSet;
    HashMap<Coordinate, TrajectoryPoint> selectedLocations;

    public KelpFusion(String area,
                      CoordinateReferenceSystem sourceCRS) {
        this.area = area;
        this.sourceCRS = sourceCRS;
    }

    public ArrayList<Line> GetShortestPathGraph(ArrayList<TrajectoryPoint> pointSet, double t) throws TransformException {
        //Note: lines in G should be sorted in length ascending order
        System.out.println("started SPG calculation !!!!!!!!!!!!!!");
        if (G == null) {
//            G = createReachabilityGraph(pointSet); // this is suggested by original paper , Cost = O(n^2)
            // as creation of reachability graph takes long time for tweet data
            // We are using Delaunay Triangulation  , Cost = O(n*lg(n))
            G = createDelaunayGraph(pointSet);
        }
        System.out.println("G calculation FINISHED !!!!!!!!!!!!!! G.size = " + G.size());

        //if there is a already created SPG
        if (SPG != null) {
            if (previousT == t) {
                return SPG; //return it
            } else {
                for (Line gLine: SPG) {
                    gLine.removeConnections();
                }
            }
        }

        previousT = t;
        SPG = new ArrayList<Line>();

        //implements shortest path graph creation from KelpFusion paper
        int progress = 0,added = 0;
        for (Line gLine: G) {
            ArrayList<Line> shortestPathFromSPG = getShortestPath(gLine, SPG, true); //uses dijkstra's algorithm
//            ArrayList<Line> shortestPathFromSPG = getShortestPathAStar(gLine, SPG, t); //uses modified A* algo
            Coordinate[] endpoints = gLine.getCoordinates();
//            double length = JTS.orthodromicDistance(endpoints[0], endpoints[1], sourceCRS) * 1000;
//            double length = gLine.getLength(); //with this value all the lengths become less than 1
            double length = gLine.getOrthodromicDistance();
            if (shortestPathFromSPG == null || getPathWeight(shortestPathFromSPG, true) >= Math.pow(length, t)) {
                gLine.setWeight(Math.pow(length, t));
                gLine.addConnection();
                SPG.add(gLine);
                ++added;
            }
            ++progress;
//            System.out.println("########### Progress = " + progress + "/" + G.size() + ", added = " + added);
        }
        System.out.println("########### Progress = " + progress + "/" + G.size() + ", added = " + added);
        System.out.println("SPG calculation FINISHED !!!!!!!!!!!!!!");

        return SPG;
    }

    public ArrayList<Line> GetShortestPathGraphViaMST(ArrayList<TrajectoryPoint> pointSet, double t) throws TransformException {
        //create delaunay graph
        System.out.println("started SPG calculation Via MST !!!!!!!!!!!!!!");
        if (G == null) {
//            G = createReachabilityGraph(pointSet); // this is suggested by original paper , Cost = O(n^2)
            // as creation of reachability graph takes long time for tweet data
            // We are using Delaunay Triangulation  , Cost = O(n*lg(n))
            G = createDelaunayGraph(pointSet);
        }
        System.out.println("G calculation FINISHED !!!!!!!!!!!!!! G.size = " + G.size());

        //if there is a already created SPG
        if (SPG != null) {
            if (previousT == t) {
                return SPG; //return it
            } else {
                for (Line gLine: SPG) {
                    gLine.removeConnections();
                }
            }
        }

        previousT = t;
        SPG = new ArrayList<Line>();
        ArrayList<Line> notInMST = new ArrayList<Line>();

        setOperator setOperator = new setOperator();

        System.out.println("MST creation started --------------");

        //create MST
        int progress = 0,added = 0,SetID = 0;
        TrajectoryPoint[] endPoints;
        for (Line gLine: G) {
            endPoints = gLine.getEndPoints();

            //if selected endpoints are not connected at all
            if (endPoints[0].getSetID() == -1 && endPoints[1].getSetID() == -1){
                //then add this line to SPG
                gLine.setWeight(Math.pow(gLine.getOrthodromicDistance(), t));
                gLine.addConnection();
                SPG.add(gLine);
                ++added;
                //Create new set with these 2 points
                HashSet<TrajectoryPoint> newSet = new HashSet<TrajectoryPoint>();
                newSet.add(endPoints[0]);
                newSet.add(endPoints[1]);
                setOperator.addSet(SetID, newSet);
                endPoints[0].setSetID(SetID);
                endPoints[1].setSetID(SetID);
                ++SetID;
            } //else if one of the selected points are connected
            else if (endPoints[0].getSetID() == -1 || endPoints[1].getSetID() == -1){
                //then add this line to SPG
                gLine.setWeight(Math.pow(gLine.getOrthodromicDistance(), t));
                gLine.addConnection();
                SPG.add(gLine);
                ++added;
                //add the not connected point to connected set
                TrajectoryPoint connectedPoint = (endPoints[0].getSetID() == -1) ? endPoints[1] : endPoints[0],
                notConnectedPoint = (endPoints[0].getSetID() == -1) ? endPoints[0] : endPoints[1];

                setOperator.getSet(connectedPoint.getSetID()).add(notConnectedPoint);
                notConnectedPoint.setSetID(connectedPoint.getSetID());
            } //else if both points are connected to sets we need to check intersection.
            else if ((endPoints[0].getSetID() != endPoints[1].getSetID()) && !setOperator.isIntersect(endPoints[0].getSetID(),endPoints[1].getSetID())) {
                //then add this line to SPG
                gLine.setWeight(Math.pow(gLine.getOrthodromicDistance(), t));
                gLine.addConnection();
                SPG.add(gLine);
                ++added;
                //Union the two point sets
                setOperator.makeUnion(endPoints[0].getSetID(),endPoints[1].getSetID());
            } else {
                notInMST.add(gLine);
            }
            ++progress;
        }

        System.out.println("########### Progress = " + progress + "/" + G.size() + ", added = " + added);
        System.out.println("MST calculation FINISHED ---------------");

        //creating spatial index
        preparedGeometryIndex = new PreparedGeometryIndex();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        //add all notInMST edges to spatial index
        for (Line gLine: notInMST) {
            //create line geometries
            Geometry lineGeometry = geometryFactory.createLineString(gLine.getCoordinates());
            //set them to line objects and put them in index
            gLine.setGeometry(lineGeometry);
            preparedGeometryIndex.insert(lineGeometry);
            //for all geometries set their user data as adding
            lineGeometry.setUserData(true);
        }

        //create SPG
        System.out.println("SPG creation started --------------");
        progress = 0;
        added = 0;

        for (Line gLine: notInMST) {
            ++progress;
            Geometry gabrielNeighbourhood = createCircle(gLine.getCenterPointCoordinate(), gLine.getLength());
//            List gabrielNeighbourhood = preparedGeometryIndex.intersects(centerCircle);
            //if there is a edge with in the gabriel neighbourhood of gline which is not added
            if (preparedGeometryIndex.isThereNotAddedGeometryIntersecting(gabrielNeighbourhood)) { //need to write a method to check if there is a edge not added
                //then set gline user data to not added
                gLine.getGeometry().setUserData(false);
                //skip remaining operations and move on to next line
                continue;
            }
            //else
            ArrayList<Line> shortestPathFromSPG = getShortestPath(gLine, SPG, true); //uses dijkstra's algorithm
//            ArrayList<Line> shortestPathFromSPG = getShortestPathAStar(gLine, SPG, t); //uses modified A* algo
            double length = gLine.getOrthodromicDistance();
            if (shortestPathFromSPG == null || getPathWeight(shortestPathFromSPG, true) >= Math.pow(length, t)) {
                gLine.setWeight(Math.pow(length, t));
                gLine.addConnection();
                SPG.add(gLine);
                ++added;
            } else {
                //then set gline user data to not added
                gLine.getGeometry().setUserData(false);
            }
        }
        System.out.println("########### Progress = " + progress + "/" + notInMST.size() + ", added = " + added);
        System.out.println("SPG calculation FINISHED !!!!!!!!!!!!!!");

        return SPG;
    }



    /**
     *This method uses dijkstra's algorithm to find the shortest path between endpoints of the line
     *
     * @param gLine provides 2 end points to calculate shortest path in between
     * @param spg
     * @return shortest path calculated with lines contained in spg
     */
    private ArrayList<Line> getShortestPath(Line gLine, ArrayList<Line> spg, boolean useWeights) throws TransformException {
        TrajectoryPoint[] endPoints = gLine.getEndPoints();

        if (endPoints[0].getConnections() == null || endPoints[1].getConnections() == null) {
            return null;
        }

        //make all previous shortest path calculations null
        for (Line line : spg) {
            line.clearEndPointShortestPaths();
        }

        ArrayList<TrajectoryPoint> pointQueue = new ArrayList<TrajectoryPoint>();
        HashSet<Long> points_to_visit = new HashSet<Long>();

        endPoints[0].setDistanceToTarget(gLine.getOrthodromicDistance());
        endPoints[0].setShortestPath(new ArrayList<Line>());
        pointQueue.add(endPoints[0]);
        points_to_visit.add(endPoints[0].getTweetID());

        while (!pointQueue.isEmpty()) {
            Collections.sort(pointQueue);

            TrajectoryPoint point = pointQueue.get(0);
            pointQueue.remove(0);
            point.setVisited(true);
            points_to_visit.remove(point.getTweetID());
            for (Line line : point.getConnections()) {

                //if considering point is the target
                if (point.getTweetID() == endPoints[1].getTweetID()){/*and all the edges connected to endpoint[1] are covered*/
                    return point.getShortestPath();
                }

                //check and select the other node not the one we originated from
                TrajectoryPoint[] tmpEndPoints = line.getEndPoints();
                TrajectoryPoint otherEndPoint = tmpEndPoints[0];
                if (otherEndPoint.getTweetID() == point.getTweetID()){
                    otherEndPoint = tmpEndPoints[1];
                }

                //shortest path = point.getshortestpath + this line
                ArrayList<Line> tmpShortestPath = new ArrayList<Line>();
                tmpShortestPath.addAll(point.getShortestPath());
                tmpShortestPath.add(line);
                //for that other point add this line to its shortest path
                double existingPathWeight = (otherEndPoint.getShortestPath() == null) ?
                        Double.MAX_VALUE : getPathWeight(otherEndPoint.getShortestPath(), useWeights),
                        tmpPathWeight = getPathWeight(tmpShortestPath, useWeights);
                if (existingPathWeight > tmpPathWeight) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }
                if (otherEndPoint.getShortestPath() == null) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }

                if (!otherEndPoint.isVisited() && !points_to_visit.contains(otherEndPoint.getTweetID())) {
                    otherEndPoint.setDistanceToTarget(tmpPathWeight);
                    points_to_visit.add(otherEndPoint.getTweetID());
                    pointQueue.add(otherEndPoint);
                }
            }
        }

        //return whatever set as target's shortest path
        return endPoints[1].getShortestPath();
    }

    /**
     *This method uses A* algorithm to find the shortest path between endpoints of the line
     *
     * @param gLine provides 2 end points to calculate shortest path in between
     * @param spg
     * @param t uses to take the power of the direct distance between 2 points to create the heuristic
     * @return shortest path calculated with lines contained in spg
     */
    private ArrayList<Line> getShortestPathAStar(Line gLine, ArrayList<Line> spg, double t) throws TransformException {
        TrajectoryPoint[] endPoints = gLine.getEndPoints();

        if (endPoints[0].getConnections() == null || endPoints[1].getConnections() == null) {
            return null;
        }

        //make all previous shortest path calculations null
        for (Line line : spg) {
            line.clearEndPointShortestPaths();
        }

        ArrayList<TrajectoryPoint> pointQueue = new ArrayList<TrajectoryPoint>();
        HashSet<Long> points_to_visit = new HashSet<Long>();

        endPoints[0].setDistanceToTarget(gLine.getOrthodromicDistance());
        endPoints[0].setShortestPath(new ArrayList<Line>());
        pointQueue.add(endPoints[0]);
        points_to_visit.add(endPoints[0].getTweetID());

        while (!pointQueue.isEmpty()) {
            Collections.sort(pointQueue);

            TrajectoryPoint point = pointQueue.get(0);
            pointQueue.remove(0);
            point.setVisited(true);
            points_to_visit.remove(point.getTweetID());
            for (Line line : point.getConnections()) {

                //if point to consider expansion is target
                if (point.getTweetID() == endPoints[1].getTweetID()){
                    return point.getShortestPath();
                }

                //check and select the other node not the one we originated from
                TrajectoryPoint[] tmpEndPoints = line.getEndPoints();
                TrajectoryPoint otherEndPoint = tmpEndPoints[0];
                if (otherEndPoint.getTweetID() == point.getTweetID()){
                    otherEndPoint = tmpEndPoints[1];
                }

                //shortest path = point.getshortestpath + this line
                ArrayList<Line> tmpShortestPath = new ArrayList<Line>();
                tmpShortestPath.addAll(point.getShortestPath());
                tmpShortestPath.add(line);
                //for that other point add this line to its shortest path
                double existingPathWeight = (otherEndPoint.getShortestPath() == null) ?
                        Double.MAX_VALUE : getPathWeight(otherEndPoint.getShortestPath(), true),
                        tmpPathWeight = getPathWeight(tmpShortestPath, true);
                if (existingPathWeight > tmpPathWeight) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }
                if (otherEndPoint.getShortestPath() == null) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }

                if (!otherEndPoint.isVisited() && !points_to_visit.contains(otherEndPoint.getTweetID())) {
                    double distanceToTarget = JTS.orthodromicDistance(otherEndPoint.getCoordinate(),
                            endPoints[1].getCoordinate(), sourceCRS) * 1000;

                    //check to power up distanceToTarget by t - ADDED
                    otherEndPoint.setDistanceToTarget(tmpPathWeight + Math.pow(distanceToTarget, t));
                    points_to_visit.add(otherEndPoint.getTweetID());
                    pointQueue.add(otherEndPoint);
                }
            }
        }

        //return whatever set as target's shortest path
        return endPoints[1].getShortestPath();
    }

    private double getPathWeight(ArrayList<Line> shortestPathFromSPG, boolean useWeights) {
        //if shortest path does not exist
        if (shortestPathFromSPG == null) {
            return Double.MAX_VALUE;
        }

        double cumulativeWeight = 0;

        if (useWeights) {
            for (Line line : shortestPathFromSPG) {
                cumulativeWeight += line.getWeight();
            }
        } else {
            for (Line line : shortestPathFromSPG) {
                cumulativeWeight += line.getOrthodromicDistance();
            }
        }

        return cumulativeWeight;
    }

    /**
     * Calculates reachability graph for the given point set
     * takes O(n^2)
     *
     * Effective testing parameters
     *
     //                144.967, 144.9675, -37.8159, -37.815, 5
     //                144.967, 144.9679, -37.8159, -37.815, 3
     //                144.967, 144.968, -37.816, -37.815, 3
     //                144.9668, 144.9679, -37.8139, -37.813, 3
     //                144.966, 144.967, -37.814, -37.813, 10
     * @param pointSet
     * @return
     * @throws TransformException
     */
    public ArrayList<Line> createReachabilityGraph(ArrayList<TrajectoryPoint> pointSet) throws TransformException {
        if (G != null) {
            //G already created, so returning G
            return G;
        }

        //inserting all points to STR Tree
        preparedGeometryIndex = new PreparedGeometryIndex();
        preparedGeometryIndex.insert(pointSet);

        G = new ArrayList<Line>();
        System.out.println("# of lines to compare = " + (pointSet.size() * pointSet.size() - 1));

        int progress = 0, added = 0;
        //Create line for each point pair and add that line to G
        for (int i = 0; i < (pointSet.size() - 1); i++) {
            for (int j = i + 1; j < pointSet.size(); j++) {
                ++progress;
                //This check avoids adding the lines with zero lengths
                if (pointSet.get(i).getX() == pointSet.get(j).getX()
                        && pointSet.get(i).getY() == pointSet.get(j).getY()) {
                    continue;
                }
                Line line = new Line(1, pointSet.get(i), pointSet.get(j));
                Coordinate[] coordinates = line.getCoordinates();

                // this is to avoid checking all the lines
                if (line.getLength() > 0.005) {
                    continue;
                }

                double length = JTS.orthodromicDistance(coordinates[0], coordinates[1], sourceCRS) * 1000;
                Geometry centerCircle = createCircle(line.getCenterPointCoordinate(), line.getLength());

                List intersectingPoints = preparedGeometryIndex.intersects(centerCircle);
                //This check makes sure G becomes a Gabriel graph
                //http://www.passagesoftware.net/webhelp/Gabriel_Graph.htm
                if (intersectingPoints.size() > 0) {
                    continue;
                }

                line.setOrthodromicDistance(length);
                G.add(line);
                ++added;
            }
        }

        Collections.sort(G);
        //lines in G are sorted in length ascending order

        return G;
    }

    /**
     * implementing algorithm described in http://s-hull.org/
     *
     * @return
     * @throws TransformException
     */
    public ArrayList<Line> createDelaunayGraph(ArrayList<TrajectoryPoint> pointSet) throws TransformException {
        if (G != null) {
            //G already created, so returning G
            return G;
        }


        lineSet = new HashMap<String, Line>();
        triangleList = new ArrayList<Triangle>();

        //TODO if there a only 3 points or less return resulting 3 or less lines

        //following section is to make sure to avoid multiple points with the same coordinates
        selectedLocations = new HashMap<Coordinate, TrajectoryPoint>();
        ArrayList<TrajectoryPoint> processingPoints = new ArrayList<TrajectoryPoint>();
        ArrayList<TrajectoryPoint> convexHull = new ArrayList<TrajectoryPoint>();

        for (TrajectoryPoint point : pointSet) {
            if (!selectedLocations.containsKey(point.getCoordinate())) {
                processingPoints.add(point);
                selectedLocations.put(point.getCoordinate(), point);
            }
        }

        //Implementing Sweep Hull
        //1. select a x_o point randomly from x_i
        TrajectoryPoint x_o = processingPoints.get(0);
        processingPoints.remove(0);

        //2. sort according to |x_i - x_0|^2
        for (TrajectoryPoint point : processingPoints) {
            double length = JTS.orthodromicDistance(point.getCoordinate(), x_o.getCoordinate(), sourceCRS);
            point.setDistanceToTarget(length);
        }
        //3. sort point in the order of distance from x_o
        Collections.sort(processingPoints);

        //4. find the point x_j closest to x_0
        TrajectoryPoint x_j = processingPoints.get(0);
        processingPoints.remove(0);

        //5. find the point x_k that creates the smallest circumCircle with x_0 and x_j
        // and record the center of the circum-circle C
        int i_x_k = 0;
        double minCircumRadius = Double.MAX_VALUE;
        Triangle triangle;
        for (int i = 0; i < processingPoints.size(); i++) {
            triangle = new Triangle(0, new TrajectoryPoint[]{x_o, x_j, processingPoints.get(i)});
            triangle.setEquationPerpendicularLines();
            double radius = /*JTS.orthodromicDistance(triangle.getCircumCenter(), x_o.getCoordinate(), sourceCRS);*/
                    x_o.getCoordinate().distance(triangle.getCircumCenter());
            if (radius < minCircumRadius) {
                minCircumRadius = radius;
                i_x_k = i;
            }
        }
        TrajectoryPoint x_k = processingPoints.get(i_x_k);
        processingPoints.remove(i_x_k);

        //6. order point x_0, x_j, x_k to give a right handed (clockwise) system this is the initial x_o convex hull
        //create line in the order x_0, x_j and check x_k is clockwise or not relative to line
        Line line = new Line(0, x_o, x_j);
        //if x_k is not clockwise swap the locations of x_o and x_j
        if (!isPointClockwiseFromLine(x_k, line)) {
            TrajectoryPoint x_temp = x_o;
            x_o = x_j;
            x_j = x_temp;
        }
        //7. after this x_o, x_j and x_k in that order creates a right handed system
        convexHull.add(x_o);
        convexHull.add(x_j);
        convexHull.add(x_k);

        //add initial 3 lines to G;
        triangle = new Triangle(0, new TrajectoryPoint[]{x_o, x_j, x_k});
        processTriangle(triangle);

        //8. re-sort the remaining points according to x_i - C|^2 to give points s_i
        triangle.setEquationPerpendicularLines();
        Coordinate c = triangle.getCircumCenter();
        for (TrajectoryPoint point : processingPoints) {
            double length = JTS.orthodromicDistance(point.getCoordinate(), c, sourceCRS);
            point.setDistanceToTarget(length);
        }
        Collections.sort(processingPoints);

        //9. sequentially add the points s_i to the propagating 2D convex hull
        // that is seeded with the triangle formed from x_0, x_j, x_k
        // as a new point is added the facets of the 2D-hull that are visible to it form new triangles
        //144.9671, 144.9674, -37.8154, -37.8152, 10 test
        //144.96725, 144.9674, -37.8154, -37.8152, 10
        //144.9671, 144.9677, -37.8154, -37.8152, 10
        int id = 3;
        int resetID = 0; // To store the convex hull position to be replaced
        for (TrajectoryPoint point : processingPoints) {
            ArrayList<Integer> postProcessIds = new ArrayList<Integer>();
            for (int i = 0; i < convexHull.size(); i++) {
                int h = (i - 1 < 0) ? convexHull.size() - 1 : i - 1;
                int j = (i + 1 == convexHull.size()) ? 0 : i + 1;

                TrajectoryPoint h_point = convexHull.get(h);
                TrajectoryPoint i_point = convexHull.get(i);
                TrajectoryPoint j_point = convexHull.get(j);

                Line BeforeConvexHullEdge = new Line(0 , h_point, i_point);
                Line afterConvexHullEdge = new Line(0 , i_point, j_point);

                boolean isRotationClockwiseWRTBefore = isPointClockwiseFromLine(point, BeforeConvexHullEdge);
                boolean isRotationClockwiseWRTAfter = isPointClockwiseFromLine(point, afterConvexHullEdge);

                //Following decision block is illustrated at
                //https://docs.google.com/drawings/d/1XqSEKAfoJ4vw-t7scUb86bauteBli94q5g_1KUMdcNE/edit?usp=sharing
                if (!isRotationClockwiseWRTBefore && !isRotationClockwiseWRTAfter) {

                    triangle = new Triangle(triangleList.size(), new TrajectoryPoint[]{point, j_point, i_point});
                    processTriangle(triangle);

                    postProcessIds.add(i);
                } else if (isRotationClockwiseWRTBefore && !isRotationClockwiseWRTAfter) {

                    triangle = new Triangle(triangleList.size(), new TrajectoryPoint[]{point, j_point, i_point});
                    processTriangle(triangle);

                    resetID = j;
                }
            }

            //processing convex hull changes
            //Add new point at the location identified by resetID
            convexHull.add(resetID, point);

            //if there are points to be removed from convex hull
            if (!postProcessIds.isEmpty()) {
                Collections.sort(postProcessIds);
                int key = -1;
                for (int i = postProcessIds.size() -1; i >= 0; i--) {
                    if (postProcessIds.get(i) >= resetID)
                        convexHull.remove(postProcessIds.get(i) + 1);
                    else {
                        //TODO test with 144.975, 144.98, -37.822, -37.815, 3
                        //Could not use key extracted from postProcessIds as is
                        //had to assign it to new variable before using
                        //otherwise convex hull point was not removed
                        key = postProcessIds.get(i);
                        convexHull.remove(key);
                    }
                }
            }
        }
        System.out.println("# of triangles = " + triangleList.size());

        //INFO: a non-overlapping triangulation of the set of points is created

        //adjacent pairs of triangles of this triangulation must be 'flipped'
        // in order to create a Delaunay triangulation from the initial non-overlapping triangulation
        //144.9673, 144.968, -37.8154, -37.815, 10 TE$ST
        //144.967, 144.9675, -37.8154, -37.8152, 10
        //144.96731, 144.9675, -37.8154, -37.8152, 10
        boolean inner_flipped = false, outer_flipped = true;
        int iterantion = 0;
        while (outer_flipped) {
            outer_flipped = false;
            for (int i = 0; i < triangleList.size(); i++) {
                triangle = triangleList.get(i);
                for (int j = 0; j < 3; j++) {
                    int k = (j == 2) ? 0 : j + 1;
                    line = getFromLineSet(triangle.getVertices()[j], triangle.getVertices()[k]);
                    if (line.getNumOfNeighbours() > 1) {
                        int otherNeighbor = getOtherNeighbour(line.getAdjacentNeighbours(), triangle.getPos());
                        inner_flipped = checkAndFlip(triangle, triangleList.get(otherNeighbor));
                    }

                    if (inner_flipped) {
                        outer_flipped = true;
                        inner_flipped = false;
                        --i;
                        break;
                    }
                }
            }
            ++iterantion;
            System.out.println("iteration no = " + iterantion);
        }

        ArrayList<Line> DG = new ArrayList<Line>();
        DG.addAll(lineSet.values());

        Collections.sort(DG);

        return DG;
    }

    /**
     * From the array adjacentNeighbours select the neighbour other than the one provided at pos
     *
     * @param adjacentNeighbours
     * @param pos
     * @return
     */
    private int getOtherNeighbour(int[] adjacentNeighbours, int pos) {
        if (adjacentNeighbours[0] == pos) {
            return adjacentNeighbours[1];
        }
        return adjacentNeighbours[0];
    }

    /**
     * Processes a triangle
     * 1. Create lines to make the triangle
     * 1.1 add neighbouring triangles to those lines
     * 2. Add provided triangle to triangle set
     * @param triangle
     */
    private void processTriangle(Triangle triangle) {
        Line[] edges = new Line[3];
        for (int i = 0; i < 3; i++) {
            int j = (i == 2) ? 0 : i + 1;
            Line line = getFromLineSet(triangle.getVertices()[i], triangle.getVertices()[j]);
            try {
                line.setOrthodromicDistance(sourceCRS);
            } catch (TransformException e) {
                System.out.println("CRITICAL : Line distance could not be updated");
            }
            if (line.getNumOfNeighbours() > 0) {
                triangle.addNeighbour(line.getAdjacentNeighbours()[0]);
            }
            line.addNeighbour(triangleList.size());
            edges[i] = line;
        }
        triangle.setEdges(edges);
        triangleList.add(triangle);
    }

    /**
     * Select a line from lineSet to represent a line between given to points
     * Creates a new line between provided points if that line does not exist
     * @param point1
     * @param point2
     * @return
     */
    private Line getFromLineSet(TrajectoryPoint point1, TrajectoryPoint point2) {

        /* Each line's ID is a combination of tweet ids from points
         * as the combination can depend on ordering of 2 ids this method checks both combinations */
        if (lineSet.containsKey(point1.getTweetID() + "," + point2.getTweetID())) {
            return lineSet.get(point1.getTweetID() + "," + point2.getTweetID());
        } else if (lineSet.containsKey(point2.getTweetID() + "," + point1.getTweetID())) {
            return lineSet.get(point2.getTweetID() + "," + point1.getTweetID());
        } else { //if line does not exist for both ID combinations
            //creates a new line and return
            lineSet.put(point1.getTweetID() + "," + point2.getTweetID(), new Line(1, point1, point2));
            return lineSet.get(point1.getTweetID() + "," + point2.getTweetID());
        }
    }

    /**
     * Removes the line between given points
     *
     * @param point1
     * @param point2
     * @return true if line exist and removed, false if line does not exist
     */
    private boolean removeFromLineSet(TrajectoryPoint point1, TrajectoryPoint point2) {
        if (lineSet.containsKey(point1.getTweetID() + "," + point2.getTweetID())) {
            lineSet.remove(point1.getTweetID() + "," + point2.getTweetID());
            return true;
        } else if (lineSet.containsKey(point2.getTweetID() + "," + point1.getTweetID())) {
            lineSet.remove(point2.getTweetID() + "," + point1.getTweetID());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks and flips given two triangles A and B if they violate Delaunay condition
     *
     a ------- b,2
     |  A   /  |
     |    /    |
     |  /   B  |
     c,3------ d,1
     * @param triangleA
     * @param triangleB
     * @return
     */
    private boolean checkAndFlip(Triangle triangleA, Triangle triangleB) {
        TrajectoryPoint[] triangle1 = triangleA.getVertices();
        TrajectoryPoint[] triangle2 = triangleB.getVertices();
//        System.out.println("Beginning = " +triangleA.getPos() + "," + triangleB.getPos());
//        System.out.println("triangleA = " + triangleA);
//        System.out.println("triangleB = " + triangleB);

        int D_index = -1,
                A_index = -1,
                B_index = -1,
                C_index = -1;
        //Following loop locates point D and sets up points for A,B,C from given triangles
        for (int i = 0; i < 3; i++) { //this traverses triangleA clockwise
            int j = (i == 2) ? 0 : i + 1;
            TrajectoryPoint endpointB = triangle1[i];
            TrajectoryPoint endpointC = triangle1[j];

            for (int k = 2; k >= 0; k--) { //this traverses triangleB counter clockwise
                int l = (k == 0) ? 2 : k - 1;
                TrajectoryPoint endpoint1 = triangle2[k];
                TrajectoryPoint endpoint2 = triangle2[l];

                if (endpointB.getCoordinate().equals(endpoint1.getCoordinate()) &&
                        endpointC.getCoordinate().equals(endpoint2.getCoordinate())) {
                    D_index = (l == 0) ? 2 : l - 1;
                    A_index = (j == 2) ? 0 : j + 1;
                    B_index = k;
                    C_index = j;
                    break;
                }
            }

            if (D_index > -1) {
                break;
            }
        }

        if (D_index == -1) {
            System.out.println(triangleA.getPos() + "," + triangleB.getPos() + " SKIPPING FLIP!!!! " +
                    "UNEXPECTED ERROR OCCURED");
            return false;
        }

        Line BC = getFromLineSet(triangle2[B_index], triangle1[C_index]);
        //Check if BC is previously flipped
        if (BC.getFlipCount() > 16) {
            System.out.println(triangleA.getPos() + "," + triangleB.getPos() + " SKIPPING FLIP!!!! " +
                    "As these triangles are already flipped before");
            return false;
        }

        //If there is a line connecting points A and D there is no point performing determinant test
        //as those D is not gonna be inside circum circle of ABC
        if (lineSet.containsKey(triangle2[D_index].getTweetID() + "," + triangle1[A_index].getTweetID()) ||
                lineSet.containsKey(triangle1[A_index].getTweetID() + "," + triangle2[D_index].getTweetID())) {
//            System.out.println(triangleA.getPos() + "," + triangleB.getPos() + "SKIPPING FLIP!!!! before determinant test");
            return false;
        }

        //if the given triangles fail determinant test
        if (isDInsideABC(triangleA, triangle2[D_index])) {
            //flip given two triangles
            //ABC and BDC -> ABD and ADC
            //edge changes
            //AB ^ BC ^ CA and BD ^ DC ^ CB -> AB ^ BD ^ DA and AD ^ DC ^ CA
            //note that from triangle A and B, edges AB and DC respectively does not change
            //we use this as reference point to decide which edges to replace
            //edge CA from triangle A moves to triangle B after edge DC and
            //edge BD from triangle B moves to triangle A after edge AB
            //positions held by edges CA and BD in triangles A and B respectively are replaced by new edge AD
            /*
            a ------- b,2
            |  \      |
            | A  \  B |
            |      \  |
            c,3------ d,1
             */
            //adding new line
            Line newLine = getFromLineSet(triangle2[D_index], triangle1[A_index]);

            if (newLine.getNumOfNeighbours() > 0) { //if new flipping edge has at least one neighbour
                System.out.println(triangleA.getPos() + "," + triangleB.getPos() + "SKIPPING FLIP!!!!");
                return false; // These 2 triangles should not be flipped
            }

            Line[] Triangle1Edges = triangleA.getEdges(),
                    Triangle2Edges = triangleB.getEdges();

            try {
                newLine.setOrthodromicDistance(sourceCRS);
            } catch (TransformException e) {
                System.out.println("CRITICAL : Line distance could not be updated");
            }

            System.out.println("removing BC line = " + triangle1[C_index].getTweetID() +
                    "," + triangle2[B_index].getTweetID());
            System.out.println("Adding AD line = " + triangle2[D_index].getTweetID() +
                    "," + triangle1[A_index].getTweetID());

            //remove BC line as it is going to be replaced by AD line
            removeFromLineSet(triangle1[C_index], triangle2[B_index]);

            //Add neighbours to new AD line
            newLine.addNeighbour(triangleA.getPos());
            newLine.addNeighbour(triangleB.getPos());
            //setting AD is a flipped line so that next time it won't be flipped again
            newLine.setFlipCount(BC);
            int flipCount = newLine.getFlipCount();

            Line tempLine;
            //updating existing BD and AC lines as their neighbours are changing
            tempLine = getFromLineSet(triangle2[B_index], triangle2[D_index]);
            tempLine.replaceAdjacentNeighbour(triangleB.getPos(), triangleA.getPos());

            tempLine = getFromLineSet(triangle1[A_index], triangle1[C_index]);
            tempLine.replaceAdjacentNeighbour(triangleA.getPos(), triangleB.getPos());

            //set C <- D and B <- A
            triangle1[C_index] = triangle2[D_index];
            Triangle1Edges[(A_index == 2) ? 0 : A_index + 1] = Triangle2Edges[B_index];
            triangle2[B_index] = triangle1[A_index];
            Triangle2Edges[(D_index == 2) ? 0 : D_index + 1] = Triangle1Edges[C_index];

            Triangle2Edges[B_index] = newLine;
            Triangle1Edges[C_index] = newLine;

            System.out.println(triangleA.getPos() + "," + triangleB.getPos() + " FLIPPING count = " + flipCount);
//            System.out.println("triangleA = " + triangleA);
//            System.out.println("triangleB = " + triangleB);
            return true;
        }

        return false;
    }

    public static boolean isDInsideABC(Triangle abc, TrajectoryPoint d) {
        abc.setEquationPerpendicularLines();

        double D_length = d.getCoordinate().distance(abc.getCircumCenter());

        if (D_length < abc.getCircumRadius()) {
            return true;
        }

        return false;
    }

    /**
     * Checks if point D is residing inside circum circle of the triangle ABC
     * Note this ABC are traversed counter clockwise direction
     * More info - https://en.wikipedia.org/wiki/Delaunay_triangulation#Algorithms
     A ------- C
     |      /  |
     |    /    |
     |  /      |
     B ------- D
     * @param A
     * @param B
     * @param C
     * @param D
     * @return True if and only if D lies inside the circumCircle ABC
     */
    public static boolean isDInsideABC(TrajectoryPoint A, TrajectoryPoint B, TrajectoryPoint C, TrajectoryPoint D)
    {
        //Utilizes determinant calculation taken from
        //https://en.wikipedia.org/wiki/Determinant
        double a = A.getX()-D.getX(),
                d = B.getX()-D.getX(),
                g = C.getX()-D.getX(),

                b = A.getY()-D.getY(),
                e = B.getY()-D.getY(),
                h = C.getY()-D.getY(),

                c = (A.getX()*A.getX()-D.getX()*D.getX())+(A.getY()*A.getY()-D.getY()*D.getY()),
                f = (B.getX()*B.getX()-D.getX()*D.getX())+(B.getY()*B.getY()-D.getY()*D.getY()),
                i = (C.getX()*C.getX()-D.getX()*D.getX())+(C.getY()*C.getY()-D.getY()*D.getY());

        double result = a*e*i + b*f*g + c*d*h - e*c*g - b*d*i - a*f*h;
        return result > 0;
    }

    public Geometry createCircle(Coordinate centerCoordinate, double Diameter) {
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(centerCoordinate);
        shapeFactory.setSize(Diameter);
        return shapeFactory.createCircle();
    }

    public String getArea() {
        return area;
    }

    public int setSPGUsage(ArrayList<Line> userTrajectoryLines) throws TransformException {
        for (Line spgLine : SPG) {
            spgLine.resetUsageCount();
        }

        int maxUsage = 0;

        for (Line userLine : userTrajectoryLines) {
            TrajectoryPoint[] endPoints = userLine.getEndPoints();
            TrajectoryPoint point0 = selectedLocations.get(endPoints[0].getCoordinate()),
                    point1 = selectedLocations.get(endPoints[1].getCoordinate());
            Line line = new Line(userLine.getID(), point0, point1);
            ArrayList<Line> shortestPathFromSPG = getShortestPath(line, SPG, false); //uses dijkstra's algorithm
            if (shortestPathFromSPG != null) {
                for (Line spgLine : shortestPathFromSPG) {
                    spgLine.incrementUsageCount();
                    if (spgLine.getUsageCount() > maxUsage) {
                        maxUsage = spgLine.getUsageCount();
                    }
                }
            }
        }
        return maxUsage;
    }

    /**
     * taken from : https://github.com/locationtech/jts/blob/master/modules/example/src/main/java/org/locationtech/jtsexample/technique/SearchUsingPreparedGeometryIndex.java
     *
     * A spatial index which indexes {@link PreparedGeometry}s
     * created from a set of {@link Geometry}s.
     * This can be used for efficient testing
     * for intersection with a series of target geomtries.
     *
     * @author Martin Davis
     *
     */
    static class PreparedGeometryIndex
    {
        private SpatialIndex index = new STRtree();

        /**
         * Creates a new index
         *
         */
        public PreparedGeometryIndex()
        {

        }

        public void insert(Geometry geometry)
        {
            index.insert(geometry.getEnvelopeInternal(), PreparedGeometryFactory.prepare(geometry));
        }

        /**
         * Inserts a collection of Geometries into the index.
         *
         * @param geoms a collection of Geometries to insert
         */
        public void insert(Collection geoms)
        {
            for (Iterator i = geoms.iterator(); i.hasNext(); ) {
                TrajectoryPoint point = (TrajectoryPoint) i.next();
                Geometry geom = point.getJTSpoint();
                index.insert(geom.getEnvelopeInternal(), PreparedGeometryFactory.prepare(geom));
            }
        }

        /**
         * Finds all {@link PreparedGeometry}s which might
         * interact with a query {@link Geometry}.
         *
         * @param g the geometry to query by
         * @return a list of candidate PreparedGeometrys
         */
        public List query(Geometry g)
        {
            STRtree stRtree = (STRtree) index;
//            stRtree.nearestNeighbour()
            return index.query(g.getEnvelopeInternal());
        }

        /**
         * Finds all {@link PreparedGeometry}s which intersect a given {@link Geometry}
         *
         * @param g the geometry to query by
         * @return a list of intersecting PreparedGeometrys
         */
        public List intersects(Geometry g)
        {
            List result = new ArrayList();
            List candidates = query(g);
            for (Iterator it = candidates.iterator(); it.hasNext(); ) {
                PreparedGeometry prepGeom = (PreparedGeometry) it.next();
                if (prepGeom.intersects(g)) {
                    result.add(prepGeom);
                }
            }
            return result;
        }

        public boolean isThereNotAddedGeometryIntersecting(Geometry gabrielNeighbourhood) {
            List candidates = query(gabrielNeighbourhood);
            for (Iterator it = candidates.iterator(); it.hasNext(); ) {
                PreparedGeometry prepGeom = (PreparedGeometry) it.next();
                if (prepGeom.getGeometry().getUserData().equals(false)
                        && prepGeom.intersects(gabrielNeighbourhood)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This is a helper class to calculate MST using Kruskal's method
     */
    private class setOperator {
        private HashMap<Integer, HashSet<TrajectoryPoint>> pointSets = new HashMap<Integer, HashSet<TrajectoryPoint>>();

        public HashSet<TrajectoryPoint> getSet(int id) {
            return pointSets.get(id);
        }

        public void addSet (int id, HashSet<TrajectoryPoint> set) {
            pointSets.put(id, set);
        }

        public boolean isIntersect(int setID1, int setID2) {
            HashSet<TrajectoryPoint> intersection = new HashSet<TrajectoryPoint>(pointSets.get(setID1));
            HashSet<TrajectoryPoint> set2 = pointSets.get(setID2);

            intersection.retainAll(set2); //after this intersection contains elements that present in both sets

            return intersection.size() > 0;
        }

        public void makeUnion(int setID1, int setID2) {
            int smallerID = (pointSets.get(setID1).size() > pointSets.get(setID2).size()) ? setID2 : setID1,
                    largerID = (smallerID == setID1) ? setID2 : setID1;

            for (TrajectoryPoint point : pointSets.get(smallerID)) {
                point.setSetID(largerID);
            }

            pointSets.get(largerID).addAll(pointSets.get(smallerID));
            pointSets.remove(smallerID);
        }
    }
}
