/*
 * Title: TEResults.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.structures.results;

import java.util.List;
import java.util.Map;

public class TEResults {

    private TEResultsParameters parameters;
    private List<List<Integer>> topicAssignments;
    private List<Map<String, Double>> topTopicalWords;
    private List<List<Double>> topicWordPros; // phi
    private List<List<Integer>> topicWordCount;
    private List<List<Double>> docTopicPros; //theta
    private List<List<Integer>> docTopicCount;

    public TEResults(TEResultsParameters parameters, List<List<Integer>> topicAssignments,
                     List<Map<String, Double>> topTopicalWords, List<List<Double>> topicWordPros,
                     List<List<Integer>> topicWordCount,  List<List<Double>> docTopicPros,
                     List<List<Integer>> docTopicCount) {
        this.parameters = parameters;
        this.topicAssignments = topicAssignments;
        this.topTopicalWords = topTopicalWords;
        this.topicWordPros = topicWordPros;
        this.topicWordCount = topicWordCount;
        this.docTopicPros = docTopicPros;
        this.docTopicCount = docTopicCount;
    }

    public TEResultsParameters getParameters() {
        return parameters;
    }

    public void setParameters(TEResultsParameters parameters) {
        this.parameters = parameters;
    }

    public List<List<Integer>> getTopicAssignments() {
        return topicAssignments;
    }

    public void setTopicAssignments(List<List<Integer>> topicAssignments) {
        this.topicAssignments = topicAssignments;
    }

    public List<Map<String, Double>> getTopTopicalWords() {
        return topTopicalWords;
    }

    public void setTopTopicalWords(List<Map<String, Double>> topTopicalWords) {
        this.topTopicalWords = topTopicalWords;
    }

    public List<List<Double>> getTopicWordPros() {
        return topicWordPros;
    }

    public void setTopicWordPros(List<List<Double>> topicWordPros) {
        this.topicWordPros = topicWordPros;
    }

    public List<List<Integer>> getTopicWordCount() {
        return topicWordCount;
    }

    public void setTopicWordCount(List<List<Integer>> topicWordCount) {
        this.topicWordCount = topicWordCount;
    }

    public List<List<Double>> getDocTopicPros() {
        return docTopicPros;
    }

    public void setDocTopicPros(List<List<Double>> docTopicPros) {
        this.docTopicPros = docTopicPros;
    }

    public List<List<Integer>> getDocTopicCount() {
        return docTopicCount;
    }

    public void setDocTopicCount(List<List<Integer>> docTopicCount) {
        this.docTopicCount = docTopicCount;
    }
}
