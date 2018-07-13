/*
 * Title: testdbread.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

import com.mongodb.client.gridfs.model.GridFSFile;
import storage.db.mongo.MongoDBStorage;
import topicextractor.structures.TEDialog;
import topicextractor.structures.message.TEMessage;
import org.bson.Document;

import java.util.List;

public class testdbread {

    static String user = "telegramJ"; // the user name
    static String db = "telegram"; // the name of the db in which the user is defined
    static String psw = "cart"; // the psw

    public static void main(String[] args) {
        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        MongoDBStorage mongo = new MongoDBStorage("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        List<TEDialog> dialogs = mongo.getDialogs();
        for (TEDialog dialog: dialogs){ //1528134100, 1528634100
            List<TEMessage> msgs = mongo.readMessages(dialog);
            System.out.println(msgs.size());
        }

        List<String> allCollections = mongo.getAllCollections();
        List<String> msgCollections = mongo.getMessagesCollections();
        Document peerInfo = mongo.getPeerInfo(777000);

        List<GridFSFile> files = mongo.getDBFilesInfo();
        mongo.saveFilesToHDD("dbFiles");

        System.out.println("done");
    }
}
