/*
 * Title: TEResultsParameters.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.structures.results;

public class TEResultsParameters {

    private String model;
    private int numTopics;
    private double alpha;
    private double beta;
    private int numIterations;
    private int topWords;

    public TEResultsParameters(String model, int numTopics,
                               double alpha, double beta, int numIterations, int topWords) {
        this.model = model;
        this.numTopics = numTopics;
        this.alpha = alpha;
        this.beta = beta;
        this.numIterations = numIterations;
        this.topWords = topWords;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getNumTopics() {
        return numTopics;
    }

    public void setNumTopics(int numTopics) {
        this.numTopics = numTopics;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public int getTopWords() {
        return topWords;
    }

    public void setTopWords(int topWords) {
        this.topWords = topWords;
    }

    @Override
    public String toString() {
        return model + "\n"
                + "topics " + numTopics + "\n"
                + "iterations " + numIterations + "\n"
                + "top words " + topWords + "\n"
                + "alpha " + alpha + "\n"
                + "beta " + beta + "\n";
    }
}
