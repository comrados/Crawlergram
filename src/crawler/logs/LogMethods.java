/*
 * Title: LogMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package crawler.logs;

import crawler.output.FileMethods;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogMethods {

    /**
     * Registers log implementations for Api
     * @param path path to the logs folder
     * @param writeLogs to write or not to write?
     */
    public static void registerLogs(String path, boolean writeLogs){
        // create & check files for logs
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        Date date = new Date();
        String logfilePathApi = path + File.separator + "apiLog_" + dateFormat.format(date) + ".log";
        String logfilePathMTProto = path + File.separator + "MTProtoLog_" + dateFormat.format(date) + ".log";
        if (writeLogs) FileMethods.checkFilePath(logfilePathApi);
        if (writeLogs) FileMethods.checkFilePath(logfilePathMTProto);
        // init logs
        org.telegram.mtproto.log.Logger.registerInterface(new MTProtoLoggerInterfaceImplemented(logfilePathMTProto, writeLogs));
        org.telegram.api.engine.Logger.registerInterface(new ApiLoggerInterfaceImplemented(logfilePathApi, writeLogs));
    }

}
