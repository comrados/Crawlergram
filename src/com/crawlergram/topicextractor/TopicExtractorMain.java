/*
 * Title: TopicExtractorMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.topicextractor;

import com.crawlergram.db.DBStorage;
import com.crawlergram.db.mongo.MongoDBStorage;
import com.crawlergram.topicextractor.extractormethods.TopicExtractionMethods;

public class TopicExtractorMain {

    public static void main(String[] args) {

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorage dbStorage = new MongoDBStorage("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        //
        TopicExtractionMethods.getTopicsForAllDialogs(dbStorage, 0, 0, 200, false);

        //TODO read from DB, calculate time intervals, create docs, perform topic extraction

    }

}
