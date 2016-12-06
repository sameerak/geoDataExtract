package org.anomally.scatterblogs;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class termCluster {

    private String term;
    private double[] minPoint;
    private double[] maxPoint;
    private double[] centroid;
    private Date maxTime;
    private Date minTime;
    private Date timeCentroid;
    private HashSet<extractedTerm> reg = new HashSet<extractedTerm>();
    private HashMap<Integer, Integer> users = new HashMap<Integer, Integer>();

    public HashMap<Integer, Integer> getUsers() {
        return users;
    }

    public float getScore() {
        float score = reg.size()/users.size();
        return score;
    }


    public termCluster(extractedTerm term){
        this.term = term.getTerm();
        centroid = term.getLocation();
        minPoint = term.getLocation();
        maxPoint = term.getLocation();
        timeCentroid = term.getTimestamp();
        maxTime = term.getTimestamp();
        minTime = term.getTimestamp();
        reg.add(term);
        users.put(term.getUserID(), 1);
    }

    public termCluster(String term, double[] centroid, Date timeCentroid) {
        this.term = term;
        this.centroid = centroid;
        this.minPoint = centroid;
        this.maxPoint = centroid;
        this.timeCentroid = timeCentroid;
        this.maxTime = timeCentroid;
        this.minTime = timeCentroid;
    }

    public double[] getCentroid() {
        return centroid;
    }

    public String getPrintableCentroid() {
        return "[" + centroid[0] + "," + centroid[1] + "]";
    }

    public double[] getMinPoint() {
        return minPoint;
    }

    public double[] getMaxPoint() {
        return maxPoint;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    public HashSet<extractedTerm> getReg() {
        return reg;
    }

    public void setReg(HashSet<extractedTerm> reg) {
        this.reg = reg;
    }

    public void addTerm(extractedTerm term) {
        //set centroid
        if (reg.isEmpty()){
            centroid = term.getLocation();
            timeCentroid = term.getTimestamp();
        } else {
            double[] newcentroid = {getWeightedMean(centroid[0], term.getLocation()[0], reg.size())
                    , getWeightedMean(centroid[1], term.getLocation()[1], reg.size())};
            centroid = newcentroid;

            timeCentroid = getWeightedTime(timeCentroid, term.getTimestamp(), reg.size());
        }

        //set min
//        double[] newminPoint = {(term.getLocation()[0] < minPoint[0]) ? term.getLocation()[0] : minPoint[0]
//                ,(term.getLocation()[1] < minPoint[1]) ? term.getLocation()[1] : minPoint[1]};
//        minPoint = newminPoint;

        if (reg.isEmpty()) {
            minPoint = term.getLocation();
            minTime = term.getTimestamp();
        }

        //set max
//        double[] newmaxPoint = {(term.getLocation()[0] > maxPoint[0]) ? term.getLocation()[0] : maxPoint[0]
//                ,(term.getLocation()[1] > maxPoint[1]) ? term.getLocation()[1] : maxPoint[1]};
//        maxPoint = newmaxPoint;

        if (minPoint != term.getLocation()) {
            maxPoint = term.getLocation();
            maxTime = term.getTimestamp();
        }

        //add term to reg
        reg.add(term);
        if (users.containsKey(term.getUserID())) {
            int num = users.get(term.getUserID());
            num++;
            users.put(term.getUserID(), num);
        } else {
            users.put(term.getUserID(), 1);
        }
    }

    private double getWeightedMean (double num1, double num2, int weight){
        double result = ((num1 * weight) + num2) / (weight + 1);
//        System.out.println("num1 = " + num1 + ", num2 = " + num2  + ", weight = " + weight + ", result = " + result);
        return result;
    }

    private Date getWeightedTime (Date num1, Date num2, int weight){
        long result = ((num1.getTime() * weight) + num2.getTime()) / (weight + 1);
//        System.out.println("num1 = " + num1 + ", num2 = " + num2  + ", weight = " + weight + ", result = " + result);
        return new Date(result);
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void clear() {
        reg.clear();
        users.clear();
        this.minPoint = centroid;
        this.maxPoint = centroid;
    }

    public Date getMaxTime() {
        return maxTime;
    }

    public Date getMinTime() {
        return minTime;
    }

    public Date getTimeCentroid() {
        return timeCentroid;
    }
}
