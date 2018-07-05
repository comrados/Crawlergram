/*
 * Title: testdownload.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

import storage.db.DBStorage;
import storage.db.MessageHistoryExclusions;
import storage.db.mongo.MongoDBStorage;
import crawler.apicallback.ApiCallbackImplemented;
import crawler.apimethods.AuthMethods;
import crawler.apimethods.DialogsHistoryMethods;
import crawler.logs.LogMethods;
import crawler.output.ConsoleOutputMethods;
import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLObject;
import org.telegram.tl.TLVector;

import java.util.HashMap;
import java.util.Optional;

public class testdownload {

        private static final int APIKEY = 0; // your api keys
        private static final String APIHASH = ""; // your api hash
        private static final String PHONENUMBER = ""; // your phone number


        public static void main(String[] args) {

            // DB "telegram" location - localhost:27017
            // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
            DBStorage dbStorage = new MongoDBStorage("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

            //register loggers
            LogMethods.registerLogs("logs");

            // api state
            AbsApiState apiState = new MemoryApiState("api.state");

            // app info set
            AppInfo appInfo = new AppInfo(APIKEY, "desktop", "Windows", "pre alpha 0.01", "en");

            // api callback methods
            ApiCallback apiCallback = new ApiCallbackImplemented();

            // init api
            TelegramApi api = new TelegramApi(apiState, appInfo, apiCallback);

            // set api state
            AuthMethods.setApiState(api, apiState);

            // do auth
            AuthMethods.auth(api, apiState, APIKEY, APIHASH, PHONENUMBER, Optional.<String>empty(), Optional.<String>empty());

            // dialogs, chats, users structures
            HashMap<Integer, TLAbsChat> chatsHashMap = new HashMap<>();
            HashMap<Integer, TLAbsUser> usersHashMap = new HashMap<>();
            TLVector<TLDialog> dialogs = new TLVector<>();
            //hashmap with top messages (needed for offsets)
            HashMap<Integer, TLAbsMessage> messagesHashMap = new HashMap<>();

            // get all dialogs of user (telegram returns 100 dialogs at maximum, getting by slices)
            DialogsHistoryMethods.getDialogsChatsUsers(api, dialogs, chatsHashMap, usersHashMap, messagesHashMap);

            // output to console
            ConsoleOutputMethods.testChatsHashMapOutputConsole(chatsHashMap);
            ConsoleOutputMethods.testUsersHashMapOutputConsole(usersHashMap);

            int parLimit = 10;
            int filter = 0;
            int msgLimit = 100;

            TLDialog dialog = new TLDialog();

            for (TLDialog d: dialogs){
                if (d.getPeer().getId() == 415770675){ //415770675
                    dialog = d;
                }
            }

            MessageHistoryExclusions exclusions = new MessageHistoryExclusions(dbStorage, dialog);

            //reads full dialog info
            TLObject fullDialog = DialogsHistoryMethods.getFullDialog(api, dialog, chatsHashMap, usersHashMap);
            //writes full dialog info
            dbStorage.writeFullDialog(fullDialog, chatsHashMap, usersHashMap);

            //reads participants
            TLObject participants = DialogsHistoryMethods.getParticipants(api, fullDialog, chatsHashMap, usersHashMap, parLimit, filter);
            // writes participants of the dialog to "messages + [dialog_id]" table/collection/etc.
            dbStorage.writeParticipants(participants, dialog);

            //reads the messages
            TLAbsMessage topMessage = DialogsHistoryMethods.getTopMessage(dialog, messagesHashMap);
            TLVector<TLAbsMessage> absMessages;
            if (exclusions.exist()){
                absMessages = DialogsHistoryMethods.getWholeMessagesHistoryWithExclusions(api, dialog, chatsHashMap, usersHashMap, topMessage, exclusions, msgLimit, 0, 0);
            } else {
                absMessages = DialogsHistoryMethods.getWholeMessagesHistory(api, dialog, chatsHashMap, usersHashMap, topMessage, msgLimit, 0, 0);
            }
            // writes messages of the dialog to "messages + [dialog_id]" table/collection/etc.
            dbStorage.writeTLAbsMessages(absMessages, dialog);
            System.err.println(dialog.getPeer().getId()+ " "+ absMessages.size());


            System.exit(0);
        }
}
