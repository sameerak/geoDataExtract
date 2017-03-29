package org.dataonfocus.clustering;

import java.util.List;

import org.dataonfocus.clustering.structures.DataPoint;

public interface Algorithm {

    public void setPoints(List<DataPoint> points);

    public void cluster();

}
