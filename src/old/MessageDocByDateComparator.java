/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package old;

import java.util.Comparator;

/**
 * This comparator sorts the Documents by the date. If the dates are equal it sorts, additionally by id of the MessageDoc.
 */

public class MessageDocByDateComparator implements Comparator<MessageDoc> {

    @Override
    public int compare(MessageDoc d2, MessageDoc d1) {
        //if dates are equal - compare IDs
        return d1.date.compareTo(d2.date) == 0 ? d1.id.compareTo(d2.id) : d1.date.compareTo(d2.date);
    }

}
