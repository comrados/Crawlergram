/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.old;

import com.crawlergram.crawler.output.FileMethods;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DataStructuresMethods {

    /**
     * Converts Set to double array
     * @param   set   input set (with doubles or integers)
     */
    public static double[] setToDoubles(Set<Integer> set){
        double[] res = new double[set.size()];

        int i = 0;
        for (Integer elem: set){
            res[i] = elem;
            i++;
        }

        return res;
    }

    /**
     * Converts Set to double array
     * @param   set   input set (with doubles or integers)
     */
    public static double[][] setToDoubles2D(Set<Integer> set){
        double[][] res = new double[set.size()][1];

        int i = 0;
        for (Integer elem: set){
            res[i][0] = elem;
            i++;
        }

        return res;
    }

    /**
     * Converts List to double array
     * @param   list   input list (with doubles or integers)
     */
    public static double[] listToDoubles(List<Integer> list){
        double[] res = new double[list.size()];

        int i = 0;
        for (Integer elem: list){
            res[i] = elem;
            i++;
        }

        return res;
    }

    /**
     * Converts List to double array
     * @param docsInDialogs HashMap with docs for output
     */
    public static void saveDocsToFiles(HashMap<Integer, List<MessageDoc>> docsInDialogs, String path) {
        Set<Integer> keysDialogs = docsInDialogs.keySet();
        //date (part of the filename uniqueness)
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS_");
        Date date = new Date();
        // create file for output
        for (Integer keyD : keysDialogs) {
            String filename = path + File.separator + "docs_" + dateFormat.format(date) + keyD + ".csv";
            FileMethods.checkFilePath(filename);
            //write docs data to files
            try {
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
                for (MessageDoc doc : docsInDialogs.get(keyD)) {
                    out.append(doc.getId() + ";" + doc.getDate() + ";" + doc.getText() + "\n");
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Replaces all the new lines in docs
     * @param docsInDialogs HashMap with docs for output
     */
    public static void removeDocsNewLine(HashMap<Integer, List<MessageDoc>> docsInDialogs){
        Set<Integer> keysDialogs = docsInDialogs.keySet();
        for (Integer keyD : keysDialogs) {
            for (int i = 0; i < docsInDialogs.get(keyD).size(); i++) {
                docsInDialogs.get(keyD).get(i).setText(replaceNewLine(docsInDialogs.get(keyD).get(i).getText(), " "));
            }
        }
    }

    /**
     * Replaces all \n, \r\n and \r with repl argument
     * @param str original string
     * @param repl replacement
     */
    private static String replaceNewLine(String str, String repl){
        String str1 = str.replaceAll("\n\r", repl);
        String str2 = str1.replaceAll("\r", repl);
        String str3 = str2.replaceAll("\n", repl);
        return str3;
    }

}
