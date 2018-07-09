/*
 * Title: MongoInterface.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package storage.db.mongo;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import crawler.output.FileMethods;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.telegram.api.channel.TLChannelParticipants;
import org.telegram.api.channel.participants.*;
import org.telegram.api.chat.*;
import org.telegram.api.chat.channel.TLChannel;
import org.telegram.api.chat.channel.TLChannelForbidden;
import org.telegram.api.chat.channel.TLChannelFull;
import org.telegram.api.chat.invite.*;
import org.telegram.api.chat.participant.chatparticipant.TLAbsChatParticipant;
import org.telegram.api.chat.participant.chatparticipant.TLChatParticipant;
import org.telegram.api.chat.participant.chatparticipant.TLChatParticipantAdmin;
import org.telegram.api.chat.participant.chatparticipant.TLChatParticipantCreator;
import org.telegram.api.chat.participant.chatparticipants.TLChatParticipants;
import org.telegram.api.chat.photo.TLAbsChatPhoto;
import org.telegram.api.chat.photo.TLChatPhoto;
import org.telegram.api.chat.photo.TLChatPhotoEmpty;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.document.TLAbsDocument;
import org.telegram.api.document.TLDocument;
import org.telegram.api.document.TLDocumentEmpty;
import org.telegram.api.document.attribute.*;
import org.telegram.api.file.location.TLAbsFileLocation;
import org.telegram.api.file.location.TLFileLocation;
import org.telegram.api.file.location.TLFileLocationUnavailable;
import org.telegram.api.game.TLGame;
import org.telegram.api.geo.point.TLAbsGeoPoint;
import org.telegram.api.geo.point.TLGeoPoint;
import org.telegram.api.geo.point.TLGeoPointEmpty;
import org.telegram.api.input.chat.TLAbsInputChannel;
import org.telegram.api.input.chat.TLInputChannel;
import org.telegram.api.input.chat.TLInputChannelEmpty;
import org.telegram.api.message.*;
import org.telegram.api.message.action.*;
import org.telegram.api.message.media.*;
import org.telegram.api.messages.TLMessagesChatFull;
import org.telegram.api.paymentapi.TLPaymentCharge;
import org.telegram.api.paymentapi.TLPaymentRequestedInfo;
import org.telegram.api.paymentapi.TLPostAddress;
import org.telegram.api.peer.TLAbsPeer;
import org.telegram.api.peer.TLPeerChannel;
import org.telegram.api.peer.TLPeerChat;
import org.telegram.api.peer.TLPeerUser;
import org.telegram.api.phone.call.discardreason.*;
import org.telegram.api.photo.TLAbsPhoto;
import org.telegram.api.photo.TLPhoto;
import org.telegram.api.photo.TLPhotoEmpty;
import org.telegram.api.photo.size.TLAbsPhotoSize;
import org.telegram.api.photo.size.TLPhotoSize;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.api.user.TLUserEmpty;
import org.telegram.api.user.TLUserFull;
import org.telegram.api.webpage.TLAbsWebPage;
import org.telegram.api.webpage.TLWebPage;
import org.telegram.api.webpage.TLWebPageEmpty;
import org.telegram.tl.TLIntVector;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;
import storage.db.DBStorage;
import topicextractor.structures.TEDialog;
import topicextractor.structures.TEMessage;

import java.io.*;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static storage.db.Constants.*;

/**
 * Class for writing and reading data to and from MongoDB.
 */

public class MongoDBStorage implements DBStorage {

    private String user; // the user name
    private String db; // the name of the db in which the user is defined
    private String psw; // the psw
    private String host; // host
    private Integer port; // port
    private MongoCredential credential; // auth info
    private MongoClientOptions options; // client options
    private MongoClient mongoClient; // client instance
    private MongoDatabase database; // db instance
    private GridFSBucket gridFSBucket; // bucket for files
    private MongoCollection<Document> collection; //collection
    private boolean upsert; // upsert into DB? if false - regular write

