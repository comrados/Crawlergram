/*
 * Title: TopicExtractorMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor;

import com.crawlergram.db.DBStorage;
import com.crawlergram.db.mongo.MongoDBStorage;
import com.crawlergram.topicextractor.extractormethods.TopicExtractionMethods;
import com.crawlergram.topicextractor.liga.LIGA;

import java.io.File;

public class TopicExtractorMain {

    public static void main(String[] args) {

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorage dbStorage = new MongoDBStorage("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        // language identification model (loaded only once)
        String ligaModel = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA liga = new LIGA().setLogLIGA(false).setMaxSearchDepth(5000).setThreshold(0.0125).setN(3).loadModel(ligaModel);

        // do topic extraction
        TopicExtractionMethods.getTopicsForAllDialogs(dbStorage, 0, 0, 200, false, liga);

        // drops model to save memory
        liga.dropModel();

        //TODO read from DB, calculate time intervals, create docs, perform topic extraction

    }

}
