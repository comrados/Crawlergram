/*
 * Title: TipicExtractionDialog.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package topicextractor.structures;

import org.bson.Document;

public class TEDialog {

    private Integer id;
    private String type;
    private Long accessHash;
    private String username;
    private Integer flags;

    public TEDialog(Integer id, String type, Long accessHash, String username, Integer flags){
        this.id = id;
        this.type = type;
        this.accessHash = accessHash;
        this.username = username;
        this.flags = flags;
    }

    public TEDialog(){
        this.id = 0;
        this.type = "";
        this.accessHash = 0L;
        this.username = "";
        this.flags = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAccessHash() {
        return accessHash;
    }

    public void setAccessHash(Long accessHash) {
        this.accessHash = accessHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    /**
     * converts data from DB to topic extraction dialog
     * @param info info from CHATS or USERS collections
     */
    public static TEDialog topicExtractionDialogFromMongoDocument(Document info){
        Integer id = (Integer) info.get("_id");
        String type = (String) info.get("class");
        Integer flags = (Integer) info.get("flags");
        Long accessHash = 0L;
        if (type.equals("User") || type.equals("Channel")){
            accessHash = (Long) info.get("accessHash");
        }
        String username = "";
        if (type.equals("Chat") || type.equals("Channel")){
            username = (String) info.get("title");
        } else if (type.equals("User")){
            username = info.get("userName") + " " + info.get("firstName") + " " + info.get("lastName");
        }
        return new TEDialog(id, type, accessHash, username, flags);
    }
}
