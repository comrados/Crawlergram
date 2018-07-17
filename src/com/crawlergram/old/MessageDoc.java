/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.old;

/**
 * MessageDoc is a single short message, channel message or set of short messages which were written during some short time interval.
 */

public class MessageDoc {

    Integer id;
    Integer date;
    String text;

    public MessageDoc(){
        this.id = null;
        this.date = null;
        this.text = null;
    }

    public MessageDoc(Integer id, Integer date, String text){
        this.id = id;
        this.date = date;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
