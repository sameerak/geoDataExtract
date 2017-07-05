package org.sameera.geo;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sameera.geo.concavehull.KelpFusion;

public class DValuationTest extends TestCase {

    public DValuationTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( DValuationTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testCorrectDvalue(){
        Double D = KelpFusion.solveForD(5,4,3);

        assertTrue(D == 2);
    }

    public void testCorrectDvalue1(){
        Double D = KelpFusion.solveForD(5,4,3.5);
        double lD = Math.pow(5,D), pD = Math.pow(4, D) + Math.pow(3.5, D);
        System.out.println("selected D = " + D + ", diff = " + lD + " - " + pD + " = " + (lD - pD));

        assertTrue(2 < D && D < 2.5);
    }

    public void testCorrectDvalue2(){
        Double D = KelpFusion.solveForD(5,4.9,4.9);
        double lD = Math.pow(5,D), pD = Math.pow(4.9, D) + Math.pow(4.9, D);
        System.out.println("selected D = " + D + ", diff = " + lD + " - " + pD + " = " + (lD - pD));

        assertTrue(34.30961849152075 < D && D < 34.3096184915208);
    }

    public void findCorrectDvalue(){
        Double D = KelpFusion.solveForD(10,4.1231,9.8489);
        double lD = Math.pow(10,D), pD = Math.pow(4.1231, D) + Math.pow(9.8489, D);
        System.out.println("selected D = " + D + ", diff = " + lD + " - " + pD + " = " + (lD - pD));

        assertTrue(34.30961849152075 < D && D < 34.3096184915208);
    }
}
