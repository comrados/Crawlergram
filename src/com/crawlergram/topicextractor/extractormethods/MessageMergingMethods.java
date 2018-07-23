/*
 * Title: MessageMergingMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.extractormethods;

import com.crawlergram.topicextractor.gaussnewton.ExpRegMethods;
import com.crawlergram.topicextractor.gaussnewton.GaussNewton;
import com.crawlergram.topicextractor.gaussnewton.NoSquareException;
import com.crawlergram.topicextractor.structures.TEDialog;
import com.crawlergram.topicextractor.structures.message.TEMessage;
import com.crawlergram.topicextractor.structures.message.TEMessageComparator;

import java.util.*;

public class MessageMergingMethods {

    /**
     * Merges short messages of supergroups and chats to larger messages (merges by topic).
     * We assume that topic can be defined from time of respond.
     * If two messages were written in a short time interval they are likely to have the same context or topic.
     * Such messages can be used to define most popular topics of the chat (e.g. sport, news, etc.).
     * We need to merge only messages from Chats, supergroups (type of Channels) and Users.
     * @param dialog dialog
     * @param msgs original messages list
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     */
    public static List<TEMessage> mergeMessages(TEDialog dialog,
                                                List<TEMessage> msgs,
                                                int docThreshold){
        // merging if chat, supergroup or user (placer, where subscribers write something)
        // channels (not supergroups) usually are blogs, subscribers can't post there
        // if flags' 9th bit is "1" - channel is supergroup (0001 0000 0000 = 256d)
        if (!(dialog.getType().equals("Channel") && ((dialog.getFlags() & 256) == 0) && (msgs.size() > 0))) {
                msgs = mergeChat(msgs, docThreshold);
        }
        return msgs;
    }

    /**
     * merges (long snd short) chats to docs
     * @param   messages  messages
     * @param   docThreshold threshold
     */
    private static List<TEMessage> mergeChat(List<TEMessage> messages, int docThreshold){
        // if number of messages < docThreshold - short chat, else - long chat
        return (messages.size() < docThreshold) ?  mergeShortChat(messages) : mergeLongChat(messages);
    }

    /**
     * merges short chat messages (number of messages < threshold) to one message
     * @param   messages  all messages
     */
    private static List<TEMessage> mergeShortChat(List<TEMessage> messages) {
        List<TEMessage> merged = new LinkedList<>();
        String text = "";
        for (TEMessage message: messages){
            if (!message.getText().isEmpty()){
                text += message.getText() + "\n";
            }
        }
        if (!text.isEmpty()){
            // id and date of last message are taken
            TEMessage first = messages.get(0);
            merged.add(new TEMessage(first.getId(), text, first.getDate()));
        }
        return merged;
    }

    /**
     * merges long chat messages (number of messages > threshold) if they fit time interval
     * @param   messages  "clean" messages (without empty and service messages)
     */
    private static List<TEMessage> mergeLongChat(List<TEMessage> messages) {
        Collections.sort(messages, new TEMessageComparator());
        // get intervals between messages to array
        List<Integer> dates = new ArrayList<>();
        for (TEMessage message: messages){
            dates.add(message.getDate());
        }
        // deltas between intervals
        List<Integer> deltas = ExpRegMethods.countDeltas(dates);
        // unique deltas (sorted tree set)
        Set<Integer> deltasUnique = new TreeSet<>(deltas);
        // unique deltas counts
        List<Integer> deltasUniqueCounts = ExpRegMethods.countUniqueDeltas(deltasUnique, deltas);

        // Gauss-Newton implementation for fitting
        GaussNewton gn = new GaussNewton() {
            @Override
            public double findY(double x, double[] b) {
                return b[0] * Math.exp(-b[1] * x);
            }
        };

        // values initialization and optimisation
        double[] expModelInit = ExpRegMethods.expRegInitValues(ExpRegMethods.setToDoubles(deltasUnique), ExpRegMethods.listToDoubles(deltasUniqueCounts));
        double[] expModel = new double[2];
        try {
            expModel = gn.optimise(ExpRegMethods.setToDoubles2D(deltasUnique), ExpRegMethods.listToDoubles(deltasUniqueCounts), expModelInit);
        } catch (NoSquareException e) {
            System.out.println(e.getMessage());
        }
        int timeThreshold = (int) Math.ceil(ExpRegMethods.mathTimeThresholdCount(expModel[1], 0.01));
        // returns the list of merged documents
        return mergeByTime(messages, timeThreshold);
    }

    /**
     * concatenates messages to one, if delta time between them is lower than time threshold
     * @param messages documents
     * @param timeThreshold maximum time between messages in one doc
     */
    private static List<TEMessage> mergeByTime(List<TEMessage> messages, int timeThreshold){
        if (timeThreshold <= 0){
            return mergeShortChat(messages);
        } else {
            List<TEMessage> mesCopy = new LinkedList<>(messages);
            for (int i = 0; i < mesCopy.size() - 1; i++) {
                // date of the current and next documents
                TEMessage d0 = mesCopy.get(i);
                TEMessage d1 = mesCopy.get(i + 1);
                // threshold criterion
                if (d0.getDate() - d1.getDate() <= timeThreshold) {
                    mesCopy.set(i, new TEMessage(d0.getId(), d0.getText() + "\n" + d1.getText(), d0.getDate()));
                    mesCopy.remove(i + 1);
                    // returns i back each time, when we have merge of the messages, to check for multiple merges in row
                    i--;
                }
            }
            return mesCopy;
        }
    }
}
