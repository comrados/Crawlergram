/*
 * Title: TopicExtractionMessage.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.structures.message;

import org.apache.commons.lang3.tuple.MutablePair;
import org.bson.Document;

import java.util.*;

public class TEMessage {

    private Integer id;
    private String text;
    private String stemmedText;
    private Integer date;
    private List<String> tokens;
    private Map<String, Double> langs;

    public TEMessage() {
        this.id = 0;
        this.text = "";
        this.stemmedText = null;
        this.date = 0;
        this.tokens = new ArrayList<>();
        this.langs = new TreeMap<>();
    }

    public TEMessage(Integer id, String text, Integer date) {
        this.id = id;
        this.text = text;
        this.stemmedText = null;
        this.date = date;
        this.tokens = new ArrayList<>();
        this.langs = new TreeMap<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStemmedText() {
        return stemmedText;
    }

    public void setStemmedText(String stemmedText) {
        this.stemmedText = stemmedText;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public Map<String, Double> getLangs() {
        return langs;
    }

    public void setLangs(Map<String, Double> langs) {
        this.langs = langs;
    }

    /**
     * Converts tokens back to the text ("clear" text)
     */
    public String getClearText() {
        StringBuilder text = new StringBuilder();
        for (String token: tokens)
            text.append(token).append(" ");
        return text.toString().trim();
    }

    public String getBestLang() {
        Double bestScore = -1.0;
        String bestLang = "UNKNOWN";
        // Get the best score or return unknown
        for (Map.Entry<String, Double> score : langs.entrySet()){
            if (score.getValue() > bestScore) {
                bestScore = score.getValue();
                bestLang = score.getKey();
            }
        }
        return bestLang;
    }

    /**
     * Converts mongoDB's document to TEM (extracts text of message or media's caption)
     * Strings are set converted to lowercase
     *
     * @param doc document
     */
    public static TEMessage topicExtractionMessageFromMongoDocument(Document doc) {
        if (doc.get("class").equals("Message")) {
            Integer id = (Integer) doc.get("_id");
            Integer date = (Integer) doc.get("date");
            String text = ((String) doc.get("message")).toLowerCase();
            if (text.isEmpty()) {
                text = getMediaCaption((Document) doc.get("media"));
            }
            return new TEMessage(id, text, date);
        } else {
            return new TEMessage();
        }
    }

    /**
     * gets media's caption or description/title
     *
     * @param doc document
     */
    private static String getMediaCaption(Document doc) {
        if (doc != null) {
            if (doc.get("class").equals("MessageMediaDocument")) {
                return (String) doc.get("caption");
            } else if (doc.get("class").equals("MessageMediaPhoto")) {
                return (String) doc.get("caption");
            } else if (doc.get("class").equals("MessageMediaVenue")) {
                return (String) doc.get("title");
            } else if (doc.get("class").equals("MessageMediaInvoice")) {
                return (String) doc.get("title") + doc.get("description");
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

}
