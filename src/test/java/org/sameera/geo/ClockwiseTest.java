package org.sameera.geo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.trajectory.clustering.Line;
import org.trajectory.clustering.TCMM;
import org.trajectory.clustering.TrajectoryPoint;

public class ClockwiseTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ClockwiseTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ClockwiseTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testClockwiseDetection(){
        TrajectoryPoint h_point1 = new TrajectoryPoint(2,1, 0,0);
        TrajectoryPoint i_point1 = new TrajectoryPoint(0,0, 0,0);
        TrajectoryPoint j_point1 = new TrajectoryPoint(1,1, 0,0);
        Line BeforeConvexHullEdge1 = new Line(0 , i_point1, j_point1);
        boolean eval = TCMM.isPointClockwiseFromLine(h_point1, BeforeConvexHullEdge1);
        assertTrue( eval );
    }

    public void testCounterClockwiseDetection() {
        TrajectoryPoint h_point1 = new TrajectoryPoint(0,2, 0,0);
        TrajectoryPoint i_point1 = new TrajectoryPoint(0,0, 0,0);
        TrajectoryPoint j_point1 = new TrajectoryPoint(1,1, 0,0);
        Line BeforeConvexHullEdge1 = new Line(0 , i_point1, j_point1);
        boolean eval = TCMM.isPointClockwiseFromLine(h_point1, BeforeConvexHullEdge1);
        assertFalse( eval );
    }
}
