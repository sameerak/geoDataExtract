package org.anomally.scatterblogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class KMeans {

    //Number of Clusters. This metric should be related to the number of points
    private int NUM_CLUSTERS = 2;
    //Number of Points
    private int NUM_POINTS = 15;
    //Min and Max X and Y
    private static final int MIN_COORDINATE = 0;
    private static final int MAX_COORDINATE = 10;

    private HashSet<extractedTerm> points;
    private List<termCluster> clusters;
    private List<double[]> lastCentroids;

    public KMeans(termCluster splitCluster) {
//        System.out.println("--------------init K-means-------------- for term = " + splitCluster.getTerm());

        this.points = splitCluster.getReg();

//        System.out.println("# points = " + points.size());
//        for (extractedTerm point: points) {
//            System.out.println(point.getPrintableLocation() + "");
//        }
        this.clusters = new ArrayList<termCluster>(2);

        termCluster maxcluster = new termCluster(splitCluster.getTerm(), splitCluster.getMaxPoint());
        termCluster mincluster = new termCluster(splitCluster.getTerm(), splitCluster.getMinPoint());
        clusters.add(maxcluster);
        clusters.add(mincluster);
    }

    /*public static void main(String[] args) {

        KMeans kmeans = new KMeans();
        kmeans.init();
        kmeans.calculate();
    }*/

    //Initializes the process
    /*public void init() {
        //Create Points
//        points = Point.createRandomPoints(MIN_COORDINATE,MAX_COORDINATE,NUM_POINTS);

        //Create Clusters
        //Set Random Centroids
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            Cluster cluster = new Cluster(i);
            Point centroid = Point.createRandomPoint(MIN_COORDINATE,MAX_COORDINATE);
            cluster.setCentroid(centroid);
            clusters.add(cluster);
        }

        //Print Initial state
        plotClusters();
    }*/

    /*private void plotClusters() {
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            Cluster c = clusters.get(i);
            c.plotCluster();
        }
    }*/

    //The process to calculate the K Means, with iterating method.
    public List<termCluster> calculate() {
        boolean finish = false;
        int iteration = 0;

        // Add in new data, one at a time, recalculating centroids with each new one.
        while(!finish) {
            //Clear cluster state
            clearClusters();

            lastCentroids = getCentroids();

            //Assign points to the closer cluster
            assignCluster();

            //Calculate new centroids.
//            calculateCentroids();


            if (clusters.get(0).getReg().isEmpty() || clusters.get(1).getReg().isEmpty()) {
                System.out.println("1_____________This is WRONG!!!!!!!!!!!!!");
            }

            iteration++;

            List<double[]> currentCentroids = getCentroids();

            //Calculates total distance between new and old Centroids
            double distance = 0;
            for(int i = 0; i < lastCentroids.size(); i++) {
                distance += getDistance(lastCentroids.get(i),currentCentroids.get(i));
            }
//            System.out.println("#################");
//            System.out.println("Iteration: " + iteration);
//            System.out.println("Centroid distances: " + distance);
//            plotClusters();

            if(distance == 0) {
                finish = true;
            }
        }
        if (clusters.get(0).getReg().isEmpty() || clusters.get(1).getReg().isEmpty()) {
            System.out.println("2_______________This is WRONG!!!!!!!!!!!!!");
        }

        return clusters;
    }

    private void clearClusters() {
        for(termCluster cluster : clusters) {
            cluster.clear();
        }
    }

    private List getCentroids() {
        List centroids = new ArrayList<double[]>(NUM_CLUSTERS);
        for(termCluster cluster : clusters) {
            double[] aux = cluster.getCentroid();
            centroids.add(aux);
        }
        return centroids;
    }

    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;

        for(extractedTerm point : points) {
            min = max;
//            for(int i = 0; i < NUM_CLUSTERS; i++) {
//                distance = getDistance(point.getLocation(), lastCentroids.get(i));
//                if(distance < min){
//                    min = distance;
//                    cluster = i;
//                }
//            }
            if (getDistance(point.getLocation(), lastCentroids.get(0)) < getDistance(point.getLocation(), lastCentroids.get(1))) {
                clusters.get(0).addTerm(point);
            }
            else {
                clusters.get(1).addTerm(point);
            }
//            point.setCluster(cluster);
//            clusters.get(cluster).addTerm(point);
        }


    }

    private void calculateCentroids() {
        for(termCluster cluster : clusters) {
            double sumX = 0;
            double sumY = 0;
            HashSet<extractedTerm> list = cluster.getReg();
            int n_points = list.size();

            for(extractedTerm point : list) {
                sumX += point.getLocation()[0];
                sumY += point.getLocation()[1];
            }

            double[] centroid = new double[2];
            if(n_points < 0) {
                centroid[0] = sumX / n_points;
                centroid[1] = sumY / n_points;
            }
            cluster.setCentroid(centroid);
        }
    }

    private static double getDistance(double[] p1, double[] p2) {
        double lat = p1[0] - p2[0];
        double longi = p1[1] - p2[1];

        return Math.sqrt(lat * lat + longi * longi);
    }
}
