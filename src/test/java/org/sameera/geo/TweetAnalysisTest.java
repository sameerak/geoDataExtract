package org.sameera.geo;

import com.google.common.annotations.VisibleForTesting;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.word.analysis.TweetAnalysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

public class TweetAnalysisTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TweetAnalysisTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TweetAnalysisTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testTokenization(){
        System.out.println("@@@@@@@@@@@ testing tokenization !!!!!!!!!!!");
        String data = "@IzzyFolau good luck this week! Hunty showed what you league boys are capable of." +
                " Prove the haters wrong. #giants #gamechanger";
        String[] result = TweetAnalysis.TokenizeTweet(data);
        for (String token : result) {
            System.out.println(token);
        }
        assertTrue( true );
    }

    public void testEmoticonTokenization() throws IOException {
        String data = "@jessieslife jessie !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! :-p";
        data = "I'm at Air New Zealand Domestic Lounge (Christchurch, Canterbury) http://t.co/9MwDtEyp";
        List<String> result = TweetAnalysis.tokenizeStopStem(data);
        for (String token : result) {
            System.out.println(token);
        }
        assertTrue( true );
    }
}
