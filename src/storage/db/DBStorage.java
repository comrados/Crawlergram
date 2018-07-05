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
import topicextractor.structures.TEMessage;

import java.util.HashMap;
import java.util.List;

public interface DBStorage {

    /**
     * Sets target of writing or reading (table, collection, etc.) in db
     * @param target
     */
    void setTarget(String target);

    /**
     * Drops target table, collection, etc. in db
     * @param target
     */
    void dropTarget(String target);

    /**
     * Sets current db
     * @param database
     */
    void setDatabase(String database);

    /**
     * Drops current db
     */
    void dropDatabase();

    /**
     * write object to db
     * @param obj
     */
    void write(Object obj);

    /**
     * writes full dialog to db
     * @param dial
     * @param chatsHashMap
     * @param usersHashMap
     */
    void writeFullDialog(TLObject dial, HashMap<Integer, TLAbsChat> chatsHashMap, HashMap<Integer, TLAbsUser> usersHashMap);

    /**
     * writes users hashmap to db
     * @param usersHashMap
     */
    void writeUsersHashMap(HashMap<Integer, TLAbsUser> usersHashMap);

    /**
     * writes chats hashmap to db
     * @param chatsHashMap
     */
    void writeChatsHashMap(HashMap<Integer, TLAbsChat> chatsHashMap);

    /**
     * writes participants
     * @param participants
     * @param dialog
     */
    void writeParticipants(TLObject participants, TLDialog dialog);

    /**
     * Writes messages from dialogs to DB (each dialog to a single collection)
     * @param absMessages
     * @param dialog
     */
    void writeTLAbsMessages(TLVector<TLAbsMessage> absMessages, TLDialog dialog);

    /**
     * Write a single TLAbsMessage to DB
     * @param absMessage
     */
    void writeTLAbsMessage(TLAbsMessage absMessage);

    /**
     * Writes messages from dialogs to DB (each dialog to a single collection) with reference to the saved file
     * @param absMessage
     * @param filePath
     */
    void writeTLAbsMessageWithReference(TLAbsMessage absMessage, String filePath);

    /**
     * max id of the message from a particular chat
     * @param dialog
     */
    Integer getMessageMaxId(TLDialog dialog);

    /**
     * min id of the message from a particular chat
     * @param dialog
     */
    Integer getMessageMinId(TLDialog dialog);

    /**
     * date of min id message from a particular chat
     * @param dialog
     */
    Integer getMessageMinIdDate(TLDialog dialog);

    /**
     * date of max id message from a particular chat
     * @param dialog
     */
    Integer getMessageMaxIdDate(TLDialog dialog);

    /**
     * writes bytes to GridFS
     * @param name
     * @param bytes
     */
    void writeFile(String name, byte[] bytes);

    /**
     * creates single field index
     * @param field
     * @param type switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(String field, int type);

    /**
     * creates composite index
     * @param fields
     * @param types switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(List<String> fields, List<Integer> types);

    /**
     * reads all messages from DB
     * @param target
     */
    List<TEMessage> readMessages(TEDialog target);

    /**
     * reads messages between two dates from DB
     * @param target
     * @param dateFrom
     * @param dateTo
     */
    List<TEMessage> readMessages(TEDialog target, int dateFrom, int dateTo);

    /**
     * returns dialogs list
     */
    List<TEDialog> getDialogs();

    /**
     * saves files from DB to HDD
     * @param path
     */
    void saveFilesToHDD(String path);

    /**
     * saves file from DB to HDD
     * @param path path
     * @param filePointer file id or another pointer
     */
    void saveFileToHDD(String path, Object filePointer);

}
