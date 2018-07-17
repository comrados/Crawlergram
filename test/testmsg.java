/*
 * Title: testmsg.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

import com.crawlergram.crawler.apicallback.ApiCallbackImplemented;
import com.crawlergram.crawler.apimethods.AuthMethods;
import com.crawlergram.crawler.apimethods.DialogsHistoryMethods;
import com.crawlergram.crawler.logs.ApiLoggerInterfaceImplemented;
import com.crawlergram.crawler.logs.MTProtoLoggerInterfaceImplemented;
import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.AppInfo;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.user.TLAbsUser;
import org.telegram.bot.kernel.engine.MemoryApiState;
import org.telegram.tl.TLVector;

import java.util.HashMap;
import java.util.Optional;

public class testmsg {

    private static final int APIKEY = 0; // your api keys
    private static final String APIHASH = ""; // your api hash
    private static final String PHONENUMBER = ""; // your phone number

    public static void main(String[] args) {

        org.telegram.mtproto.log.Logger.registerInterface(new MTProtoLoggerInterfaceImplemented("log1.log", false));
        org.telegram.api.engine.Logger.registerInterface(new ApiLoggerInterfaceImplemented("log.log", false));

        // auth
        AbsApiState apiState = new MemoryApiState("api.state");
        AppInfo appInfo = new AppInfo(APIKEY, "desktop", "Windows", "pre alpha 0.01", "en");
        ApiCallback apiCallback = new ApiCallbackImplemented();
        TelegramApi api = new TelegramApi(apiState, appInfo, apiCallback);
        AuthMethods.setApiState(api, apiState);
        AuthMethods.auth(api, apiState, APIKEY, APIHASH, PHONENUMBER, Optional.<String>empty(), Optional.<String>empty());


        // dialogs, chats, users structures
        HashMap<Integer, TLAbsChat> chatsHashMap = new HashMap<>();
        HashMap<Integer, TLAbsUser> usersHashMap = new HashMap<>();
        HashMap<Integer, TLAbsMessage> messagesHashMap = new HashMap<>();
        TLVector<TLDialog> dialogs = new TLVector<>();

        // get all dialogs of user (telegram returns 100 dialogs at maximum, getting by slices)
        DialogsHistoryMethods.getDialogsChatsUsers(api, dialogs, chatsHashMap, usersHashMap, messagesHashMap);



        //CrawlingMethods.saveOnlyMessages(api, dialogs, chatsHashMap, usersHashMap, messagesHashMap, 1000);



        System.exit(0);

    }

}
