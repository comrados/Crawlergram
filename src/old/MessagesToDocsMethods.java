/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package old;

import crawler.apimethods.DialogsHistoryMethods;
import topicextractor.maths.gaussnewton.ExpRegMethods;
import topicextractor.maths.gaussnewton.GaussNewton;
import crawler.output.ConsoleOutputMethods;
import topicextractor.maths.gaussnewton.NoSquareException;
import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.chat.channel.TLChannel;
import org.telegram.api.chat.channel.TLChannelForbidden;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.message.TLMessage;
import org.telegram.api.peer.TLPeerChannel;
import org.telegram.api.user.TLAbsUser;
import org.telegram.tl.TLVector;

import java.util.*;

public class MessagesToDocsMethods {

    /**
     * Saves messages to documents hashtable
     * @param	api  TelegramApi instance for RPC request
     * @param   dialogs dialogs TLVector
     * @param   chatsHashMap    chats hashtable
     * @param   usersHashMap    users hashtable
     * @param   limit   maximum number of retrieved messages from each dialog (0 if no limit)
     * @param   docThreshold    threshold between long and short chat
     * @see TelegramApi
     */
    public static HashMap<Integer, List<MessageDoc>> apiMessagesToDocuments(TelegramApi api,
                                                                            TLVector<TLDialog> dialogs,
                                                                            HashMap<Integer, TLAbsChat> chatsHashMap,
                                                                            HashMap<Integer, TLAbsUser> usersHashMap,
                                                                            int limit, int docThreshold){
        HashMap<Integer, List<MessageDoc>> docsInDialogs = new HashMap<>();

        System.out.println(dialogs.size());
        int i=0;

        for (TLDialog dialog : dialogs) {

            i++;
            System.out.println(i);
            System.out.println(ConsoleOutputMethods.testOutHashMapObjectByKey(dialog.getPeer().getId(), chatsHashMap,usersHashMap));

            // make actions upon each message in loop
            TLVector<TLAbsMessage> messages = DialogsHistoryMethods.getOnlyUsersMessagesHistory(api, dialog, chatsHashMap, usersHashMap, limit);

            System.out.println(dialog.getPeer().getId() + " " + messages.size());

            if (messages.size() > 0){
                docsInDialogs.put(dialog.getPeer().getId(), apiMessagesToDocs(dialog, chatsHashMap, messages, docThreshold));
            }
            // sleep between transmissions to avoid flood wait
            try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
        }
        // remove empty ones
        removeEmptyDocs(docsInDialogs);
        return docsInDialogs;
    }

    /**
     * Converts messages from chats, channels and users to docs
     * @param   dialog  current dialog instance
     * @param   chatsHashMap  hashmap of chats
     * @param	messages    TLVector<TLAbsMessage> array of messages
     * @param   docThreshold   if number of messages in current chat is smaller than docThreshold -> all messages is one document
     * @see TLVector<TLAbsMessage>
     * @see TLAbsMessage
     */
    private static  List<MessageDoc> apiMessagesToDocs(TLDialog dialog,
                                                       HashMap<Integer, TLAbsChat> chatsHashMap,
                                                       TLVector<TLAbsMessage> messages, int docThreshold){
        List<MessageDoc> docs = new ArrayList<>();
        //if channel
        if (dialog.getPeer() instanceof TLPeerChannel) {
            TLAbsChat absChannel = chatsHashMap.get(dialog.getPeer().getId());
            if (absChannel instanceof TLChannel){
                TLChannel channel = (TLChannel) absChannel;
                if ((channel.getFlags() & 256) != 0){ // check for supergroup flag: if 9th bit is "1" channel is supergroup (0001 0000 0000 = 256d)
                    docs = apiChatsToDocs(messages, docThreshold);
                } else {
                    docs = apiChannelsToDocs(messages);
                }
            } else if (absChannel instanceof TLChannelForbidden) { // channel forbidden
                docs = apiChannelsToDocs(messages);
            }
        } else { // if user or chat
            docs = apiChatsToDocs(messages, docThreshold);
        }
        return docs;
    }

    /**
     * Converts channels to docs (each message is a document)
     * @param   messages  messages
     * @see TLAbsMessage
     */
    private static  List<MessageDoc> apiChannelsToDocs(TLVector<TLAbsMessage> messages) {
        // get list of "clean" docs (without empty and service messages)
        return apiMessagesToDocs(messages);
    }

    /**
     * converts (long snd short) chats to docs
     * @param   messages  messages
     * @param   docThreshold    threshold
     * @see TLAbsMessage
     */
    private static List<MessageDoc> apiChatsToDocs(TLVector<TLAbsMessage> messages, int docThreshold){
        // get list of "clean" docs (without empty and service messages)
        List<MessageDoc> docs = apiMessagesToDocs(messages);
        // if number of docs < docThreshold - short chat, else - long chat
        return ((docs.size() < docThreshold) && (docs.size() > 0)) ? apiShortChatsToDocs(docs) : apiLongChatsToDocs(docs);
    }

