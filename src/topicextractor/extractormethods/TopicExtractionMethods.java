/*
 * Title: ReadFromDBMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package topicextractor.extractormethods;

import storage.db.DBStorage;
import topicextractor.structures.TEDialog;
import topicextractor.structures.TEMessage;
import topicextractor.structures.TEMessageToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicExtractionMethods {

    final static String PUNCT = "[\\p{Punct}‹›«»¿¡!?\'\"‘’“”⟨⟩°※©℗®℠™]";//punctuation

    /**
     * do topic extraction for each dialog, if dates are wrong (from > to) or both dates equal zero -> read all messages
     * @param dbStorage    db storage implementation
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     */
    public static void getTopicsForAllDialogs(DBStorage dbStorage, int dateFrom, int dateTo, int docThreshold) {
        // get all dialogs
        List<TEDialog> dialogs = dbStorage.getDialogs();
        if ((dialogs != null) && (!dialogs.isEmpty())) {
            for (TEDialog dialog : dialogs) {
                // do for one
                getTopicsForOneDialog(dbStorage, dialog, dateFrom, dateTo, docThreshold);
            }
        } else {
            System.out.println("NO DIALOGS FOUND");
        }
    }

    /**
     * do topic extraction for a specific dialog, if dates are wrong (from > to) or both dates equal zero -> read all
     * @param dbStorage    db storage implementation
     * @param dialog       dialog
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     */
    public static void getTopicsForOneDialog(DBStorage dbStorage, TEDialog dialog,
                                             int dateFrom, int dateTo, int docThreshold) {
        List<TEMessage> msgs;
        // if dates valid - get only messages between these dates, otherwise - get all messages
        if (datesCheck(dateFrom, dateTo)) {
            msgs = dbStorage.readMessages(dialog, dateFrom, dateTo);
        } else {
            msgs = dbStorage.readMessages(dialog);
        }
        // check if resulting list is not empty
        if ((msgs != null) && !msgs.isEmpty()) {
            msgs = MessageMergingMethods.mergeMessages(dialog, msgs, docThreshold);
            getSimpleTokens(msgs);
            getTokenCompounds(msgs);
            Set<String> uniqueWords = getUniqueWords(msgs);
            System.out.println();
            //TODO
        } else {
            System.out.println("EMPTY MESSAGES: " + dialog.getId() + " " + dialog.getUsername());
        }
    }

    /**
     * if dates are wrong (from > to) or both dates equal zero -> false
     * @param dateFrom
     * @param dateTo
     */
    private static boolean datesCheck(int dateFrom, int dateTo) {
        return ((dateFrom != 0) || (dateTo != 0)) && dateFrom < dateTo;
    }

    /**
     * Tokenization method for strings. Returns tokens with punctuation (except of web links and numbers).
     * Simple tokens can contain compounds (e.g. web-development: web, development).
     * @param msgs original messages
     */
    private static void getSimpleTokens(List<TEMessage> msgs) {
        for (TEMessage msg : msgs) {
            String[] tokensA = msg.getText().split("[\\s]");
            List<TEMessageToken> tokens = new LinkedList<>();
            for (String token : tokensA) {
                if (!tokenCheck(token)) {
                    tokens.add(new TEMessageToken(token));
                }
            }
            msg.setTokens(tokens);
        }
    }

    /**
     * Tokenization method for strings. Returns compounds of simple tokens (e.g. web-development: web, development).
     * @param msgs original messages (with precalculated simple tokens)
     */
    private static void getTokenCompounds(List<TEMessage> msgs) {
        for (TEMessage msg : msgs) {
            for (TEMessageToken token: msg.getTokens()){
                String[] tokensA = token.getSimple().split(PUNCT);
                List<String> tokensL = new LinkedList<>();
                for (String tokenA : tokensA) {
                    if(!tokenCheck(tokenA)){
                        tokensL.add(tokenA.toLowerCase());
                    }
                }
                token.setCompound(tokensL);
            }
        }
    }

    /**
     * various checks: emptiness, number check, link check, etc.
     * @param token original token
     */
    private static boolean tokenCheck(String token) {
        return token.isEmpty()
                || tokenIsLink(token)
                || tokenIsNumber(token.replaceAll(PUNCT, ""))
                || tokensLengthIsNotOk(token, 1, 30);
    }

    /**
     * checks if token is web link
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
            Double d = Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * checks if token is longer than min and shorter than max
     * @param token original token
     * @param min     minimal length of token (inclusive)
     * @param max     maximal length of token (inclusive)
     */
    private static boolean tokensLengthIsNotOk(String token, int min, int max) {
        return !((token.length() <= max) && (token.length() >= min));
    }

    /**
     * returns a sorted list (set) of sorted compounds of tokens
     * @param msgs original msgs object (with calculated simple and compound tokens)
     */
    private static Set<String> getUniqueWords(List<TEMessage> msgs) {
        Set<String> uniqueWords = new TreeSet<>();
        for (TEMessage msg : msgs) {
            for (TEMessageToken token: msg.getTokens()){
                uniqueWords.addAll(token.getCompound());
            }
        }
        return uniqueWords;
    }

}
