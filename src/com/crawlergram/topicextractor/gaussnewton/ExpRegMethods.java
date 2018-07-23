/*
 * Title: TopicExtractorMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.gaussnewton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExpRegMethods {

    /**
     * Counts initial values for optimization. Fits values to exponential function.
     * Returns [B, r] parameters of exponential function y(x) = B*exp(-r*x)
     * @param x x-axis values
     * @param y y-axis values
     */
    public static double[] expRegInitValues(double[] x, double[] y) {
        double[] result = new double[2];
        //variables for sums
        double sx2y = 0;
        double sylny = 0;
        double sxy = 0;
        double sxylny = 0;
        double sy = 0;
        double sxy_2 = 0;
        int n = x.length;
        double lny[] = new double[n];
        for (int i = 0; i < n; i++) {
            lny[i] = Math.log(y[i]);
            sx2y += x[i]*x[i]*y[i];
            sylny += y[i]*lny[i];
            sxy += x[i]*y[i];
            sxylny += x[i]*y[i]*lny[i];
            sy += y[i];
        }

        sxy_2 += Math.pow(sxy, 2);
        result[1] = -(sy*sxylny-sxy*sylny)/(sy*sx2y-sxy_2); // r
        result[0] = Math.exp((sx2y*sylny-sxy*sxylny)/(sy*sx2y-sxy_2)); // B
        return result;
    }

    /**
     * calculates time for topic threshold
     * @param r r parameter of exponential function y(x) = B*exp(-r*x)
     * @param p probability (0.01 or 0.05 are recommended)
     */
    public static double mathTimeThresholdCount(double r, double p){
        return -Math.log(1-(1-p))/r;
    }

    /**
     * counts differences between dates
     * @param dates dates array
     */
    public static List<Integer> countDeltas(List<Integer> dates){
        List<Integer> deltas = new ArrayList<>();
        for (int i = 0; i < dates.size()-1; i++){
            if (dates.get(i+1) != 0){
                deltas.add(dates.get(i) - dates.get(i+1));
            }
        }
        return deltas;
    }

    /**
     * gets count of each unique delta
     * @param deltasUnique unique deltas set
     * @param deltas all deltas
     */
    public static List<Integer> countUniqueDeltas(Set<Integer> deltasUnique, List<Integer> deltas){
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
        return deltasUniqueCounts;
    }

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

}
