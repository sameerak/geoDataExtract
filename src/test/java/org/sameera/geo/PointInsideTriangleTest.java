package org.sameera.geo;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sameera.geo.concavehull.Triangle;
import org.trajectory.clustering.TrajectoryPoint;

import static org.sameera.geo.concavehull.KelpFusion.isDInsideABC;

public class PointInsideTriangleTest
        extends TestCase {
    TrajectoryPoint h = new TrajectoryPoint(1,-3, 0,0);
    TrajectoryPoint i = new TrajectoryPoint(1,-1, 0,0);
    TrajectoryPoint j = new TrajectoryPoint(2,-2, 0,0);
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PointInsideTriangleTest( String testName )
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
    public void testInsidePoint(){
        //point traversal i, j, h creates a clockwise system

        TrajectoryPoint test = new TrajectoryPoint(1,-2, 0,0);

        boolean val = isDInsideABC(j, i, h, test);

        assertTrue(val);
    }

    public void testOutsidePoint() {
        TrajectoryPoint test = new TrajectoryPoint(0,-2, 0,0);

        boolean val = isDInsideABC(j, i, h, test);

        assertFalse(val);
    }



    public void testSilverTriangle() {

        TrajectoryPoint h1 = new TrajectoryPoint(1,-3, 0,0);
        TrajectoryPoint i1 = new TrajectoryPoint(1,-1, 0,0);
        TrajectoryPoint j1 = new TrajectoryPoint(0.999999,-2, 0,0);
        TrajectoryPoint test = new TrajectoryPoint(10,-2, 0,0);

        boolean val = isDInsideABC(i1, j1, h1, test);

        assertTrue(val);
    }
}
