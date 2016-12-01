package org.sameera.geo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.anomally.scatterblogs.AnomalyCluster;

import java.io.IOException;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws IOException {
        String data = "@IzzyFolau good luck this week! Hunty showed what you league boys are capable of." +
                " Prove the haters wrong. #giants #gamechanger";
        List<String> result = AnomalyCluster.tokenizeStopStem(data);
        for (String token : result) {
            System.out.println(token);
        }
        assertTrue( true );
    }
}