    /**
     * converts short chats (number of docs < threshold) to one doc
     * @param   docs  "clean" docs (without empty and service messages)
     */
    private static List<MessageDoc> apiShortChatsToDocs(List<MessageDoc> docs) {
        List<MessageDoc> chatDocs = new ArrayList<>();
        String docText = "";
        for (MessageDoc doc: docs){
            if (!doc.getText().isEmpty()){
                docText += doc.getText() + "\n";
            }
        }
        if (!docText.isEmpty()){
            // id and date of last doc are written to doc
            chatDocs.add(new MessageDoc(docs.get(0).getId(), docs.get(0).getDate(), docText));
        }
        return chatDocs;
    }

    /**
     * converts long chats (number of docs > threshold) to docs divided by time
     * @param   docs  "clean" docs (without empty and service messages)
     */
    private static List<MessageDoc> apiLongChatsToDocs(List<MessageDoc> docs) {
        Collections.sort(docs, new MessageDocByDateComparator());
        // get intervals between messages to array
        List<Integer> dates = new ArrayList<>();
        for (MessageDoc doc: docs){
            dates.add(doc.getDate());
        }
        // deltas between intervals
        List<Integer> deltas = new ArrayList<>();
        for (int i = 0; i < dates.size()-1; i++){
            deltas.add(dates.get(i) - dates.get(i+1));
        }
        // unique deltas
        Set<Integer> deltasUnique = new HashSet<>(deltas);
        // unique deltas counts
        List<Integer> deltasUniqueCounts = new ArrayList<>();
        for (Integer deltaUnique: deltasUnique){
            Integer count = 0;
            for (Integer delta: deltas){
                if (deltaUnique.equals(delta)){
                    count++;
                }
            }
            deltasUniqueCounts.add(count);
        }
        // Gauss-Newton implementation for fitting
        GaussNewton gn = new GaussNewton() {
            @Override
            public double findY(double x, double[] b) {
                return b[0] * Math.exp(-b[1] * x);
            }
        };
        // values initialization and optimisation
        double[] expModelInit = ExpRegMethods.expRegInitValues(DataStructuresMethods.setToDoubles(deltasUnique), DataStructuresMethods.listToDoubles(deltasUniqueCounts));
        double[] expModel = new double[2];
        try {
            expModel = gn.optimise(DataStructuresMethods.setToDoubles2D(deltasUnique), DataStructuresMethods.listToDoubles(deltasUniqueCounts), expModelInit);
        } catch (NoSquareException e) {
            System.out.println(e.getMessage());
        }
        int timeThreshold = (int) Math.ceil(ExpRegMethods.mathTimeThresholdCount(expModel[1], 0.01));
        // returns the list of merged documents
        return apiMessageMergeDocsByTime(docs, timeThreshold);
    }

    /**
     * converts message content to document
     * @param   absMessage  abstract message
     * @see TLAbsMessage
     */
    private static MessageDoc apiMessageToDoc(TLAbsMessage absMessage){
        MessageDoc doc = null;
        // if not empty message and not service message
        if (absMessage instanceof TLMessage) {
            TLMessage message = (TLMessage) absMessage;
            doc = new MessageDoc(message.getId(), message.getDate(), message.getMessage());
        }
        return doc;
    }

    /**
     * converts messages content to documents, gets list of "clean", not null docs (without TLMessageEmpty and TLMessageService)
     * @param   messages  abstract message
     * @see TLAbsMessage
     */
    private static List<MessageDoc> apiMessagesToDocs(TLVector<TLAbsMessage> messages){
        List<MessageDoc> docs = new ArrayList<>();
        // each TLMessage is doc
        for (TLAbsMessage absMessage : messages) {
            MessageDoc doc = apiMessageToDoc(absMessage);
            if (doc != null){
                docs.add(doc);
            }
        }
        return docs;
    }

    /**
     * concatenates docs to one, if delta time between them is lower than time threshold
     * @param docs documents
     * @param timeThreshold maximum time between messages in one doc
     */
    private static List<MessageDoc> apiMessageMergeDocsByTime(List<MessageDoc> docs, int timeThreshold){
        ArrayList<MessageDoc> docsCopy = new ArrayList<>(docs);
        for (int i = 0; i < docsCopy.size() - 1; i++){
            // date of the current and next documents
            MessageDoc d0 = docsCopy.get(i);
            MessageDoc d1 = docsCopy.get(i+1);
            // threshold criterion
            if (d0.getDate() - d1.getDate() <= timeThreshold){
                docsCopy.set(i, new MessageDoc(d0.getId(), d0.getDate(), d0.getText() + "\n" + d1.getText()));
                docsCopy.remove(i+1);
                // returns i back each time, when we have merge of the docs, to check for multiple merges in row
                i--;
            }
        }
        return docsCopy;
    }

    /**
     * Removes empty docs (no text message)
     * @param docsInDialogs HashMap with docs for output
     */
    private static void removeEmptyDocs(HashMap<Integer, List<MessageDoc>> docsInDialogs){
        Set<Integer> keysDialogs = docsInDialogs.keySet();
        for (Integer keyD : keysDialogs) {
            for (int i = 0; i < docsInDialogs.get(keyD).size(); i++) {
                if (docsInDialogs.get(keyD).get(i).getText().isEmpty()){
                    docsInDialogs.get(keyD).remove(i);
                    i--;
                }
            }
        }
    }

}
