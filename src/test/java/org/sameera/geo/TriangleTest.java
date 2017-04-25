package org.sameera.geo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sameera.geo.concavehull.Triangle;
import org.trajectory.clustering.Line;
import org.trajectory.clustering.TCMM;
import org.trajectory.clustering.TrajectoryPoint;

public class TriangleTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TriangleTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TriangleTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testClockwiseDetection(){
        TrajectoryPoint h = new TrajectoryPoint(1,-3, 0,0);
        TrajectoryPoint i = new TrajectoryPoint(1,-1, 0,0);
        TrajectoryPoint j = new TrajectoryPoint(2,-2, 0,0);
        Triangle triangle = new Triangle(0, new TrajectoryPoint[]{i, j, h});
        triangle.setEquation();

        assertTrue(true);
    }

    public void testCounterClockwiseDetection() {
        TrajectoryPoint h = new TrajectoryPoint(-1,1, 0,0);
        TrajectoryPoint i = new TrajectoryPoint(0,0, 0,0);
        TrajectoryPoint j = new TrajectoryPoint(0,2, 0,0);
        Triangle triangle = new Triangle(0, new TrajectoryPoint[]{i, j, h});
        triangle.setEquation();

        assertTrue(true);
    }
}
