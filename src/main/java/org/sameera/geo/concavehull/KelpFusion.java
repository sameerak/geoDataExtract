package org.sameera.geo.concavehull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
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
    ArrayList<Triangle> triangleList = new ArrayList<Triangle>();

    public KelpFusion(String area,
                      CoordinateReferenceSystem sourceCRS) {
        this.area = area;
        this.sourceCRS = sourceCRS;
    }

    public ArrayList<Line> GetShortestPathGraph(ArrayList<TrajectoryPoint> pointSet, double t) throws TransformException {
        //Create G = all point pair line set
        //Note: lines in G should be sorted in length ascending order
        System.out.println("started SPG calculation !!!!!!!!!!!!!!");
        if (G == null) {
//            G = createReachabilityGraph(pointSet);
            G = createDelaunayGraph(pointSet);
        }
        System.out.println("G calculation FINISHED !!!!!!!!!!!!!! G.size = " + G.size());

        if (SPG != null && previousT == t) {
            return SPG;
        }

        previousT = t;
        SPG = new ArrayList<Line>();

        //implements shortest path graph creation from KelpFusion paper
        int progress = 0,added = 0;
        for (Line gLine: G) {
//            ArrayList<Line> shortestPathFromSPG = getShortestPath(gLine, SPG);
            ArrayList<Line> shortestPathFromSPG = getShortestPathAStar(gLine, SPG, t);
            Coordinate[] endpoints = gLine.getCoordinates();
//            double length = JTS.orthodromicDistance(endpoints[0], endpoints[1], sourceCRS) * 1000;
//            double length = gLine.getLength(); //with this value all the lengths become less than 1
            double length = gLine.getOrthodromicDistance();
            if (shortestPathFromSPG == null || getPathWeight(shortestPathFromSPG) >= Math.pow(length, t)) {
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

    /**
     *This method uses dijkstra's algorithm to find the shortest path between endpoints of the line
     *
     * @param gLine provides 2 end points to calculate shortest path in between
     * @param spg
     * @return shortest path calculated with lines contained in spg
     */
    private ArrayList<Line> getShortestPath(Line gLine, ArrayList<Line> spg) {
        TrajectoryPoint[] endPoints = gLine.getEndPoints();

        if (endPoints[0].getConnections() == null || endPoints[1].getConnections() == null) {
            return null;
        }

        //make all previous shortest path calculations null
        for (Line line : spg) {
            line.clearEndPointShortestPaths();
        }

        ArrayList<TrajectoryPoint> pointQueue = new ArrayList<TrajectoryPoint>();
        endPoints[0].setShortestPath(new ArrayList<Line>());
        pointQueue.add(endPoints[0]);
        HashSet<Long> points_to_visit = new HashSet<Long>();
        points_to_visit.add(endPoints[0].getTweetID());

        while (!pointQueue.isEmpty()) {
            TrajectoryPoint point = pointQueue.get(0);
            pointQueue.remove(0);
            point.setVisited(true);
            points_to_visit.remove(point.getTweetID());
            for (Line line : point.getConnections()) {
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
                        Double.MAX_VALUE : getPathWeight(otherEndPoint.getShortestPath()),
                        tmpPathWeight = getPathWeight(tmpShortestPath);
                if (existingPathWeight > tmpPathWeight) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }
                if (otherEndPoint.getShortestPath() == null) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }

                if (otherEndPoint.getTweetID() == endPoints[1].getTweetID()){
                    return tmpShortestPath;
                }
                if (!otherEndPoint.isVisited() && !points_to_visit.contains(otherEndPoint.getTweetID())) {
                    points_to_visit.add(otherEndPoint.getTweetID());
                    pointQueue.add(otherEndPoint);
                }
            }
        }

        return endPoints[1].getShortestPath();
    }

    /**
     *This method uses A* algorithm to find the shortest path between endpoints of the line
     *
     * @param gLine provides 2 end points to calculate shortest path in between
     * @param spg
     * @param t
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
                        Double.MAX_VALUE : getPathWeight(otherEndPoint.getShortestPath()),
                        tmpPathWeight = getPathWeight(tmpShortestPath);
                if (existingPathWeight > tmpPathWeight) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }
                if (otherEndPoint.getShortestPath() == null) {
                    otherEndPoint.setShortestPath(tmpShortestPath);
                }

                if (otherEndPoint.getTweetID() == endPoints[1].getTweetID()){
                    return tmpShortestPath;
                }
                if (!otherEndPoint.isVisited() && !points_to_visit.contains(otherEndPoint.getTweetID())) {
                    double distanceToTarget = JTS.orthodromicDistance(otherEndPoint.getCoordinate(),
                            endPoints[1].getCoordinate(), sourceCRS) * 1000;

                    //check may need to power up distanceToTarget by t - ADDED
                    otherEndPoint.setDistanceToTarget(tmpPathWeight + Math.pow(distanceToTarget, t));
                    points_to_visit.add(otherEndPoint.getTweetID());
                    pointQueue.add(otherEndPoint);
                }
            }
        }

        return endPoints[1].getShortestPath();
    }

    private double getPathWeight(ArrayList<Line> shortestPathFromSPG) {
        //if shortest path does not exist
        if (shortestPathFromSPG == null) {
            return Double.MAX_VALUE;
        }

        double cumulativeWeight = 0;

        for (Line line: shortestPathFromSPG) {
            cumulativeWeight += line.getWeight();
        }

        return cumulativeWeight;
    }

    /**
     * Calculates reachability graph for the given point set
     * takes O(n^2)
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
//                centerCircle.getEnvelope();

                List intersectingPoints = preparedGeometryIndex.intersects(centerCircle);
                //This check makes sure G becomes a Gabriel graph
                //http://www.passagesoftware.net/webhelp/Gabriel_Graph.htm
                if (intersectingPoints.size() > 0) {
                    continue;
                }
//                144.967, 144.9675, -37.8159, -37.815, 5
//                144.967, 144.9679, -37.8159, -37.815, 3
//                144.967, 144.968, -37.816, -37.815, 3
//                144.9668, 144.9679, -37.8139, -37.813, 3
//                144.966, 144.967, -37.814, -37.813, 10

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
     *
     * @param pointSet
     * @return
     * @throws TransformException
     */
    public ArrayList<Line> createDelaunayGraph(ArrayList<TrajectoryPoint> pointSet) throws TransformException {
//        if (G != null) {
//            //G already created, so returning G
//            return G;
//        }


        lineSet = new HashMap<String, Line>();
        triangleList = new ArrayList<Triangle>();

        //TODO if there a only 3 points or less return resulting 3 or less lines

        //following section is to make sure to avoid multiple points with the same coordinates
        HashSet<Coordinate> selectedLocationIds = new HashSet<Coordinate>();
        ArrayList<TrajectoryPoint> processingPoints = new ArrayList<TrajectoryPoint>();
        ArrayList<TrajectoryPoint> convexHull = new ArrayList<TrajectoryPoint>();

        for (TrajectoryPoint point : pointSet) {
            if (!selectedLocationIds.contains(point.getCoordinate())) {
                processingPoints.add(point);
                selectedLocationIds.add(point.getCoordinate());
            }
        }

        //select a x_o point x_0 from x_i
        TrajectoryPoint x_o = processingPoints.get(0);
        processingPoints.remove(0);

        //sort according to |x_i - x_0|^2
        for (TrajectoryPoint point : processingPoints) {
            double length = JTS.orthodromicDistance(point.getCoordinate(), x_o.getCoordinate(), sourceCRS);
            point.setDistanceToTarget(length);
        }
        //sort point in the order of distance from x_o
        Collections.sort(processingPoints);

        //find the point x_j closest to x_0
        TrajectoryPoint x_j = processingPoints.get(0);
        processingPoints.remove(0);

        //find the point x_k that creates the smallest circumCircle with x_0 and x_j and record the center of the circum-circle C
        //TODO implement this correctly
        //for now Taking the next closest point as x_k
        TrajectoryPoint x_k = processingPoints.get(0);
        processingPoints.remove(0);
        //instead of center of the circumCircle taking centroid of the triangle
        Coordinate c = new Coordinate((x_o.getX() + x_j.getX() + x_k.getX()) / 3,
                (x_o.getY() + x_j.getY() + x_k.getY()) / 3);

        //order point x_0, x_j, x_k to give a right handed (clockwise) system this is the initial x_o convex hull
        //create line in the order x_0, x_j and check x_k is clockwise or not relative to line
        Line line = new Line(0, x_o, x_j);
        //if x_k is not clockwise swap the locations of x_o and x_j
        if (!isPointClockwiseFromLine(x_k, line)) {
            TrajectoryPoint x_temp = x_o;
            x_o = x_j;
            x_j = x_temp;
        }
        //after this x_o, x_j and x_k in that order creates a right handed system
        convexHull.add(x_o);
        convexHull.add(x_j);
        convexHull.add(x_k);

        //add initial 3 lines to G;

        Triangle triangle = new Triangle(0, new TrajectoryPoint[]{x_o, x_j, x_k});
        processTriangle(triangle);

//        lineSet.put(x_o.getTweetID() + "," + x_j.getTweetID(), new Line(1, x_o, x_j));
//        lineSet.get(x_o.getTweetID() + "," + x_j.getTweetID()).addNeighbour(0);
//        lineSet.put(x_j.getTweetID() + "," + x_k.getTweetID(), new Line(2, x_j, x_k));
//        lineSet.get(x_o.getTweetID() + "," + x_j.getTweetID()).addNeighbour(0);
//        lineSet.put(x_k.getTweetID() + "," + x_o.getTweetID(), new Line(3, x_k, x_o));
//        lineSet.get(x_o.getTweetID() + "," + x_j.getTweetID()).addNeighbour(0);
//        DG.add(new Line(1, x_o, x_j));
//        DG.add(new Line(2, x_j, x_k));
//        DG.add(new Line(3, x_k, x_o));
//        triangleList.add(triangle);

        //resort the remaining points according to x_i - C|^2 to give points s_i
        for (TrajectoryPoint point : processingPoints) {
            double length = JTS.orthodromicDistance(point.getCoordinate(), c, sourceCRS);
            point.setDistanceToTarget(length);
        }
        Collections.sort(processingPoints);

        // sequentially add the points s_i to the propagating 2D convex hull
        // that is seeded with the triangle formed from x_0, x_j, x_k
        // as a new point is added the facets of the 2D-hull that are visible to it form new triangles
        //144.9671, 144.9674, -37.8154, -37.8152, 10 test
        //144.96725, 144.9674, -37.8154, -37.8152, 10
        //144.9671, 144.9677, -37.8154, -37.8152, 10
        int id = 3;
        int resetID = 0;
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

                if (!isRotationClockwiseWRTBefore && !isRotationClockwiseWRTAfter) {

//                    DG.add(new Line(++id, point, i_point));
                    triangle = new Triangle(triangleList.size(), new TrajectoryPoint[]{point, j_point, i_point});
                    processTriangle(triangle);

                    /*line = new Line(0, triangle[0], triangle[1]);
                    if (!isPointClockwiseFromLine(triangle[2], line)) {
                        System.out.println("failed to create a right handed system 111111");
                        TrajectoryPoint x_temp = triangle[0];
                        triangle[0] = triangle[1];
                        triangle[1] = x_temp;
                    } else
                        System.out.println("CORRECTLY created a right handed system 111111");*/
//                    triangleList.add(triangle);
//                    convexHull.remove(i);
                    postProcessIds.add(i);
                } else if (isRotationClockwiseWRTBefore && !isRotationClockwiseWRTAfter) {
//                    DG.add(new Line(++id, i_point, point));
                    triangle = new Triangle(triangleList.size(), new TrajectoryPoint[]{point, j_point, i_point});
                    processTriangle(triangle);
                    /*line = new Line(0, triangle[0], triangle[1]);
                    if (!isPointClockwiseFromLine(triangle[2], line)) {
                        System.out.println("failed to create a right handed system 222222");
                        TrajectoryPoint x_temp = triangle[0];
                        triangle[0] = triangle[1];
                        triangle[1] = x_temp;
                    } else
                        System.out.println("CORRECTLY created a right handed system 222222");*/
//                    triangleList.add(triangle);
//                    convexHull.add(j, point);
                    resetID = j;
                } else if (!isRotationClockwiseWRTBefore && isRotationClockwiseWRTAfter) {
//                    DG.add(new Line(++id, point, i_point));
                }
            }

            convexHull.add(resetID, point);
            if (!postProcessIds.isEmpty()) {
                Collections.sort(postProcessIds);
                int key = -1;
                for (int i = postProcessIds.size() -1; i >= 0; i--) {
                    if (postProcessIds.get(i) >= resetID)
                        convexHull.remove(postProcessIds.get(i) + 1);
                    else {
                        //TODO test with 144.975, 144.98, -37.822, -37.815, 3
                        key = postProcessIds.get(i);
                        convexHull.remove(key);
//                        convexHull.remove(postProcessIds.get(i));
                    }
                }
            }
        }
        System.out.println("# of triangles = " + triangleList.size());

        //INFO: a non-overlapping triangulation of the set of points is created

        //adjacent pairs of triangles of this triangulation must be 'flipped'
        // in order to create a Delaunay triangulation from the initial non-overlapping triangulation
        //144.9673, 144.968, -37.8154, -37.815, 10 TE$ST TODO
        //144.967, 144.9675, -37.8154, -37.8152, 10
        //144.96731, 144.9675, -37.8154, -37.8152, 10
        boolean inner_flipped = false, outer_flipped = true;
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
        }

        ArrayList<Line> DG = new ArrayList<Line>();
        DG.addAll(lineSet.values());

        Collections.sort(DG);

        return DG;
    }

    private int getOtherNeighbour(int[] adjacentNeighbours, int pos) {
        if (adjacentNeighbours[0] == pos) {
            return adjacentNeighbours[1];
        }
        return adjacentNeighbours[0];
    }

    private void processTriangle(Triangle triangle) {
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
        }
        triangleList.add(triangle);
    }

    private Line getFromLineSet(TrajectoryPoint point1, TrajectoryPoint point2) {
        if (lineSet.containsKey(point1.getTweetID() + "," + point2.getTweetID())) {
            return lineSet.get(point1.getTweetID() + "," + point2.getTweetID());
        } else if (lineSet.containsKey(point2.getTweetID() + "," + point1.getTweetID())) {
            return lineSet.get(point2.getTweetID() + "," + point1.getTweetID());
        } else {
            lineSet.put(point1.getTweetID() + "," + point2.getTweetID(), new Line(1, point1, point2));
            return lineSet.get(point1.getTweetID() + "," + point2.getTweetID());
        }
    }

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

    private boolean checkAndFlip(Triangle triangleA, Triangle triangleB) {
        TrajectoryPoint[] triangle1 = triangleA.getVertices();
        TrajectoryPoint[] triangle2 = triangleB.getVertices();
        System.out.println("Beginning = " +triangleA.getPos() + "," + triangleB.getPos());
//        System.out.println("triangle" + triangleA.getPos() + " = " +
//                triangle1[0].getX() + "," + triangle1[0].getY() + " -> " +
//                triangle1[1].getX() + "," + triangle1[1].getY() + " -> " +
//                triangle1[2].getX() + "," + triangle1[2].getY());
//        System.out.println("triangle" + triangleB.getPos() + " = " +
//                triangle2[0].getX() + "," + triangle2[0].getY() + " -> " +
//                triangle2[1].getX() + "," + triangle2[1].getY() + " -> " +
//                triangle2[2].getX() + "," + triangle2[2].getY());

        int D_index = -1,
                A_index = -1,
                B_index = -1,
                C_index = -1;
        for (int i = 0; i < 3; i++) { //this traverses triangle1 clockwise
            int j = (i == 2) ? 0 : i + 1;
            TrajectoryPoint endpointB = triangle1[i];
            TrajectoryPoint endpointC = triangle1[j];
//            System.out.println("BC = " + endpointB.getX() + "," + endpointB.getY() + " -> " +
//                    endpointC.getX() + "," + endpointC.getY());

            for (int k = 2; k >= 0; k--) { //this traverses triangle2 counter clockwise
                int l = (k == 0) ? 2 : k - 1;
                TrajectoryPoint endpoint1 = triangle2[k];
                TrajectoryPoint endpoint2 = triangle2[l];
//                System.out.println("12 = " + endpoint1.getX() + "," + endpoint1.getY() + " -> " +
//                        endpoint2.getX() + "," + endpoint2.getY());

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

        if (lineSet.containsKey(triangle2[D_index].getTweetID() + "," + triangle1[A_index].getTweetID()) ||
                lineSet.containsKey(triangle1[A_index].getTweetID() + "," + triangle2[D_index].getTweetID())) {
            System.out.println("SKIPPING FLIP!!!! before Cline_Renka_test");
            return false;
        }

        //if the given triangles fail Cline_Renka_test
//        if(!isCline_Renka_testPassed(triangle1[0], triangle1[1], triangle1[2], triangle2[D_index])) {
        if (isDInsideABC(triangle1[0], triangle1[1], triangle1[2], triangle2[D_index])) {
            //flip given two triangles
            //ABC and BDC -> ABD and ADC
            Line newLine = getFromLineSet(triangle2[D_index], triangle1[A_index]);
            if (newLine.getNumOfNeighbours() > 0) { //if new flipping edge has at least one neighbour
                System.out.println("SKIPPING FLIP!!!!");
                return false; // These 2 triangles should not be flipped
            }

            try {
                newLine.setOrthodromicDistance(sourceCRS);
            } catch (TransformException e) {
                System.out.println("CRITICAL : Line distance could not be updated");
            }


//            DG.add(new Line(100, triangle1[A_index], triangle2[D_index]));
            removeFromLineSet(triangle1[C_index], triangle2[B_index]);

            //adding new line
            newLine.addNeighbour(triangleA.getPos());
            newLine.addNeighbour(triangleB.getPos());

            //updating existing 4 lines
            newLine = getFromLineSet(triangle2[B_index], triangle2[D_index]);
            newLine.replaceAdjacentNeighbour(triangleB.getPos(), triangleA.getPos());

            newLine = getFromLineSet(triangle1[A_index], triangle1[C_index]);
            newLine.replaceAdjacentNeighbour(triangleA.getPos(), triangleB.getPos());

            triangle1[C_index] = triangle2[D_index];
            triangle2[B_index] = triangle1[A_index];

            //TODO make sure direct changes in triangle arrays propagated to triangle list
            System.out.println("FLIPPING");
            System.out.println("triangle1 = " + triangle1[0].getX() + "," + triangle1[0].getY() + " -> " +
                    triangle1[1].getX() + "," + triangle1[1].getY() + " -> " +
                    triangle1[2].getX() + "," + triangle1[2].getY());
            System.out.println("triangle2 = " + triangle2[0].getX() + "," + triangle2[0].getY() + " -> " +
                    triangle2[1].getX() + "," + triangle2[1].getY() + " -> " +
                    triangle2[2].getX() + "," + triangle2[2].getY());

            return true;
        }

        return false;
    }

    private boolean isDInsideABC(TrajectoryPoint A, TrajectoryPoint B, TrajectoryPoint C, TrajectoryPoint D)
    {
        double a = A.getX()-D.getX(),
                d = B.getX()-D.getX(),
                g = C.getX()-D.getX(),

                b = A.getY()-D.getY(),
                e = B.getY()-D.getY(),
                h = C.getY()-D.getY(),

                c = (A.getX()*A.getX()-D.getX()*D.getX())+(A.getY()*A.getY()-D.getY()*D.getY()),
                f = (B.getX()*B.getX()-D.getX()*D.getX())+(B.getY()*B.getY()-D.getY()*D.getY()),
                i = (C.getX()*C.getX()-D.getX()*D.getX())+(C.getY()*C.getY()-D.getY()*D.getY());

//        double d12 = getDeterminant(ByDy, Bx2Dx2By2Dy2, CyDy, Cx2Dx2Cy2Dy2),
//        d22 = getDeterminant(BxDx, Bx2Dx2By2Dy2, CxDx, Cx2Dx2Cy2Dy2),
//        d32 = getDeterminant(BxDx, ByDy, CxDx, CyDy);

        double result = a*e*i + b*f*g + c*d*h - e*c*g - b*d*i - a*f*h;
        return result > 0;
    }

    private double getDeterminant(double a, double b, double c, double d) {
        return a*d - b*c;
    }

    /*
    This method is taken from the cpp source available in the s-hull web site


   minimum angle constraint for circumcircle test.
   due to Cline & Renka

   A ------- B0-8
   |      /  |
   |    /    |
   |  /      |
   C ------- D
 */
    private boolean isCline_Renka_testPassed(TrajectoryPoint A, TrajectoryPoint B, TrajectoryPoint C, TrajectoryPoint D)
    {

        double v1x = B.getX()-A.getX(),
                v1y = B.getY()-A.getY(),
                v2x = C.getX()-A.getX(),
                v2y = C.getY()-A.getY(),
                v3x = B.getX()-D.getX(),
                v3y = B.getY()-D.getY(),
                v4x = C.getX()-D.getX(),
                v4y = C.getY()-D.getY();

        double cosA = v1x*v2x + v1y*v2y;
        double cosD = v3x*v4x + v3y*v4y;

        if( cosA < 0 && cosD < 0 ) // two obtuse angles
            return false;

//        float ADX = Ax-Dx, ADy = Ay-Dy;


        if( cosA > 0 && cosD > 0 )  // two acute angles
            return true;


        double sinA = Math.abs(v1x*v2y - v1y*v2x);
        double sinD = Math.abs(v3x*v4y - v3y*v4x);

        if( cosA*sinD + sinA*cosD < 0 )
            return false;

        return true;

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

        /**
         * Inserts a collection of Geometrys into the index.
         *
         * @param geoms a collection of Geometrys to insert
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
    }
}