/*
 * Title: TEMessageTokens.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package topicextractor.structures;

import java.util.LinkedList;
import java.util.List;

public class TEMessageToken {

    private String simple;
    private List<String> compound;

    public TEMessageToken(){
        this.simple = "";
        this.compound = new LinkedList<>();
    }

    public TEMessageToken(String simple, List<String> compound){
        this.simple = simple;
        this.compound = compound;
    }

    public TEMessageToken(String simple){
        this.simple = simple;
        this.compound = new LinkedList<>();
    }

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public List<String> getCompound() {
        return compound;
    }

    public void setCompound(List<String> compound) {
        this.compound = compound;
    }

}
