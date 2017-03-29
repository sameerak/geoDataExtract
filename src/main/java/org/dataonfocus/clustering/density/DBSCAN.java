package org.dataonfocus.clustering.density;

import java.util.*;

import com.mongodb.*;
import org.dataonfocus.clustering.Algorithm;
import org.dataonfocus.clustering.structures.Cluster;
import org.dataonfocus.clustering.structures.DataPoint;
import org.dataonfocus.clustering.structures.DataPointImpl;

public class DBSCAN implements Algorithm{
    //need to remove less useful twitter accounts

    public List<DataPoint> points;
    private List clusters;
    String temp = "144.95, 144.98, -37.82, -37.81";

    public double max_distance;
    public int min_points;

    public boolean[] visited;
    public HashMap<String, DataPoint> visitedTweetIDs = new HashMap<String, DataPoint>();
    //initialize a DB connection

    //connecting mongoDB on local machine.
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    //connecting to database named test
    DB db = mongoClient.getDB("test");
    // getting collection of all files
    DBCollection collection = db.getCollection("correctTweetId");

    public static void main(String[] args) throws Exception {
        DBSCAN clusterer = new DBSCAN(500, 10);
        clusterer.cluster();

        System.out.println("# of clusters = " + clusterer.clusters.size());
    }

    public DBSCAN(double max_distance, int min_points) {
//        this.points = new ArrayList<DataPoint>();
        this.clusters = new ArrayList();
        this.max_distance = max_distance;
        this.min_points = min_points;
    }

    public void cluster() {
        // get all record set temporally ordered

        BasicDBObject query = new BasicDBObject();

        String[] coordinates1 = temp.split(", ");
        double[] coor = {Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1])};
        double[] coor1 = {Double.parseDouble(coordinates1[2]), Double.parseDouble(coordinates1[3])};

            List<BasicDBObject> andArray = new ArrayList<BasicDBObject>();
            andArray.add(new BasicDBObject("coordinates.0", BasicDBObjectBuilder.start("$gte", coor[0])
                    .add("$lte", coor[1]).get()));
            andArray.add(new BasicDBObject("coordinates.1", BasicDBObjectBuilder.start("$gte", coor1[0])
                    .add("$lte", coor1[1]).get()));
            query.put("$and", andArray);
//        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));
        int n = 0;

        while(dbCursor.hasNext()) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();
            DataPointImpl point = new DataPointImpl(basicObject);

            if(!visitedTweetIDs.containsKey(point.getID())) {
                visitedTweetIDs.put(point.getID(), point);

                List<DataPoint> neighbors = (point.getNeighbors() != null) ? point.getNeighbors() :getNeighbors(point);

                if (point.getNeighbors() == null) {
                    point.setNeighbors(neighbors);
                }

                System.out.println("Neighbor size = " + neighbors.size());

                if(neighbors.size() >= min_points) {
                    Cluster c = new Cluster(clusters.size());
                    buildCluster(point,c,neighbors);
                    clusters.add(c);

                    n++;
                    System.out.println("# of clusters = " + n);
                }
            }
        }
    }

    private void buildCluster(DataPoint d, Cluster c, List<DataPoint> neighbors) {
        c.addPoint(d);
        List<DataPoint> newNeighbors = new ArrayList<DataPoint>();

        int count = 0;
        for (DataPoint point : neighbors) {
            if(!visitedTweetIDs.containsKey(point.getID())) {
                visitedTweetIDs.put(point.getID(), point);
                List tempnewNeighbors = (point.getNeighbors() != null) ? point.getNeighbors() :getNeighbors(point);

                if (point.getNeighbors() == null) {
                    point.setNeighbors(tempnewNeighbors);
                }
//                System.out.println("tempnewNeighbors size = " + tempnewNeighbors.size());

                if(tempnewNeighbors.size() >= min_points) {
                    newNeighbors.addAll(tempnewNeighbors);
                }
            }
            if(point.getCluster() == -1) {
                c.addPoint(point);
            }
            count ++;

            if (count == neighbors.size()){
                neighbors = newNeighbors;
                newNeighbors = new ArrayList<DataPoint>();
                count = 0;
            }
        }
    }

    private List<DataPoint> getNeighbors(DataPoint d) {
        // get tweets covered by a square centering around "d"
        BasicDBObject query = new BasicDBObject();
        double[] coor1 = {d.getX() - max_distance,
                d.getX() + max_distance,
                d.getY() - max_distance,
                d.getY() + max_distance};

        List<BasicDBObject> andArray = new ArrayList<BasicDBObject>();
        andArray.add(new BasicDBObject("coordinates.0", BasicDBObjectBuilder.start("$gte", coor1[0])
                .add("$lte", coor1[1]).get()));
        andArray.add(new BasicDBObject("coordinates.1", BasicDBObjectBuilder.start("$gte", coor1[2])
                .add("$lte", coor1[3]).get()));
        query.put("$and", andArray);
//        query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

        DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));
        // Then query for the circle
        List<DataPoint> neighbors = new ArrayList<DataPoint>();
        int i = 0;
        while (dbCursor.hasNext()) {
            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();
            String id = basicObject.getString("tweet_id");
            DataPoint neighbor = visitedTweetIDs.containsKey(id) ? visitedTweetIDs.get(id) : new DataPointImpl(basicObject);

            double distance = d.distance(neighbor);

            if(distance <= max_distance) {
                neighbors.add(neighbor);
            }
            i++;
        }

        return neighbors;
    }

    public void setPoints(List points) {
        this.points = points;
        this.visited = new boolean[points.size()];
    }

}
