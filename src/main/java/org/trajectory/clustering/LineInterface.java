package org.trajectory.clustering;

import com.vividsolutions.jts.geom.Coordinate;

public interface LineInterface {
    public double getTheta();
    public double getLength();
    public TrajectoryPoint getCenterPoint();
    public Coordinate[] getCoordinates();
}
