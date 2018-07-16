/*
 * Title: TopicExtractionMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package topicextractor.extractormethods;

import storage.db.DBStorage;
import topicextractor.maths.gras.GRAS;
import topicextractor.maths.ldadmm.models.GSDMM;
import topicextractor.maths.ldadmm.models.GSLDA;
import topicextractor.structures.TEDialog;
import topicextractor.structures.message.TEMessage;
import topicextractor.structures.message.TEMessageToken;
import topicextractor.structures.results.TEResults;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicExtractionMethods {

    final private static String PUNCT = "[\\p{Punct}–…‹›§«»¿¡!?≠\'´\"‘’“”⟨⟩°※©℗®℠™—]"; // punctuation
    final private static String CHAR_REPEATS_BEG = "^((.)\\2)\\2+"; // same char doesn't repeat more than once at the beginning
    final private static String CHAR_REPEATS_MID_END = "((.)\\2)\\2+"; // same char doesn't appear more than twice at mid and end
    final private static String DATASIZES = "^[0-9]+([kmgtp])?([bб])(it|yte|ит|айт)?(s)?$"; // data sizes
    final private static String SECONDS = "^[0-9]+([nmнм])?([sс])(ec|ек)?(ond)?(s)?$"; // seconds
    final private static String HOURS = "^[0-9]+([hч])(our)?(s)?$"; // hours
    final private static String METERS = "^[0-9]+([skmcdnкмдн])?([mм])(eter)?(s)?$"; // meters
    final private static String TIME = "^[0-9]+(ap)m$"; // time
    final private static String NUMBERS_SUP = "^[0-9]+(([kmкм])+|(ish|th|nd|st|rd|g|x|ый|ой|ий))?[0-9]*$"; // numbers
    final private static String HEX = "^([0]+x)[0-9a-f]+$"; // hexadecimal 0xCAFE1 (doesn't remove abbreviations as ABBA)

    final private static String CHAR_FILTER = "[^\u0000-\u1FFF]"; // filters all the characters that fall out this list

    /**
     * do topic extraction for each dialog, if dates are wrong (from > to) or both dates equal zero -> read all messages
     * @param dbStorage    db storage implementation
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     * @param msgMerging    if true - artificial documents will be created from messages, preferable for LDA
     */
    public static void getTopicsForAllDialogs(DBStorage dbStorage, int dateFrom, int dateTo, int docThreshold, boolean msgMerging) {
        // get all dialogs
        List<TEDialog> dialogs = dbStorage.getDialogs();
        if ((dialogs != null) && (!dialogs.isEmpty())) {
            for (TEDialog dialog : dialogs) {
                // do for one
                getTopicsForOneDialog(dbStorage, dialog, dateFrom, dateTo, docThreshold, msgMerging);
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
     * @param msgMerging   if true - artificial documents will be created from messages, preferable for LDA
     */
    public static void getTopicsForOneDialog(DBStorage dbStorage, TEDialog dialog,
                                             int dateFrom, int dateTo, int docThreshold, boolean msgMerging) {
        List<TEMessage> msgs;
        // if dates valid - get only messages between these dates, otherwise - get all messages
        if (datesCheck(dateFrom, dateTo)) {
            msgs = dbStorage.readMessages(dialog, dateFrom, dateTo);
        } else {
            msgs = dbStorage.readMessages(dialog);
        }
        // check if resulting list is not empty
        if ((msgs != null) && !msgs.isEmpty()) {
            if (msgMerging) msgs = MessageMergingMethods.mergeMessages(dialog, msgs, docThreshold);
            removeEmptyMessages(msgs);
            getSimpleTokens(msgs);
            getTokenCompounds(msgs);

            removeStopWords(msgs, "en");
            removeStopWords(msgs, "ru");

            Map<String, String> uniqueWords = getUniqueWords(msgs);
            uniqueWords = GRAS.doStemming(uniqueWords, 5, 4, 0.8);
            getStemmedText(msgs, uniqueWords);

            GSDMM dmm = new GSDMM(msgs, 10, 0.1, 0.1, 1000, 10);
            TEResults resDMM = dmm.inference();

            GSLDA lda = new GSLDA(msgs, 10, 0.01, 0.1, 1000, 10);
            TEResults resLDA = lda.inference();

            //print some stats
            statUtils(msgs, uniqueWords);

            printTopWords(resDMM);
            printTopWords(resLDA);

            try {
                saveSet("words.txt", uniqueWords);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO
        } else {
            System.out.println("EMPTY MESSAGES: " + dialog.getId() + " " + dialog.getUsername());
        }
    }

    /**
     * if dates are wrong (from > to) or both dates equal zero -> false
     * @param dateFrom date from
     * @param dateTo date to
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
            String[] tokensA = msg.getText().split("\\s+");
            List<TEMessageToken> tokens = new LinkedList<>();
            for (String token : tokensA) {
                if (tokenCheck(token)) {
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
                    tokenA = compoundTokenEdit(tokenA);
                    if(tokenCheck(tokenA)){
                        tokensL.add(tokenA);
                    }
                }
                token.setCompounds(tokensL);
            }
        }
    }

    /**
     * various checks: emptiness, number check, link check, etc.
     * @param token original token
     */
    private static boolean tokenCheck(String token) {
        return !token.isEmpty()
                && !tokenIsLink(token)
                && !tokenIsNumber(token.replaceAll(PUNCT, ""))
                && !tokensLengthIsNotOk(token, 2, 30);
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
            Double.parseDouble(token);
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

    private static String compoundTokenEdit(String token){
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

    /**
     * returns a sorted list (set) of sorted compounds of tokens
     * @param msgs original msgs object (with calculated simple and compound tokens)
     */
    private static Map<String, String> getUniqueWords(List<TEMessage> msgs) {
        Map<String, String> uniqueWords = new TreeMap<>();
        for (TEMessage msg : msgs) {
            for (TEMessageToken token: msg.getTokens()){
                for (String compound: token.getCompounds()){
                    uniqueWords.put(compound, null);
                }
            }
        }
        return uniqueWords;
    }

    /**
     * Creates a message consisting of stems of original words
     * @param msgs messages
     * @param uniqueWords unique words
     */
    private static void getStemmedText(List<TEMessage> msgs ,Map<String, String> uniqueWords){
        for (TEMessage msg: msgs){
            StringBuilder stemmedText = new StringBuilder();
            List<TEMessageToken> tokens = msg.getTokens();
            for (TEMessageToken token: tokens){
                List<String> compounds = token.getCompounds();
                for (String compound: compounds){
                    stemmedText.append(uniqueWords.get(compound)).append(" ");
                }
            }
            msg.setStemmedText(stemmedText.toString().trim());
        }
    }

    /**
     * Removes empty messages from the list
     * @param msgs messages list
     */
    private static void removeEmptyMessages(List<TEMessage> msgs){
        for (int i = 0; i < msgs.size(); i++) {
            if (msgs.get(i).getText().isEmpty()){
                msgs.remove(i);
            }
        }
    }

    /**
     * removes stopwords from token compounds
     * @param msgs messages
     * @param language language code (i.e. en, es, de, ru etc.)
     */
    private static void removeStopWords(List<TEMessage> msgs, String language) {
        Set<String> stopWords = loadStopWords(language);
        for (int i = 0; i < msgs.size(); i++){
            List<TEMessageToken> tokens = msgs.get(i).getTokens();
            for (int j = 0; j < tokens.size(); j++){
                List<String> compounds = tokens.get(j).getCompounds();
                boolean removalFlag = false;
                for (int k = 0; k < compounds.size(); k++){
                    if (stopWords.contains(compounds.get(k))) {
                        removalFlag = true;
                        msgs.get(i).getTokens().get(j).getCompounds().remove(k);
                        k--;
                    }
                }
            }
        }
    }

    /**
     * loads stop words from a file to the set
     * @param language language code (i.e. en, es, de, ru etc.)
     */
    private static Set<String> loadStopWords(String language){
        Set<String> stopWords = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("stopwords" + File.separator + language.toLowerCase() + ".txt"))) {
            for (String doc; (doc = br.readLine()) != null;)
                if (!doc.trim().isEmpty()) stopWords.add(doc.trim());
        } catch (IOException e){
            System.out.println("Can't read stopwords for " + language.toUpperCase() + " language");
        }
        return stopWords;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static void statUtils(List<TEMessage> msgs ,Map<String, String> uniqueWords){
        System.out.println();
        System.out.println("Number of documents: " + msgs.size());
        double l = calcL(uniqueWords);
        double tok = calcAv(msgs);
        System.out.println("Number of valid unique words: " + uniqueWords.keySet().size());
        System.out.println("Ratio tokens_in_doc/unique_words : " + String.format("%.2f", tok/uniqueWords.keySet().size()*100) + " %");
    }


    private static void saveSet(String filename, Map<String, String> words) throws IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false), Charset.forName("UTF-8")));
        Set<String> keys = words.keySet();
        for (String key: keys){
            writer.write(key + "\r\n");
        }
        writer.close();
    }

    private static double calcL(Map<String, String> words){
        Set<String> keys = words.keySet();
        double totalL = 0.0;
        int n = keys.size();
        for (String key: keys){
            totalL += key.length();
        }
        System.out.println("Average valid word length L: " + String.format("%.2f", totalL/n));
        return totalL/n;
    }

    private static double calcAv(List<TEMessage> msgs){
        double totalAv = 0.0;
        int n = msgs.size();
        for (TEMessage msg : msgs) {
            for (TEMessageToken tok: msg.getTokens()){
                totalAv += tok.getCompounds().size();
            }
        }
        System.out.println("Valid tokens per document: " + String.format("%.2f", totalAv/n));
        return totalAv/n;
    }

    private static void printTopWords(TEResults res){
        System.out.println();
        System.out.println();
        System.out.println(res.getParameters().toString());
        for (Map<String, Double> topic: res.getTopTopicalWords()){
           System.out.println();
           System.out.println("------------------------topic----------------------------");
           topic.entrySet()
                   .stream()
                   .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                   .forEachOrdered(x -> System.out.println(x.getKey() + " " + x.getValue()));
            System.out.println("------------------------topic end------------------------");
        }
        System.out.println("----------------------------------------------------------");
    }


}