    public MongoDBStorage(String user, String db, String psw, String host, Integer port, String gridFSBucketName){
        this.user = user;
        this.db = db;
        this.psw = psw;
        this.host = host;
        this.port = port;
        this.credential = MongoCredential.createCredential(user, db, psw.toCharArray());
        this.options = MongoClientOptions.builder().build();
        this.mongoClient = new MongoClient(new ServerAddress(host, port), credential, options);
        this.database = mongoClient.getDatabase(db);
        this.gridFSBucket = GridFSBuckets.create(this.database, gridFSBucketName);
        this.upsert = false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public MongoCredential getCredential() {
        return credential;
    }

    public void setCredential(MongoCredential credential) {
        this.credential = credential;
    }

    public MongoClientOptions getOptions() {
        return options;
    }

    public void setOptions(MongoClientOptions options) {
        this.options = options;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    public void setGridFSBucket(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public boolean isUpsert() {
        return upsert;
    }

    public void setUpsert(boolean upsert) {
        this.upsert = upsert;
    }

    public void setGridFSBucket(String gridFSBucketName) {
        gridFSBucket = GridFSBuckets.create(database, gridFSBucketName);
    }

    public void setCollection(String collName) {
        collection = database.getCollection(collName);
    }

    /**
     * sets target database
     * @param database target database
     */
    @Override
    public void setDatabase(String database) {
        try {
            this.database = mongoClient.getDatabase(database);
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * place to read/write
     * @param target target collection
     */
    @Override
    public void setTarget(String target) {
        try {
            collection = database.getCollection(target);
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * Drops target collection
     * @param target target collection
     */
    @Override
    public void dropTarget(String target) {
        try{
            database.getCollection(target).drop();
        } catch (MongoException e){
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * Drops current db
     */
    @Override
    public void dropDatabase() {
        try{
            database.drop();
        } catch (MongoException e){
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * writes object to db
     * @param obj object
     */
    @Override
    public void write(Object obj) {
        if (obj != null) {
            if (!isUpsert()) {
                try {
                    collection.insertOne((Document) obj);
                } catch (MongoException e) {
                    System.err.println(e.getCode() + " " + e.getMessage());
                }
            } else {
                try {
                    Document doc = (Document) obj;
                    collection.updateOne(Filters.eq("_id", doc.get("_id")), new Document("$set", doc), new UpdateOptions().upsert(true));
                } catch (MongoException e) {
                    System.err.println(e.getCode() + " " + e.getMessage());
                }

            }
        }
    }

    /**
     * Writes full dialog to db
     * @param dial object with dialog (chat/channel/user)
     * @param chatsHashMap prevents unnecessary downloading by getting data from HashMap
     * @param usersHashMap prevents unnecessary downloading by getting data from HashMap
     */
    @Override
    public void writeFullDialog(TLObject dial, Map<Integer, TLAbsChat> chatsHashMap, Map<Integer, TLAbsUser> usersHashMap){
        // set target
        this.setTarget(DIALOGS);
        // write it
        if (dial instanceof TLMessagesChatFull) {
            TLAbsChatFull absChatFull = ((TLMessagesChatFull) dial).getFullChat();
            //check if chat full or channel full
            if (absChatFull instanceof TLChannelFull) {
                this.write(tlChannelFullToDocument((TLChannelFull) absChatFull));
            } else if (absChatFull instanceof TLChatFull) {
                this.write(tlChatFullToDocument((TLChatFull) absChatFull));
            }
        } else if (dial instanceof TLUserFull) {
            this.write(tlUserFullToDocument((TLUserFull) dial));
        }
    }

    /**
     * writes users hashmap to db
     * @param usersHashMap hashmap
     */
    @Override
    public void writeUsersHashMap(Map<Integer, TLAbsUser> usersHashMap) {
        // set target
        this.setTarget(USERS_COL);
        // write
        Set<Integer> keys = usersHashMap.keySet();
        for (Integer key : keys) {
            TLAbsUser absUser = usersHashMap.get(key);
            this.write(tlAbsUserToDocument(absUser));
        }
    }

    /**
     * writes chats hashmap to db
     * @param chatsHashMap hashmap
     */
    @Override
    public void writeChatsHashMap(Map<Integer, TLAbsChat> chatsHashMap) {
        // set target
        this.setTarget(CHATS_COL);
        // write
        Set<Integer> keys = chatsHashMap.keySet();
        for (Integer key : keys) {
            TLAbsChat absChat = chatsHashMap.get(key);
            this.write(tlAbsChatToDocument(absChat));
        }
    }

    /**
     * writes participants to db
     * @param participants participants
     */
    @Override
    public void writeParticipants(TLObject participants, TLDialog dialog) {
        this.setTarget(PAR_DIAL_PREF + dialog.getPeer().getId());
        if (participants != null){
            if (participants instanceof TLChatParticipants){
                writeChatsParticipants(((TLChatParticipants) participants).getParticipants());
            } else if (participants instanceof TLUserFull){
                this.write(tlUserFullToDocument((TLUserFull) participants));
            } else if ((participants instanceof TLChannelParticipants)){
                if (((TLChannelParticipants) participants).getCount() > 0){
                    writeChannelParticipants(((TLChannelParticipants) participants).getParticipants());
                }
            }
        }
    }

    /**
     * Write messages to DB
     * @param absMessages messages
     * @param dialog dialog
     */
    @Override
    public void writeTLAbsMessages(TLVector<TLAbsMessage> absMessages, TLDialog dialog) {
        this.setTarget(MSG_DIAL_PREF + dialog.getPeer().getId());
        if ((absMessages != null) && (!absMessages.isEmpty())){
            try {
                for (TLAbsMessage absMessage : absMessages) {
                    writeTLAbsMessage(absMessage);
                }
            } catch (MongoException e) {
                System.err.println(e.getCode() + " " + e.getMessage());
            }
        }
    }

    /**
     * Write a single TLAbsMessage to DB
     * @param absMessage abstract message
     */
    @Override
    public void writeTLAbsMessage(TLAbsMessage absMessage){
        if (absMessage instanceof TLMessage){
            this.write(tlMessageToDocument((TLMessage) absMessage));
        } else if (absMessage instanceof TLMessageService){
            this.write(tlMessageServiceToDocument((TLMessageService) absMessage));
        } else if (absMessage instanceof TLMessageEmpty){
            this.write(new Document("class","MessageEmpty")
                    .append("_id",((TLMessageEmpty) absMessage).getId())
                    .append("chatId", absMessage.getChatId()));
        }
    }

    /**
     * Write messages to DB
     * @param absMessage messages
     * @param filePath path to the downloaded (reference)
     */
    @Override
    public void writeTLAbsMessageWithReference(TLAbsMessage absMessage, String filePath) {
        int id = -1;
        if (absMessage instanceof TLMessage){
            this.write(tlMessageToDocumentWithReference((TLMessage) absMessage, filePath));
            id = ((TLMessage) absMessage).getId();
        } else if (absMessage instanceof TLMessageService){
            this.write(tlMessageServiceToDocument((TLMessageService) absMessage));
            id = ((TLMessageService) absMessage).getId();
        } else if (absMessage instanceof TLMessageEmpty){
            this.write(new Document("class","MessageEmpty")
                    .append("_id",((TLMessageEmpty) absMessage).getId())
                    .append("chatId", absMessage.getChatId()));
            id = ((TLMessageEmpty) absMessage).getId();
        }
        System.out.println(id);
    }

    /**
     * max id of the message from a particular chat
     */
    @Override
    public Integer getMessageMaxId(TLDialog dialog){
        try {
            this.setTarget(MSG_DIAL_PREF + dialog.getPeer().getId());
            FindIterable<Document> findMax = collection.find().sort(descending("_id")).limit(1);
            Document docMax = findMax.first();
            return docMax != null ? (Integer) docMax.get("_id") : null;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * min id of the message from a particular chat (for offset)
     */
    @Override
    public Integer getMessageMinId(TLDialog dialog) {
        try {
            this.setTarget(MSG_DIAL_PREF + dialog.getPeer().getId());
            FindIterable<Document> findMin = collection.find().sort(ascending("_id")).limit(1);
            Document docMin = findMin.first();
            return docMin != null ? (Integer) docMin.get("_id") : null;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * date of min id message from a particular chat (for offset)
     */
    @Override
    public Integer getMessageMinIdDate(TLDialog dialog) {
        try {
            this.setTarget(MSG_DIAL_PREF + dialog.getPeer().getId());
            FindIterable<Document> findMin = collection.find().sort(ascending("_id")).limit(1);
            Document docMin = findMin.first();
            return docMin != null ? (Integer) docMin.get("date") : null;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * date of max id message from a particular chat (for offset)
     */
    @Override
    public Integer getMessageMaxIdDate(TLDialog dialog) {
        try {
            this.setTarget(MSG_DIAL_PREF + dialog.getPeer().getId());
            FindIterable<Document> findMax = collection.find().sort(descending("_id")).limit(1);
            Document docMax = findMax.first();
            return docMax != null ? (Integer) docMax.get("date") : null;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * writes bytes to GridFS
     * @param name filename
     * @param bytes contents
     */
    @Override
    public void writeFile(String name, byte[] bytes) {
        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            // file type (last split)
            String[] split = name.split("\\.");
            String type = split[split.length - 1];
            // 100kb chunks
            GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(100 * 1024).metadata(new Document("type", type));
            gridFSBucket.uploadFromStream(name, inputStream, options);
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * creates single field index
     * @param field indexing field
     * @param type switch: 1 - ascending, -1 - descending, default - ascending
     */
    @Override
    public void createIndex(String field, int type) {
        try {
            switch (type) {
                case 1:
                    collection.createIndex(Indexes.ascending(field));
                    break;
                case -1:
                    collection.createIndex(Indexes.descending(field));
                    break;
                default:
                    collection.createIndex(Indexes.ascending(field));
                    break;
            }
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * creates composite index
     * @param fields indexing fields
     * @param types switch: 1 - ascending, -1 - descending, default - ascending
     */
    @Override
    public void createIndex(List<String> fields, List<Integer> types) {
        try {
            // asc and desc indexes
            List<String> asc = new ArrayList<>();
            List<String> desc = new ArrayList<>();
            //check sizes
            if (fields.size() == types.size()){
                // separate desc and asc
                for (int i = 0; i < types.size(); i++){
                    switch (types.get(i)) {
                        case 1:
                            asc.add(fields.get(i)); break;
                        case -1:
                            desc.add(fields.get(i)); break;
                        default:
                            asc.add(fields.get(i)); break;
                    }
                }
                // if only desc is not empty
                if (asc.isEmpty() && (!desc.isEmpty())){
                    collection.createIndex(Indexes.descending(desc));
                }
                // if only asc is not empty
                if (desc.isEmpty() && (!asc.isEmpty())){
                    collection.createIndex(Indexes.ascending(asc));
                }
                // if asc & desc is not empty
                if ((!asc.isEmpty()) && (!desc.isEmpty())) {
                    collection.createIndex(Indexes.compoundIndex(Indexes.ascending(asc), Indexes.descending(desc)));
                }
            } else {
                System.out.println("UNABLE TO CREATE INDEXES: fields and types have different lengths");
            }
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
        }
    }

    /**
     * reads all messages from DB for target collection
     * @param target target collection
     */
    @Override
    public List<TEMessage> readMessages(TEDialog target) {
        try {
            List<TEMessage> msgs = new LinkedList<>();
            this.setTarget(MSG_DIAL_PREF + target.getId());
            FindIterable<Document> docs = collection.find().sort(descending("_id"));
            for (Document doc : docs) {
                msgs.add(TEMessage.topicExtractionMessageFromMongoDocument(doc));
            }
            return msgs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * reads messages between two dates from DB for target collection
     * @param target targetCollectionName collection
     * @param dateFrom start date date
     * @param dateTo end date
     */
    @Override
    public List<TEMessage> readMessages(TEDialog target, int dateFrom, int dateTo) {
        try {
            List<TEMessage> msgs = new LinkedList<>();
            this.setTarget(MSG_DIAL_PREF + target.getId());
            FindIterable<Document> docs = collection
                    .find(and(gte("date", dateFrom), lte("date", dateTo)))
                    .sort(descending("_id"));
            for (Document doc : docs) {
                msgs.add(TEMessage.topicExtractionMessageFromMongoDocument(doc));
            }
            return msgs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns dialogs list from respective collection
     */
    @Override
    public List<TEDialog> getDialogs() {
        try {
            List<TEDialog> dialogs = new ArrayList<>();
            this.setTarget("DIALOGS");
            FindIterable<Document> dials = collection.find();
            for (Document dial : dials) {
                Document info = getPeerInfo((Integer) dial.get("_id"));
                if (info != null){
                    dialogs.add(TEDialog.topicExtractionDialogFromMongoDocument(info));
                }
            }
            return dialogs;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * saves files from DB to HDD
     * @param path HDD path
     */
    @Override
    public void saveFilesToHDD(String path) {
        List<GridFSFile> files = getDBFilesInfo();
        for (GridFSFile file : files) {
            saveFileToHDD(path, file);
        }
    }

    /**
     * saves files from DB to HDD
     * @param path path
     * @param filePointer file id or another pointer
     */
    @Override
    public void saveFileToHDD(String path, Object filePointer) {
        try {
            GridFSFile file = (GridFSFile) filePointer;
            ObjectId oid = file.getObjectId();
            path += File.separator + file.getFilename();
            FileMethods.checkFilePath(path);
            FileOutputStream fos = new FileOutputStream(path);
            gridFSBucket.downloadToStream(oid, fos);
            fos.close();
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            System.out.println("MONGODB ERROR " + ((GridFSFile) filePointer).getFilename());
        } catch (IOException e){
            System.err.println(e.getMessage());
            System.out.println("OUTPUT STREAM ERROR " + ((GridFSFile) filePointer).getFilename());
        }
    }

    /**
     * gets peer info from database
     * @param id id
     */
    public Document getPeerInfo(Integer id) {
        try {
            this.setTarget("CHATS");
            Document peerInfo = collection.find(eq("_id", id)).first();
            if (peerInfo == null) {
                this.setTarget("USERS");
                peerInfo = collection.find(eq("_id", id)).first();
            }
            return peerInfo;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing collections names
     */
    public List<String> getAllCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                colNames.add(collection);
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing message collections names
     */
    public List<String> getMessagesCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                if (collection.startsWith("MESSAGES")) {
                    colNames.add(collection);
                }
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns list of existing participant collections names
     */
    public List<String> getParticipantsCollections() {
        try {
            List<String> colNames = new ArrayList<>();
            MongoIterable<String> collections = database.listCollectionNames();
            for (String collection : collections) {
                if (collection.startsWith("PARTICIPANTS")) {
                    colNames.add(collection);
                }
            }
            return colNames;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * returns names of stored files
     */
    public List<GridFSFile> getDBFilesInfo() {
        try {
            List<GridFSFile> files = new LinkedList<>();
            GridFSFindIterable gfsi = gridFSBucket.find();
            for (GridFSFile gfsf : gfsi) {
                files.add(gfsf);
            }
            return files;
        } catch (MongoException e) {
            System.err.println(e.getCode() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts channel full to document
     * @param cf channel full
     */
    private static Document tlChannelFullToDocument(TLChannelFull cf){
        return new Document("_id", cf.getId())
                .append("class", "ChannelFull")
                .append("about", cf.getAbout())
                .append("adminCount", cf.getAdminCount())
                .append("migratedFromId", cf.getMigratedFromChatId())
                .append("pinnedMessageId", cf.getPinnedMessageId())
                .append("exportedInvite", tlAbsChatInviteToDocument(cf.getExportedInvite()));
    }

    /**
     * Converts chat full to document
     * @param cf chat full
     */
    private static Document tlChatFullToDocument(TLChatFull cf){
        return new Document("_id", cf.getId())
                .append("class", "ChatFull")
                .append("exportedInvite", tlAbsChatInviteToDocument(cf.getExportedInvite()));
    }

    /**
     * Converts abstract chat invite to document
     * @param aci abstract chat invite
     */
    private static Document tlAbsChatInviteToDocument(TLAbsChatInvite aci){
        if (aci instanceof TLChatInvite){
            return new Document("class", "ChatInvite")
                    .append("chatInvite", tlChatInviteToDocument((TLChatInvite) aci));
        } else if (aci instanceof TLChatInviteAlready){
            return new Document("class", "ChatInviteAlready")
                    .append("chat", tlAbsChatToDocument(((TLChatInviteAlready) aci).getChat()));
        } else if (aci instanceof TLChatInviteEmpty){
            return new Document("class", "ChatInviteEmpty");
        } else if (aci instanceof TLChatInviteExported){
            return new Document("class", "ChatInviteExported")
                    .append("link", ((TLChatInviteExported) aci).getLink());
        } else {
            return null;
        }
    }

    /**
     * Converts chat invite to document
     * @param ci chat invite
     */
    private static Document tlChatInviteToDocument(TLChatInvite ci){
        return new Document("title", ci.getTitle())
                .append("participantsCount", ci.getParticipantsCount())
                .append("photo", tlAbsPhotoToDocument(ci.getPhoto()))
                .append("participants", tlVectorTlAbsUserToDocument(ci.getParticipants()));
    }

    /**
     * Converts user full to document
     * @param uf user full
     */
    private static Document tlUserFullToDocument(TLUserFull uf){
        TLAbsUser au = uf.getUser();
        return new Document("_id",au.getId())
                .append("class", "UserFull")
                .append("about", uf.getAbout())
                .append("commonChatsCount", uf.getCommonChatsCount())
                .append("flags",uf.getFlags())
                .append("photo", tlAbsPhotoToDocument(uf.getProfilePhoto()))
                .append("user", tlAbsUserToDocument(au));
    }

    /**
     * Converts abstract user to document
     * @param au abstract user
     */
    private static Document tlAbsUserToDocument(TLAbsUser au){
        if (au instanceof TLUser){
            TLUser u = (TLUser) au;
            return new Document("class", "User")
                    .append("_id", u.getId())
                    .append("accessHash", u.getAccessHash())
                    .append("firstName",u.getFirstName())
                    .append("lastName",u.getLastName())
                    .append("userName",u.getUserName())
                    .append("flags",u.getFlags())
                    .append("langCode",u.getLangCode())
                    .append("phone",u.getPhone())
                    .append("botInfoVersion",u.getBotInfoVersion())
                    .append("botInlinePlaceholder",u.getBotInlinePlaceholder());
        } else if (au instanceof TLUserEmpty){
            TLUserEmpty u = (TLUserEmpty) au;
            return new Document("class", "UserEmpty").append("_id", u.getId());
        } else {
            return null;
        }
    }

    /**
     * Converts vector of abstract users to document
     * @param vau abstract user vector
     */
    private static List<Document> tlVectorTlAbsUserToDocument(TLVector<TLAbsUser> vau){
        List<Document> doc = new ArrayList<>();
        if ((vau != null) && (!vau.isEmpty())){
            for (TLAbsUser au: vau){
                doc.add(tlAbsUserToDocument(au));
            }
        }
        return doc;
    }

    /**
     * Converts an abstract chat to Document
     * @param ac abstract chat
     */
    private static Document tlAbsChatToDocument(TLAbsChat ac){
        if (ac instanceof TLChannel){
            return new Document("class", "Channel")
                    .append("_id", ac.getId())
                    .append("accessHash", ((TLChannel) ac).getAccessHash())
                    .append("date", ((TLChannel) ac).getDate())
                    .append("flags", ((TLChannel) ac).getFlags())
                    .append("title", ((TLChannel) ac).getTitle())
                    .append("username", ((TLChannel) ac).getUsername())
                    .append("version", ((TLChannel) ac).getVersion())
                    .append("photo", tlAbsChatPhotoToDocument(((TLChannel) ac).getPhoto()));
        } else if (ac instanceof TLChannelForbidden){
            return new Document("class", "ChannelForbidden")
                    .append("accessHash", ((TLChannelForbidden) ac).getAccessHash())
                    .append("title", ((TLChannelForbidden) ac).getTitle())
                    .append("_id", ac.getId());
        } else if (ac instanceof TLChat){
            return new Document("class", "Chat")
                    .append("_id", ac.getId())
                    .append("date", ((TLChat) ac).getDate())
                    .append("flags", ((TLChat) ac).getFlags())
                    .append("participantsCount", ((TLChat) ac).getParticipantsCount())
                    .append("title", ((TLChat) ac).getTitle())
                    .append("version", ((TLChat) ac).getVersion())
                    .append("photo", tlAbsChatPhotoToDocument(((TLChat) ac).getPhoto()))
                    .append("migratedTo", tlAbsInputChannelToDocument(((TLChat) ac).getMigratedTo()));
        } else if (ac instanceof TLChatForbidden){
            return new Document("class", "ChatForbidden")
                    .append("title", ((TLChatForbidden) ac).getTitle())
                    .append("_id", ac.getId());
        } else if (ac instanceof TLChatEmpty){
            return new Document("class", "ChatEmpty")
                    .append("_id", ac.getId());
        } else {
            return null;
        }
    }

    /**
     * Converts abstract chat photo to document
     * @param acp abstract chat photo
     */
    private static Document tlAbsChatPhotoToDocument(TLAbsChatPhoto acp){
        if (acp instanceof TLChatPhoto){
            return new Document("class", "ChatPhoto")
                    .append("bigPhoto", tlAbsFileLocationToDocument(((TLChatPhoto) acp).getPhoto_big()))
                    .append("smallPhoto", tlAbsFileLocationToDocument(((TLChatPhoto) acp).getPhoto_small()));
        } else if (acp instanceof TLChatPhotoEmpty){
            return new Document("class", "ChatPhotoEmpty");
        } else {
            return null;
        }
    }

    /**
     * Converts file location to Document
     * @param afl abstract file location
     */
    private static Document tlAbsFileLocationToDocument(TLAbsFileLocation afl){
        if (afl instanceof TLFileLocation){
            return new Document("class", "FileLocation")
                    .append("secret", ((TLFileLocation) afl).getSecret())
                    .append("volumeId",((TLFileLocation) afl).getVolumeId())
                    .append("localId",((TLFileLocation) afl).getLocalId())
                    .append("dcId",((TLFileLocation) afl).getDcId());
        } else if (afl instanceof TLFileLocationUnavailable){
            return new Document("class", "FileLocationUnavailable");
        } else {
            return null;
        }
    }

    /**
     * Converts abstract input channel to document
     * @param aic abstract input channel
     */
    private static Document tlAbsInputChannelToDocument (TLAbsInputChannel aic){
        if (aic instanceof TLInputChannel){
            return new Document("class", "InputChannel")
                    .append("accessHash", ((TLInputChannel) aic).getAccessHash())
                    .append("_id", aic.getChannelId());
        } else if (aic instanceof TLInputChannelEmpty){
            return new Document("class", "InputChannelEmpty")
                    .append("_id",aic.getChannelId());
        } else {
            return null;
        }
    }

    /**
     * converts abs photo to doc
     * @param ap abs photo
     * @return doc
     */
    private static Document tlAbsPhotoToDocument(TLAbsPhoto ap){
        if (ap instanceof TLPhoto){
            return new Document("class", "Photo")
                    .append("_id", ((TLPhoto) ap).getId())
                    .append("accessHash", ((TLPhoto) ap).getAccessHash())
                    .append("date", ((TLPhoto) ap).getDate())
                    .append("location", getLargestPhotoLocation(((TLPhoto) ap).getSizes()));
        } else if (ap instanceof TLPhotoEmpty){
            return new Document("class", "PhotoEmpty")
                    .append("_id", ((TLPhotoEmpty) ap).getId());
        } else {
            return null;
        }
    }

    /**
     * gets the location of the largest (and last one in the list) accessible photo
     * @param apss abs photo size
     * @return doc
     */
    private static Document getLargestPhotoLocation(TLVector<TLAbsPhotoSize> apss){
        // getting the last and largest TLPhotoSize
        Document doc = null;
        TLPhotoSize aps;
        TLFileLocation psl;
        for (int i = apss.size()-1; i >= 0; i--){
            if (sizeAvailable(apss.get(i))){
                aps = (TLPhotoSize) apss.get(i);
                psl = (TLFileLocation) aps.getLocation();
                doc = new Document("class", "PhotoSize")
                        .append("size", aps.getSize())
                        .append("type", aps.getType())
                        .append("location", tlFileLocationToDocument(psl));
                break;
            }
        }
        return doc;
    }

    /**
     * checks if file location available
     * @param aps abstract photo size
     */
    private static boolean sizeAvailable(TLAbsPhotoSize aps){
        boolean f = false;
        if (aps instanceof TLPhotoSize){
            if (((TLPhotoSize) aps).getLocation() instanceof TLFileLocation){
                f = true;
            }
        }
        return f;
    }


    /**
     * converts file location to doc
     * @param fl location
     * @return doc
     */
    private static Document tlFileLocationToDocument(TLFileLocation fl){
        return new Document("class", "FileLocation")
                .append("dcId", fl.getDcId())
                .append("localId", fl.getLocalId())
                .append("volumeId", fl.getVolumeId())
                .append("secret", fl.getSecret());
    }

    /**
     * writes channel participants
     * @param vacp participants vector
     */
    private void writeChannelParticipants(TLVector<TLAbsChannelParticipant> vacp){
        for (TLAbsChannelParticipant acp: vacp){
            this.write(tlAbsChannelParticipantToDocument(acp));
        }
    }

    /**
     * converts participant to document
     * @param acp participant
     */
    private static Document tlAbsChannelParticipantToDocument(TLAbsChannelParticipant acp){
        if (acp instanceof TLChannelParticipant){
            return new Document("class", "ChannelParticipant")
                    .append("_id",((TLChannelParticipant) acp).getUserId())
                    .append("date", ((TLChannelParticipant) acp).getDate());
        } else if (acp instanceof TLChannelParticipantSelf){
            return new Document("class", "ChannelParticipantSelf")
                    .append("_id", ((TLChannelParticipantSelf) acp).getUserId())
                    .append("date", ((TLChannelParticipantSelf) acp).getDate())
                    .append("inviterId", ((TLChannelParticipantSelf) acp).getInviterId());
        } else if (acp instanceof TLChannelParticipantModerator){
            return new Document("class", "ChannelParticipantModerator")
                    .append("_id", ((TLChannelParticipantModerator) acp).getUserId())
                    .append("date", ((TLChannelParticipantModerator) acp).getDate())
                    .append("inviterId", ((TLChannelParticipantModerator) acp).getInviterId());
        } else if (acp instanceof TLChannelParticipantKicked){
            return new Document("class", "ChannelParticipantKicked")
                    .append("_id", ((TLChannelParticipantKicked) acp).getUserId())
                    .append("date", ((TLChannelParticipantKicked) acp).getDate())
                    .append("kickedBy", ((TLChannelParticipantKicked) acp).getKickedBy());
        } else if (acp instanceof TLChannelParticipantEditor){
            return new Document("class", "ChannelParticipantEditor")
                    .append("_id", ((TLChannelParticipantEditor) acp).getUserId())
                    .append("date", ((TLChannelParticipantEditor) acp).getDate())
                    .append("inviterId", ((TLChannelParticipantEditor) acp).getInviterId());
        } else if (acp instanceof TLChannelParticipantCreator){
            return new Document("class", "ChannelParticipantCreator")
                    .append("_id", ((TLChannelParticipantCreator) acp).getUserId());
        } else {
            return null;
        }
    }

    /**
     * writes chat participants
     * @param vacp participants vector
     */
    private void writeChatsParticipants(TLVector<TLAbsChatParticipant> vacp){
        for (TLAbsChatParticipant acp: vacp){
            this.write(tlAbsChatParticipantToDocument(acp));
        }
    }

    /**
     * converts participant to document
     * @param acp participant
     */
    private static Document tlAbsChatParticipantToDocument(TLAbsChatParticipant acp){
        if (acp instanceof TLChatParticipant){
            return new Document("class", "ChatParticipant")
                    .append("_id", acp.getUserId())
                    .append("date", ((TLChatParticipant) acp).getDate())
                    .append("inviterId", ((TLChatParticipant) acp).getInviterId());
        } else if (acp instanceof TLChatParticipantAdmin){
            return new Document("class", "ChatParticipantAdmin")
                    .append("_id", acp.getUserId())
                    .append("date", ((TLChatParticipantAdmin) acp).getDate())
                    .append("inviterId", ((TLChatParticipantAdmin) acp).getInviterId());
        } else if (acp instanceof TLChatParticipantCreator){
            return new Document("class", "ChatParticipantCreator")
                    .append("_id", acp.getUserId());
        } else {
            return null;
        }
    }

    /**
     * converts message to document
     * @param m message
     */
    private static Document tlMessageToDocument(TLMessage m){
        return new Document("_id", m.getId())
                .append("class", "Message")
                .append("flags", m.getFlags())
                .append("fromId", m.getFromId())
                .append("toId", tlAbsPeerToDocument(m.getToId()))
                .append("fwdFrom", tlMsgFwdHeaderToDocument(m.getFwdFrom()))
                .append("viaBotId", m.getViaBotId())
                .append("replyToMsgId", m.getReplyToMsgId())
                .append("date", m.getDate())
                .append("message", m.getMessage())
                .append("media", tlAbsMessageMediaToDocument(m.getMedia()))
                .append("views", m.getViews())
                .append("editDate", m.getEditDate());
    }

    /**
     * converts message to document with reference
     * @param m message
     */
    private static Document tlMessageToDocumentWithReference(TLMessage m, String filePath){
        return new Document("_id", m.getId())
                .append("class", "Message")
                .append("flags", m.getFlags())
                .append("fromId", m.getFromId())
                .append("toId", tlAbsPeerToDocument(m.getToId()))
                .append("fwdFrom", tlMsgFwdHeaderToDocument(m.getFwdFrom()))
                .append("viaBotId", m.getViaBotId())
                .append("replyToMsgId", m.getReplyToMsgId())
                .append("date", m.getDate())
                .append("message", m.getMessage())
                .append("media", tlAbsMessageMediaToDocument(m.getMedia()))
                .append("views", m.getViews())
                .append("editDate", m.getEditDate())
                .append("mediaReference", filePath);
    }

    /**
     * converts abstract peer to doc
     * @param ap abstract peer
     * @return doc
     */
    private static Document tlAbsPeerToDocument(TLAbsPeer ap){
        if (ap instanceof TLPeerUser){
            return new Document("class", "PeerUser")
                    .append("_id", ap.getId());
        } else if (ap instanceof TLPeerChannel){
            return new Document("class", "PeerChannel")
                    .append("_id", ap.getId());
        } else if (ap instanceof TLPeerChat){
            return new Document("class", "PeerChat")
                    .append("_id", ap.getId());
        } else {
            return null;
        }
    }

    /**
     * converts forward header to doc
     * @param fh forward header
     * @return doc
     */
    private static Document tlMsgFwdHeaderToDocument(TLMessageFwdHeader fh){
        if (fh != null) {
            return new Document("class", "MessageFwdHeader")
                    .append("fromId", fh.getFromId())
                    .append("date", fh.getDate())
                    .append("channelId", fh.getChannelId())
                    .append("channelPost", fh.getChannelPost());
        } else{
            return null;
        }
    }

    /**
     * converts media to doc
     * @param amm abstract message media
     * @return doc
     */
    private static Document tlAbsMessageMediaToDocument(TLAbsMessageMedia amm){
        if (amm instanceof TLMessageMediaContact) {
            return new Document("class", "MessageMediaContact")
                    .append("_id", ((TLMessageMediaContact) amm).getUserId())
                    .append("firstName",((TLMessageMediaContact) amm).getFirstName())
                    .append("lastName", ((TLMessageMediaContact) amm).getLastName())
                    .append("phone", ((TLMessageMediaContact) amm).getPhoneNumber());

        } else if (amm instanceof TLMessageMediaDocument) {
            return new Document("class", "MessageMediaDocument")
                    .append("caption", ((TLMessageMediaDocument) amm).getCaption())
                    .append("document", tlAbsDocumentToDocument(((TLMessageMediaDocument) amm).getDocument()));

        } else if (amm instanceof TLMessageMediaEmpty) {
            return new Document("class", "MessageMediaEmpty");

        } else if (amm instanceof TLMessageMediaGame) {
            return new Document("class", "MessageMediaGame")
                    .append("game", tlGameToDocument(((TLMessageMediaGame) amm).getGame()));

        } else if (amm instanceof TLMessageMediaGeo) {
            return new Document("class", "MessageMediaGeo")
                    .append("geo", tlGeoPointToDocument(((TLMessageMediaGeo) amm).getGeo()));

        } else if (amm instanceof TLMessageMediaPhoto) {
            return new Document("class", "MessageMediaPhoto")
                    .append("caption", ((TLMessageMediaPhoto) amm).getCaption())
                    .append("photo", tlAbsPhotoToDocument(((TLMessageMediaPhoto) amm).getPhoto()));

        } else if (amm instanceof TLMessageMediaUnsupported) {
            return new Document("class", "MessageMediaUnsupported");

        } else if (amm instanceof TLMessageMediaVenue) {
            return new Document("class", "MessageMediaVenue")
                    .append("id", ((TLMessageMediaVenue) amm).getVenue_id())
                    .append("address", ((TLMessageMediaVenue) amm).getAddress())
                    .append("provider", ((TLMessageMediaVenue) amm).getProvider())
                    .append("title", ((TLMessageMediaVenue) amm).getTitle())
                    .append("geo", tlGeoPointToDocument(((TLMessageMediaVenue) amm).getGeo()));

        } else if (amm instanceof TLMessageMediaWebPage) {
            return new Document("class", "MessageMediaWebPage")
                    .append("web", (tlWebPageToDocument(((TLMessageMediaWebPage) amm).getWebPage())));

        } else if (amm instanceof TLMessageMediaInvoice) {
            return new Document("class", "MessageMediaInvoice")
                    .append("title", ((TLMessageMediaInvoice) amm).getTitle())
                    .append("amount", ((TLMessageMediaInvoice) amm).getTotalAmount())
                    .append("currency", ((TLMessageMediaInvoice) amm).getCurrency())
                    .append("description", ((TLMessageMediaInvoice) amm).getDescription())
                    .append("msgId", ((TLMessageMediaInvoice) amm).getReceiptMsgId())
                    .append("startParam", ((TLMessageMediaInvoice) amm).getStartParam());
        } else {
            return null;
        }
    }

    /**
     * converts geo point to doc
     * @param agp geo point
     * @return doc
     */
    private static Document tlGeoPointToDocument(TLAbsGeoPoint agp){
        if (agp instanceof TLGeoPoint){
            return new Document("class", "GeoPoint")
                    .append("lat", ((TLGeoPoint) agp).getLat())
                    .append("lon", ((TLGeoPoint) agp).getLon());
        } else if (agp instanceof TLGeoPointEmpty) {
            return new Document("class", "GeoPointEmpty");
        } else {
            return null;
        }
    }

    /**
     * converts abs doc to doc
     * @param ad abs doc
     * @return doc
     */
    private static Document tlAbsDocumentToDocument(TLAbsDocument ad){
        if (ad instanceof TLDocument){
            return new Document("class", "Document")
                    .append("_id", ad.getId())
                    .append("accessHash", ((TLDocument) ad).getAccessHash())
                    .append("dcId", ((TLDocument) ad).getDcId())
                    .append("date", ((TLDocument) ad).getDate())
                    .append("mimeType", ((TLDocument) ad).getMimeType())
                    .append("size", ((TLDocument) ad).getSize())
                    .append("version", ((TLDocument) ad).getVersion())
                    // only file name & no thumb
                    .append("filename", tlAbsDocumentAttributesToName(((TLDocument) ad).getAttributes(), (TLDocument) ad));
        } else if (ad instanceof TLDocumentEmpty) {
            return new Document("class", "DocumentEmpty")
                    .append("_id", ad.getId());
        } else {
            return null;
        }
    }

    /**
     * converts doc attrs to name of the doc
     */
    private static String tlAbsDocumentAttributesToName(TLVector<TLAbsDocumentAttribute> adas, TLDocument doc) {
        String name = "";
        for (TLAbsDocumentAttribute ada: adas) {
            if (ada instanceof TLDocumentAttributeFilename) {
                name = ((TLDocumentAttributeFilename) ada).getFileName();
            }
        }
        if (name.isEmpty()) {
            name = doc.getId()+"_document";
            for (TLAbsDocumentAttribute attr : adas) {
                if (attr instanceof TLDocumentAttributeAudio) {
                    name = doc.getDate()+"_"+doc.getId()+".ogg"; // audio message
                    return name;
                } else if (attr instanceof TLDocumentAttributeVideo) {
                    name = doc.getDate()+"_"+doc.getId()+".mp4"; // video message
                    return name;
                } else if (attr instanceof TLDocumentAttributeAnimated) {
                    name = doc.getDate()+"_"+doc.getId()+".gif"; // //gif
                    return name;
                } else if (attr instanceof TLDocumentAttributeSticker) {
                    name = doc.getDate()+"_"+doc.getId()+".webp";
                    return name;
                }
            }
        }
        return name;
    }

    /**
     * converts game to doc
     * @param g game
     * @return doc
     */
    private static Document tlGameToDocument(TLGame g){
        return new Document("class", "Game")
                .append("_id", g.getId())
                .append("name", g.getShortName())
                .append("title", g.getTitle())
                .append("accessHash", g.getAccessHash())
                .append("description", g.getDescription())
                .append("document", tlAbsDocumentToDocument(g.getDocument()));
    }

    /**
     * converts web page to document
     * @param wp web page
     */
    private static Document tlWebPageToDocument(TLAbsWebPage wp){
        if (wp instanceof TLWebPage){
            return new Document("class", "WebPage")
                    .append("title", ((TLWebPage) wp).getTitle())
                    .append("url", ((TLWebPage) wp).getUrl())
                    .append("site", ((TLWebPage) wp).getSite_name())
                    .append("_id", ((TLWebPage) wp).getId())
                    .append("description", ((TLWebPage) wp).getDescription())
                    .append("author", ((TLWebPage) wp).getAuthor())
                    .append("duration", ((TLWebPage) wp).getDuration())
                    .append("type", ((TLWebPage) wp).getType())
                    .append("photo", tlAbsPhotoToDocument(((TLWebPage) wp).getPhoto()))
                    .append("document", tlAbsDocumentToDocument(((TLWebPage) wp).getDocument()))
                    .append("hash", ((TLWebPage) wp).getHash());
        } else if (wp instanceof TLWebPageEmpty) {
            return new Document("class", "WebPageEmpty");
        } else {
            return null;
        }
    }

    /**
     * converts service message to document
     * @param ms message service
     */
    private static Document tlMessageServiceToDocument(TLMessageService ms){
        return new Document("class", "MessageService")
                .append("_id", ms.getId())
                .append("date", ms.getDate())
                .append("chatId", ms.getChatId())
                .append("flags", ms.getFlags())
                .append("fromId", ms.getFromId())
                .append("toId", tlAbsPeerToDocument(ms.getToId()))
                .append("replyToMsgId", ms.getReplyToMessageId())
                .append("action", tlAbsMessageActionToDocument(ms.getAction()));
    }

    private static Document tlAbsMessageActionToDocument(TLAbsMessageAction ama){
        if (ama instanceof TLMessageActionEmpty){
            return new Document("class", "MessageActionEmpty");
        } else if (ama instanceof TLMessageActionChannelCreate){
            return new Document("class", "MessageActionChannelCreate")
                    .append("title", ((TLMessageActionChannelCreate) ama).getTitle());
        } else if (ama instanceof TLMessageActionChannelMigratedFrom){
            return new Document("class", "MessageActionChannelMigratedFrom")
                    .append("title",((TLMessageActionChannelMigratedFrom) ama).getTitle())
                    .append("chatId",((TLMessageActionChannelMigratedFrom) ama).getChatId());
        } else if (ama instanceof TLMessageActionChatAddUser){
            return new Document("class", "MessageActionChatAddUser")
                    .append("users", tlIntVectorToList(((TLMessageActionChatAddUser) ama).getUsers()));
        } else if (ama instanceof TLMessageActionChatCreate){
            return new Document("class", "MessageAction")
                    .append("title",((TLMessageActionChatCreate) ama).getTitle())
                    .append("users", tlIntVectorToList(((TLMessageActionChatCreate) ama).getUsers()));
        } else if (ama instanceof TLMessageActionChatDeletePhoto){
            return new Document("class", "MessageActionChatDeletePhoto");
        } else if (ama instanceof TLMessageActionChatDeleteUser){
            return new Document("class", "MessageActionChatDeleteUser")
                    .append("user",((TLMessageActionChatDeleteUser) ama).getUserId());
        } else if (ama instanceof TLMessageActionChatEditPhoto){
            return new Document("class", "MessageActionChatEditPhoto")
                    .append("photo",tlAbsPhotoToDocument(((TLMessageActionChatEditPhoto) ama).getPhoto()));
        } else if (ama instanceof TLMessageActionChatEditTitle){
            return new Document("class", "MessageActionChatEditTitle")
                    .append("title",((TLMessageActionChatEditTitle) ama).getTitle());
        } else if (ama instanceof TLMessageActionChatJoinedByLink){
            return new Document("class", "MessageActionChatJoinedByLink")
                    .append("inviterId",((TLMessageActionChatJoinedByLink) ama).getInviterId());
        } else if (ama instanceof TLMessageActionGameScore){
            return new Document("class", "MessageActionGameScore")
                    .append("game",((TLMessageActionGameScore) ama).getGameId())
                    .append("score",((TLMessageActionGameScore) ama).getScore());
        } else if (ama instanceof TLMessageActionHistoryClear){
            return new Document("class", "MessageActionHistoryClear");
        } else if (ama instanceof TLMessageActionMigrateTo){
            return new Document("class", "MessageActionMigrateTo")
                    .append("channelId",((TLMessageActionMigrateTo) ama).getChannelId());
        } else if (ama instanceof TLMessageActionPaymentSent){
            return new Document("class", "MessageActionPaymentSent")
                    .append("totalAmount",((TLMessageActionPaymentSent) ama).getTotalAmount())
                    .append("currency", ((TLMessageActionPaymentSent) ama).getCurrency());
        } else if (ama instanceof TLMessageActionPaymentSentMe){
            return new Document("class", "MessageActionPaymentSentMe")
                    .append("totalAmount",((TLMessageActionPaymentSentMe) ama).getTotalAmount())
                    .append("currency", ((TLMessageActionPaymentSentMe) ama).getCurrency())
                    .append("ShippingOptionId",((TLMessageActionPaymentSentMe) ama).getShippingOptionId())
                    .append("payload",((TLMessageActionPaymentSentMe) ama).getPayload().getData())
                    .append("charge", tlPaymentChargeToDocument(((TLMessageActionPaymentSentMe) ama).getCharge()))
                    .append("info",tlPaymentRequestedInfoToDocument(((TLMessageActionPaymentSentMe) ama).getInfo()));
        } else if (ama instanceof TLMessageActionPhoneCall){
            return new Document("class", "MessageAction")
                    .append("callId",((TLMessageActionPhoneCall) ama).getCallId())
                    .append("duration",((TLMessageActionPhoneCall) ama).getDuration())
                    .append("flags",((TLMessageActionPhoneCall) ama).getFlags())
                    .append("reason", tlAbsPhoneCallDiscardReasonToDocument(((TLMessageActionPhoneCall) ama).getReason()));
        } else if (ama instanceof TLMessageActionPinMessage){
            return new Document("class", "MessageAction");
        } else{
            return null;
        }
    }

    /**
     * converts int vector to list
     * @param iv int vector
     */
    private static List<Integer> tlIntVectorToList(TLIntVector iv){
        List<Integer> list = new ArrayList<>();
        if ((iv != null) && (!iv.isEmpty())) {
            list.addAll(iv);
        }
        return list;
    }

    /**
     * converts payment charge to document
     * @param pc payment charge
     */
    private static Document tlPaymentChargeToDocument(TLPaymentCharge pc){
        return new Document("class", "PaymentCharge")
                .append("_id",pc.getId())
                .append("providerChargeId", pc.getProviderChargeId());
    }

    /**
     * converts pri to document
     * @param pri payment info
     */
    private static Document tlPaymentRequestedInfoToDocument(TLPaymentRequestedInfo pri){
        return new Document("class", "PaymentRequestedInfo")
                .append("email", pri.getEmail())
                .append("name", pri.getName())
                .append("phone", pri.getPhone())
                .append("shippingAddress", tlPostAddressToDocument(pri.getShippingAddress()));
    }

    /**
     * converts post address to document
     * @param pa post address
     */
    private static Document tlPostAddressToDocument(TLPostAddress pa){
        return new Document("class", "PostAddress")
                .append("city", pa.getCity())
                .append("countryIso2", pa.getCountryIso2())
                .append("postCode", pa.getPostCode())
                .append("state", pa.getState())
                .append("streetLine1", pa.getStreetLine1())
                .append("streetLine2", pa.getStreetLine2());
    }

    /**
     * convetrs apcdr to document
     * @param apcdr phone call discard reason
     */
    private static Document tlAbsPhoneCallDiscardReasonToDocument(TLAbsPhoneCallDiscardReason apcdr){
        if (apcdr instanceof TLPhoneCallDiscardReasonBusy){
            return new Document("class", "PhoneCallDiscardReasonBusy");
        } else if (apcdr instanceof TLPhoneCallDiscardReasonDisconnect){
            return new Document("class", "PhoneCallDiscardReasonDisconnect");
        } else if (apcdr instanceof TLPhoneCallDiscardReasonHangup){
            return new Document("class", "PhoneCallDiscardReasonHangup");
        } else if (apcdr instanceof TLPhoneCallDiscardReasonMissed){
            return new Document("class", "PhoneCallDiscardReasonMissed");
        } else {
            return null;
        }
    }

}
