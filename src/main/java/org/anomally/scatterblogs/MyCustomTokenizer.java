package org.anomally.scatterblogs;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

public class MyCustomTokenizer extends CharTokenizer {
    private static int atInt;
    private static int hashInt;

    public MyCustomTokenizer(Version matchVersion, Reader in) {
        super(matchVersion, in);
        char at = "@".charAt(0);
        atInt = 64;
        char hash = "#".charAt(0);
        hashInt = 35;
    }

    protected boolean isTokenChar(int c) {
//        if (Character.isWhitespace(c) || (!Character.isLetterOrDigit(c) && (c == atInt || c == hashInt))) {
//            return false;
//        } else {
//        }
        char b = (char) c;
        boolean ret = Character.isLetterOrDigit(c) || c == atInt || c == hashInt;
//        c
//        return !Character.isWhitespace(c);
        return ret;
    }

    public static void main(String[] args) throws Exception {
        char at = "@".charAt(0);
        atInt = Character.getNumericValue(at);
        char hash = "#".charAt(0);
        hashInt = Character.getNumericValue(hash);
        System.out.println("at = " + at + ", hash = " + hash);
        System.out.println("atInt = " + atInt + ", hashInt = " + hashInt);
    }

}
