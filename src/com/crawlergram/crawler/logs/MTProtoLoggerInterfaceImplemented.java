/*
 * Title: MTProtoLoggerInterfaceImplemented.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.crawler.logs;

import org.telegram.mtproto.log.LogInterface;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MTProtoLoggerInterfaceImplemented implements LogInterface {

    String filename;
    PrintWriter out;
    BufferedWriter bw;
    FileWriter fw;
    boolean writeLog = false;

    //constructor
    public MTProtoLoggerInterfaceImplemented(String filename, boolean writeLog) {
        this.filename = filename;
        try {
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            this.writeLog = writeLog;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void w(String s, String s1) {
        if (writeLog) out.println(s + ":" + s1);
    }

    @Override
    public void d(String s, String s1) {
        if (writeLog) out.println(s + ":" + s1);
    }

    @Override
    public void e(String s, String s1) {
        if (writeLog) out.println(s + ":" + s1);
    }

    @Override
    public void e(String s, Throwable throwable) {
        if (writeLog) out.println(s + ":" + throwable.getMessage());
    }
}
