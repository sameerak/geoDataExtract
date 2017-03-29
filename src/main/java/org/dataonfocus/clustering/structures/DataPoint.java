package org.dataonfocus.clustering.structures;

import java.util.List;

public interface DataPoint {

    public double distance(DataPoint datapoint);

    public double distanceWithTime(DataPoint datapoint);

    public double distance(double x, double y);

    public void setCluster(int id);

    public int getCluster();

    public double getX();

    public double getY();

    public String getID();

    public long getTimestamp();

    public List<DataPoint> getNeighbors();

    public int getNeighborsSize();

    public void setNeighbors(List<DataPoint> neighbors);
}
