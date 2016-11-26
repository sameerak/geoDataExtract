package org.anomally.scatterblogs;

import java.util.ArrayList;
import java.util.List;

public class kmeansCluster {

    public List<extractedTerm> points;
    public extractedTerm centroid;
    public int id;

    //Creates a new Cluster
    public kmeansCluster(int id) {
        this.id = id;
        this.points = new ArrayList<extractedTerm>();
        this.centroid = null;
    }

    public List<extractedTerm> getPoints() {
        return points;
    }

    public void addPoint(extractedTerm point) {
        points.add(point);
    }

    public void setPoints(List<extractedTerm> points) {
        this.points = points;
    }

    public extractedTerm getCentroid() {
        return centroid;
    }

    public void setCentroid(extractedTerm centroid) {
        this.centroid = centroid;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        points.clear();
    }

    public void plotCluster() {
        System.out.println("[Cluster: " + id+"]");
        System.out.println("[Centroid: " + centroid + "]");
        System.out.println("[Points: \n");
        for(extractedTerm p : points) {
            System.out.println(p);
        }
        System.out.println("]");
    }

}