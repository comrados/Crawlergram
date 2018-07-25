/*
 * Title: Tokenizer.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.extractormethods;

import com.crawlergram.topicextractor.structures.message.TEMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    final private static String PUNCT = "[\\p{Punct}–…‹›§«»¿¡!?≠\'´\"‘’“”⟨⟩°※©℗®℠™—]"; // punctuation
    final private static String CHAR_REPEATS_BEG = "^((.)\\2)\\2+"; // same char doesn't repeat more than once at the beginning
    final private static String CHAR_REPEATS_MID_END = "((.)\\2)\\2+"; // same char doesn't appear more than twice at mid and end
    final private static String DATASIZES = "^[0-9]+([kmgtp])?([bб])(it|yte|ит|айт)?(s)?$"; // data sizes
    final private static String SECONDS = "^[0-9]+([nmнм])?([sс])(ec|ек)?(ond)?(s)?$"; // seconds
    final private static String HOURS = "^[0-9]+([hч])(our)?(s)?$"; // hours
    final private static String METERS = "^[0-9]+([skmcdnкмдн])?([mм])(eter)?(s)?$"; // meters
    final private static String TIME = "^[0-9]+(ap)m$"; // time
    final private static String NUMBERS_SUP = "^[0-9]+(([kmкм])+|(ish|th|nd|st|rd|g|x|ый|ой|ий))?[0-9]*$"; // numbers
    final private static String HEX = "^([0]+x)[0-9a-f]+$"; // hexadecimal 0xCAFE1 (doesn't match words like ABBA or CAFE)
    final private static String CHAR_FILTER = "[^\u0000-\u1FFF]"; // filters all the characters that fall out this list

    /**
     * Tokenizes text to list of strings
     *
     * @param text original text
     */
    public static List<String> tokenizeToList(String text) {
        List<String> tokens = getSimpleTokens(text);
        return getTokenCompounds(tokens);
    }

    /**
     * Preprocesses text. Returns clear text without punctuation, numbers and additional charachters.
     *
     * @param text original text
     */
    public static String preprocess(String text) {
        List<String> pureTokens = tokenizeToList(text);
        StringBuilder output = new StringBuilder();
        for (String token : pureTokens)
            output.append(token).append(" ");
        return output.toString().trim();
    }

    /**
     * Tokenizes text to array of strings
     *
     * @param text original text
     */
    public static String[] tokenizeToArray(String text) {
        List<String> tokens = tokenizeToList(text);
        String[] arr = new String[tokens.size()];
        return tokens.toArray(arr);
    }

    /**
     * Tokenizes list of messages
     *
     * @param msgs messages
     */
    public static List<TEMessage> tokenizeMessages(List<TEMessage> msgs) {
        List<TEMessage> tokenized = new ArrayList<>();
        for (TEMessage msg : msgs) {
            tokenized.add(tokenizeMessage(msg));
        }
        return tokenized;
    }

    /**
     * Tokenizes single message
     *
     * @param msg message
     */
    private static TEMessage tokenizeMessage(TEMessage msg) {
        List<String> tokens = getSimpleTokens(msg.getText());
        tokens = getTokenCompounds(tokens);
        msg.setTokens(tokens);
        return msg;
    }

    /**
     * Tokenization method for strings. Returns tokens with punctuation (except of web links and numbers).
     * Simple tokens can contain compounds (e.g. web-development: web, development).
     *
     * @param text original message text
     */
    private static List<String> getSimpleTokens(String text) {
        String[] tokensA = text.split("\\s+");
        List<String> tokens = new LinkedList<>();
        for (String token : tokensA) {
            if (tokenCheck(token)) {
                tokens.add(token.replaceAll("['‘’]", ""));
            }
        }
        return tokens;
    }

    /**
     * Tokenization method for strings. Returns compounds of simple tokens (e.g. web-development: web, development).
     *
     * @param tokens simple tokens
     */
    private static List<String> getTokenCompounds(List<String> tokens) {
        List<String> tokensL = new LinkedList<>();
        for (String token : tokens) {
            String[] tokensA = token.split(PUNCT);
            for (String tokenA : tokensA) {
                tokenA = compoundTokenEdit(tokenA);
                if (tokenCheck(tokenA)) {
                    tokensL.add(tokenA);
                }
            }
        }
        return tokensL;
    }

    /**
     * various checks: emptiness, number check, link check, etc.
     *
     * @param token original token
     */
    private static boolean tokenCheck(String token) {
        return !token.isEmpty()
                && !tokenIsLink(token)
                && !tokenIsNumber(token.replaceAll(PUNCT, ""))
                && !tokensLengthIsNotOk(token, 1, 30);
    }

    /**
     * checks if token is web link
     *
     * @param token original token
     */
    private static boolean tokenIsLink(String token) {
        // http(s), www, ftp links
        String p1 = ".*(http://|https://|ftp://|file://|mailto:|nfs://|irc://|ssh://|telnet://|www\\.).+";
        // short links of type: youtube.com & youtube.com/watch?v=oHg5SJYRHA0
        String p2 = "^[A-Za-z0-9_.-~@]+\\.[A-Za-z0-9_.-~@]+(/.*)?";
        Pattern pat = Pattern.compile("(" + p1 + ")" + "|" + "(" + p2 + ")");
        Matcher mat = pat.matcher(token);
        return mat.matches();
    }

    /**
     * checks if token can be casted into double
     */
    private static boolean tokenIsNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * checks if token is longer than min and shorter than max
     *
     * @param token original token
     * @param min   minimal length of token (inclusive)
     * @param max   maximal length of token (inclusive)
     */
    private static boolean tokensLengthIsNotOk(String token, int min, int max) {
        return !((token.length() <= max) && (token.length() >= min));
    }

    /**
     * Replaces given patterns from token
     *
     * @param token token
     */
    private static String compoundTokenEdit(String token) {
        String temp = token.toLowerCase();
        temp = temp.replaceAll(CHAR_FILTER, ""); //removes redundant characters, emoticons and so on
        temp = temp.replaceAll(CHAR_REPEATS_BEG, "$2"); // removes multiple char repeats a the beginning
        temp = temp.replaceAll(CHAR_REPEATS_MID_END, "$2$2"); // removes char repeats (more than twice)
        temp = temp.replaceAll(DATASIZES, ""); // removes tokens such as 2kb, 15mb etc.
        temp = temp.replaceAll(SECONDS, ""); // removes tokens such as 2sec, 15s etc.
        temp = temp.replaceAll(HOURS, ""); // removes tokens such as 2h, 15hours etc.
        temp = temp.replaceAll(METERS, ""); // removes tokens such as 2m, 15meters etc.
        temp = temp.replaceAll(NUMBERS_SUP, ""); // removes tokens such as 2k, 15ish etc.
        temp = temp.replaceAll(TIME, ""); // removes tokens such as 2am 6pm
        temp = temp.replaceAll(HEX, ""); // removes hexadecimal numbers
        return temp;
    }

}
