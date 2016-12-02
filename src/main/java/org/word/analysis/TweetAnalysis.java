package org.word.analysis;


import java.util.List;
import java.util.regex.Pattern;

public class TweetAnalysis {

    public static String[] TokenizeTweet(String tweetText) {
        Pattern whiteSpaceSplitter = Pattern.compile("\\s");
        String userNamesPattern = "@[\\w_]+";
        String[] splitted = whiteSpaceSplitter.split(tweetText);

        for (String token: splitted) {
            if (token.matches(userNamesPattern))
                System.out.println("user name = " + token);;
        }


        return splitted;
    }
}
