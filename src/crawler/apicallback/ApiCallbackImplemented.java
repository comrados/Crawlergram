/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package crawler.apicallback;

import org.telegram.api.engine.ApiCallback;
import org.telegram.api.engine.Logger;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.updates.TLAbsUpdates;

public class ApiCallbackImplemented implements ApiCallback {

    @Override
    public void onUpdatesInvalidated(TelegramApi api) {
        Logger.d("CALLBACK"," >> # UpdatesInvalidated");
    }

    @Override
    public void onUpdate(TLAbsUpdates updates) {
        Logger.d("CALLBACK"," >> # Update " + updates.getClass());
    }

    @Override
    public void onAuthCancelled(TelegramApi api) {
        Logger.d("CALLBACK"," >> # AuthCancelled");
    }

}
