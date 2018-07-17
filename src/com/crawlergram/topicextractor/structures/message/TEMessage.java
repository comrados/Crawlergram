/*
 * Title: TopicExtractionMessage.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.topicextractor.structures.message;

import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public class TEMessage {

    private Integer id;
    private String text;
    private String stemmedText;
    private Integer date;
    private List<TEMessageToken> tokens;

    public TEMessage(){
        this.id = 0;
        this.text = "";
        this.stemmedText = null;
        this.date = 0;
        this.tokens = new LinkedList<>();
    }

    public TEMessage(Integer id, String text, Integer date){
        this.id = id;
        this.text = text;
        this.stemmedText = null;
        this.date = date;
        this.tokens = new LinkedList<>();
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

    public List<TEMessageToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<TEMessageToken> tokens) {
        this.tokens = tokens;
    }

    /**
     * Converts mongoDB's document to TEM (extracts text of message or media's caption)
     * Strings are set converted to lowercase
     * @param doc document
     */
    public static TEMessage topicExtractionMessageFromMongoDocument(Document doc){
        if (doc.get("class").equals("Message")){
            Integer id = (Integer) doc.get("_id");
            Integer date = (Integer) doc.get("date");
            String text = ((String) doc.get("message")).toLowerCase();
            if (text.isEmpty()){
                text = getMediaCaption((Document) doc.get("media"));
            }
            return new TEMessage(id, text, date);
        } else {
            return new TEMessage();
        }
    }

    /**
     * gets media's caption or description/title
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
