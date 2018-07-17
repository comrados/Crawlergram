/*
 * Title: TopicExtractionMessageComparator.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.topicextractor.structures.message;

import java.util.Comparator;

public class TEMessageComparator implements Comparator<TEMessage> {
    @Override
    public int compare(TEMessage d1, TEMessage d2) {
        //if dates are equal - compare IDs
        return d2.getDate().compareTo(d1.getDate()) == 0 ? d2.getId().compareTo(d1.getId()) : d2.getDate().compareTo(d1.getDate());
    }
}
