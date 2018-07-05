/*
 * Title: testdb.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */


import storage.db.mongo.MongoDBStorage;
import org.bson.Document;

import java.util.*;

public class testdbwrite {

    static String user = "telegramJ"; // the user name
    static String db = "telegram"; // the name of the db in which the user is defined
    static String psw = "cart"; // the psw

    public static void main(String[] args) {

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        MongoDBStorage mongo = new MongoDBStorage("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        //HashMap<Integer, List<MessageDoc>> hm = mongo.dbReadMessageDocsHashMap();


        List<Document> dl = new ArrayList<>();
        dl.add(new Document("s", 1).append("1", "s"));
        dl.add(new Document("a", 2).append("2","a"));
        dl.add(new Document("q", 3).append("3","q"));


        /*
        Document doc = new Document("_id", 1000000)
                .append("type", "database")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102))
                .append("arr", Arrays.asList(new Document("x", 200).append("y", 100), new Document("x", 201).append("y", 101), new Document("x", 202).append("y", 102)))
                .append("afl", dl);

        mongo.write(doc);
        */

        mongo.setTarget("testColl");

        //mongo.dropTarget("testColl");
        //mongo.dropDatabase();

        /*
        collection.createIndex(Indexes.ascending("name"));

        MongoIterable<String> collections = database.listCollectionNames();
        for (String collecion: collections){
            System.out.println(collecion);
        }



        Document doc = new Document("_id", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));

        collection.insertOne(doc);

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        };

        collection.find().forEach(printBlock);
        */


        System.out.println("done");

    }

}
