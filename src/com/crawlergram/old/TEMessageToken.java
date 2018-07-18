/*
 * Title: TEMessageTokens.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.old;

import java.util.LinkedList;
import java.util.List;

public class TEMessageToken {

    private List<String> tokens;
    private List<String> stems;

    public TEMessageToken(){
        this.tokens = new LinkedList<>();
        this.stems = new LinkedList<>();
    }

    public TEMessageToken(List<String> tokens, List<String> compound){
        this.tokens = tokens;
        this.stems = compound;
    }

    public TEMessageToken(List<String> tokens){
        this.tokens = tokens;
        this.stems = new LinkedList<>();
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getStems() {
        return stems;
    }

    public void setStems(List<String> stems) {
        this.stems = stems;
    }

}
