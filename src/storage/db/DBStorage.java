/*
 * Title: DBStorage.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package storage.db;

import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;
import topicextractor.structures.TEDialog;
import topicextractor.structures.message.TEMessage;

import java.util.List;
import java.util.Map;

public interface DBStorage {

    /**
     * Sets target of writing or reading (table, collection, etc.) in db
     * @param target target's name
     */
    void setTarget(String target);

    /**
     * Drops target table, collection, etc. in db
     * @param target target's name
     */
    void dropTarget(String target);

    /**
     * Sets current db
     * @param database db name
     */
    void setDatabase(String database);

    /**
     * Drops current db
     */
    void dropDatabase();

    /**
     * write object to db
     * @param obj object
     */
    void write(Object obj);

    /**
     * writes full dialog to db
     * @param dial dialog
     * @param chatsHashMap map of chats
     * @param usersHashMap map of users
     */
    void writeFullDialog(TLObject dial, Map<Integer, TLAbsChat> chatsHashMap, Map<Integer, TLAbsUser> usersHashMap);

    /**
     * writes users hashmap to db
     * @param usersHashMap maf of users
     */
    void writeUsersHashMap(Map<Integer, TLAbsUser> usersHashMap);

    /**
     * writes chats hashmap to db
     * @param chatsHashMap map of chats
     */
    void writeChatsHashMap(Map<Integer, TLAbsChat> chatsHashMap);

    /**
     * writes participants
     * @param participants participants
     * @param dialog dialog
     */
    void writeParticipants(TLObject participants, TLDialog dialog);

    /**
     * Writes messages from dialogs to DB (each dialog to a single collection)
     * @param absMessages messages
     * @param dialog dialog
     */
    void writeTLAbsMessages(TLVector<TLAbsMessage> absMessages, TLDialog dialog);

    /**
     * Write a single TLAbsMessage to DB
     * @param absMessage message
     */
    void writeTLAbsMessage(TLAbsMessage absMessage);

    /**
     * Writes messages from dialogs to DB (each dialog to a single collection) with reference to the saved file
     * @param absMessage message
     * @param filePath file reference
     */
    void writeTLAbsMessageWithReference(TLAbsMessage absMessage, String filePath);

    /**
     * max id of the message from a particular chat
     * @param dialog dialog
     */
    Integer getMessageMaxId(TLDialog dialog);

    /**
     * min id of the message from a particular chat
     * @param dialog dialog
     */
    Integer getMessageMinId(TLDialog dialog);

    /**
     * date of min id message from a particular chat
     * @param dialog dialog
     */
    Integer getMessageMinIdDate(TLDialog dialog);

    /**
     * date of max id message from a particular chat
     * @param dialog dialog
     */
    Integer getMessageMaxIdDate(TLDialog dialog);

    /**
     * writes bytes to GridFS
     * @param name filename
     * @param bytes bytes
     */
    void writeFile(String name, byte[] bytes);

    /**
     * creates single field index
     * @param field indexing field
     * @param type switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(String field, int type);

    /**
     * creates composite index
     * @param fields indexing fields
     * @param types switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(List<String> fields, List<Integer> types);

    /**
     * reads all messages from DB
     * @param target target dialog
     */
    List<TEMessage> readMessages(TEDialog target);

    /**
     * reads messages between two dates from DB
     * @param target target dialog
     * @param dateFrom date from
     * @param dateTo date to
     */
    List<TEMessage> readMessages(TEDialog target, int dateFrom, int dateTo);

    /**
     * returns dialogs list
     */
    List<TEDialog> getDialogs();

    /**
     * saves files from DB to HDD
     * @param path path
     */
    void saveFilesToHDD(String path);

    /**
     * saves file from DB to HDD
     * @param path path
     * @param filePointer file id or another pointer
     */
    void saveFileToHDD(String path, Object filePointer);

}
