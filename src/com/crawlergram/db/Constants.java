/*
 * Title: Const.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.db;

/**
 * DB constants
 */

public class Constants {
    public static String MSG_DIAL_PREF = "MESSAGES"; // dialog messages table/collection/etc. prefix
    public static String PAR_DIAL_PREF = "PARTICIPANTS"; // dialog participants table/collection/etc. prefix
    public static String USERS_COL = "USERS"; // users table/collection/etc.
    public static String CHATS_COL = "CHATS"; // chats table/collection/etc.
    public static String DIALOGS = "DIALOGS"; // user dialogs (full info) table/collection/etc.

    public static String getMsgDialPref() {
        return MSG_DIAL_PREF;
    }

    public static String getUsersCol() {
        return USERS_COL;
    }

    public static void setUsersCol(String usersCol) {
        USERS_COL = usersCol;
    }

    public static String getChatsCol() {
        return CHATS_COL;
    }

    public static void setChatsCol(String chatsCol) {
        CHATS_COL = chatsCol;
    }

    public static void setMsgDialPref(String msgDialPref) {
        MSG_DIAL_PREF = msgDialPref;
    }

    public static String getParDialPref() {
        return PAR_DIAL_PREF;
    }

    public static void setParDialPref(String parDialPref) {
        PAR_DIAL_PREF = parDialPref;
    }

    public static String getDialogs() {
        return DIALOGS;
    }

    public static void setDialogs(String DIALOGS) {
        Constants.DIALOGS = DIALOGS;
    }
}
