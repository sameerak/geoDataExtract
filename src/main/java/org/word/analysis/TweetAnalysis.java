package org.word.analysis;


import org.anomally.scatterblogs.MyCustomTokenizer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TweetAnalysis {

    //source
    // https://github.com/twitter/commons/blob/master/src/java/com/twitter/common/text/extractor/EmoticonExtractor.java
    private static final String SPACE_EXCEPTIONS = "\\n\\r";
    public static final String SPACE_CHAR_CLASS = "\\p{C}\\p{Z}&&[^" + SPACE_EXCEPTIONS + "\\p{Cs}]";
    public static final String SPACE_REGEX = "[" + SPACE_CHAR_CLASS + "]";

    public static final String PUNCTUATION_CHAR_CLASS = "\\p{P}\\p{M}\\p{S}" + SPACE_EXCEPTIONS;
    public static final String PUNCTUATION_REGEX = "[" + PUNCTUATION_CHAR_CLASS + "]";
    private static final String EMOTICON_DELIMITER =
            SPACE_REGEX + "|" + PUNCTUATION_REGEX;

    public static final Pattern SMILEY_REGEX_PATTERN = Pattern.compile("(:|:-)[)DdpP\\]]|<3");
    public static final Pattern FROWNY_REGEX_PATTERN = Pattern.compile("(:|:-)[\\[(<]");
    public static final Pattern EMOTICON_REGEX_PATTERN =
            Pattern.compile("(?<=^|" + EMOTICON_DELIMITER + ")("
                    + SMILEY_REGEX_PATTERN.pattern() + "|" + FROWNY_REGEX_PATTERN.pattern()
                    + ")+(?=$|" + EMOTICON_DELIMITER + ")");

    public static final Pattern HTML_REGEX_PATTERN = Pattern.compile("<[^>]+>");
    public static final Pattern AT_MENTION_REGEX_PATTERN = Pattern.compile("(?:@[\\w_]+)");
    public static final Pattern HASH_TAG_REGEX_PATTERN = Pattern.compile("(?:#+[\\w_]+[\\w\'_\\-]*[\\w_]+)");
    public static final Pattern URL_REGEX_PATTERN = Pattern.compile("http[s]?://(?:[a-z]|[0-9]|[$-_@.&amp;+]|[!*(),]|(?:%[0-9a-f][0-9a-f]))+");
    public static final Pattern NUMBERS_REGEX_PATTERN = Pattern.compile("(?:(?:\\d+,?)+(?:\\.?\\d+)?)");
    public static final Pattern WORDS_WITH_SPECIAL_REGEX_PATTERN = Pattern.compile("(?:[a-z][a-z'\\-_]+[a-z])");
    public static final Pattern OTHER_WORDS_REGEX_PATTERN = Pattern.compile("(?:[\\w_]+)");
    public static final Pattern ANYTHING_REGEX_PATTERN = Pattern.compile("(?:\\S)");

    public static final Pattern TWEET_REGEX_PATTERN = Pattern.compile(""
            + AT_MENTION_REGEX_PATTERN.pattern() + "|" + HASH_TAG_REGEX_PATTERN.pattern() + "|"
            + SMILEY_REGEX_PATTERN.pattern() + "|" + FROWNY_REGEX_PATTERN.pattern() + "|"
            + HTML_REGEX_PATTERN.pattern() + "|" + URL_REGEX_PATTERN.pattern() + "|"
            + NUMBERS_REGEX_PATTERN.pattern() + "|" + WORDS_WITH_SPECIAL_REGEX_PATTERN.pattern() + "|"
            + OTHER_WORDS_REGEX_PATTERN.pattern() + "|" + ANYTHING_REGEX_PATTERN.pattern()
            + "");


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

    public static List<String> tokenizeStopStem(String input){
        input = input.toLowerCase();
        List<String> result = new ArrayList<String>();
        TokenStream tokenStream = new PatternTokenizer(new StringReader(input), TWEET_REGEX_PATTERN, 0);
        try {
            tokenStream.reset();
        } catch (IOException e) {
            System.out.println("Exception occurred = " + e.getMessage());
            return result;
        }
        CharTermAttribute charTermAttr = tokenStream.getAttribute(CharTermAttribute.class);
        try{
            while (tokenStream.incrementToken()) {
                String word = charTermAttr.toString();
                word = word.replaceAll("([a-z])\\1{1,}", "$1$1");
                result.add(word);
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return result;
    }
}
