/*
 * Title: MTProtoLoggerInterfaceImplemented.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package crawler.logs;

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

    //constructor
    public MTProtoLoggerInterfaceImplemented(String filename) {
        this.filename = filename;
        try {
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void w(String s, String s1) {
        out.println(s + ":" + s1);
    }

    @Override
    public void d(String s, String s1) {
        out.println(s + ":" + s1);
    }

    @Override
    public void e(String s, String s1) {
        out.println(s + ":" + s1);
    }

    @Override
    public void e(String s, Throwable throwable) {
        out.println(s + ":" + throwable.getMessage());
    }
}
