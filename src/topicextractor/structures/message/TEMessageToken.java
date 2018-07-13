/*
 * Title: TEMessageTokens.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package topicextractor.structures.message;

import java.util.LinkedList;
import java.util.List;

public class TEMessageToken {

    private String simple;
    private List<String> compounds;

    public TEMessageToken(){
        this.simple = "";
        this.compounds = new LinkedList<>();
    }

    public TEMessageToken(String simple, List<String> compound){
        this.simple = simple;
        this.compounds = compound;
    }

    public TEMessageToken(String simple){
        this.simple = simple;
        this.compounds = new LinkedList<>();
    }

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public List<String> getCompounds() {
        return compounds;
    }

    public void setCompounds(List<String> compounds) {
        this.compounds = compounds;
    }

}
