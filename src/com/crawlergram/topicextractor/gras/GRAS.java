/*
 * 	Author: Giulio Busato
 * 		1111268
 * 		Master Student
 * 		University of Padua
 *
 *	Date:	April 2017
 *
 * */

package com.crawlergram.topicextractor.gras;

import java.util.*;

public class GRAS {

    private static boolean debug = false;

    /**
     * Executing method. Returns words and resulting stems
     *
     * @param sortedWordSet sorted set of unique sortedWordSet for stemming
     * @param l             l parameter (shortest possible common prefix) (~ 5-8)
     * @param alpha         suffix frequency cutoff (~ 4)
     * @param delta         graph edge cutoff threshold (~ 0.5-1.0)
     */
    public static Map<String, String> doStemming(Map<String, String> sortedWordSet, int l, int alpha, double delta) {
        if ((sortedWordSet != null) && !sortedWordSet.isEmpty()) {
            String[] words = sortedWordSet.keySet().toArray(new String[0]);
            String[] stems = GRAS.stemming(words, l, alpha, delta);
            return combineOutput(words, stems);
        } else {
            return new HashMap<>();
        }
    }

    /**
     * combines words and stems into sorted map
     *
     * @param words words
     * @param stems stems
     */
    private static Map<String, String> combineOutput(String[] words, String[] stems) {
        Map<String, String> out = new TreeMap<>();
        if (words.length == stems.length) {
            for (int i = 0; i < words.length; i++) {
                if ((stems[i] == null) || (stems[i].isEmpty())) {
                    out.put(words[i], words[i]);
                } else {
                    out.put(words[i], stems[i]);
                }
            }
        }
        return out;
    }

    /**
     * Performs statistical stemming based on GRAS algorithm
     *
     * @param words words array
     * @param l     l parameter (shortest possible common prefix)
     * @param alpha suffix frequency cutoff
     * @param delta graph edge cutoff threshold
     */
    private static String[] stemming(String[] words, int l, int alpha, double delta) {
        long startTime = System.currentTimeMillis();

        // partition the words in a set of classes such that any two words in the same class have the same LCP>=l
        List<Integer> classes = getPartitions(words, l);
        long partitionsTime = System.currentTimeMillis();

        // find the alpha-frequent suffix pairs <s1,s2> such that: w1=rs1, w2=rs2, r=LCP>= l
        HashMap<String, Integer> frequentSuffixPairs = getFrequentSuffixPairs(words, classes, l, alpha);
        long freqPairsTime = System.currentTimeMillis();

        // identify the class with the pivot word
        String[] stems = identifyClass(words, classes, frequentSuffixPairs, l, delta);
        long stopTime = System.currentTimeMillis();

        if (debug) System.out.println("\n - Stemming:");
        if (debug) System.out.println("\t- partition the words:\t\t" + (partitionsTime - startTime) + "ms");
        if (debug) System.out.println("\t- find frequent suffix pairs:\t" + (freqPairsTime - partitionsTime) + "ms");
        if (debug) System.out.println("\t- identify classes:\t\t" + (stopTime - freqPairsTime) + "ms");
        if (debug) System.out.println("\t- total stemming time:\t\t" + (stopTime - startTime) + "ms");
        return stems;
    }

    /**
     * Returns a list of first and last positions of each class with at least two words
     *
     * @param words words array
     * @param l     l parameter (shortest possible common prefix)
     */
    private static List<Integer> getPartitions(String[] words, int l) {
        List<Integer> classes = new ArrayList<>();

        for (int i = 0; i < words.length; i++)
            if (words[i].length() >= l) {
                String prefix = words[i].substring(0, l);
                int count = 0;

                // search the words with the same prefix l
                for (int j = i + 1; j < words.length; j++) {
                    // if the word is shorter than l or the prefixes are different, stop the search
                    if (words[j].length() < l) break;
                    if (!prefix.equals(words[j].substring(0, l))) break;
                    count++;
                }
                // a class must contain at least two words
                if (count > 0) {
                    classes.add(i);
                    classes.add(i + count);
                }
                i = i + count;
            }

        if (debug) System.out.println("Number of Classes:\t" + classes.size() / 2);
        return classes;
    }

    /**
     * Computes the suffix pairs and their frequencies
     *
     * @param words   words array
     * @param classes classes list
     * @param l       l parameter (shortest possible common prefix)
     * @param alpha   suffix frequency cutoff
     */
    private static HashMap<String, Integer> getFrequentSuffixPairs(String[] words, List<Integer> classes, int l, int alpha) {
        HashMap<String, Integer> alphaFrequent = new HashMap<>();
        HashMap<String, Integer> nonFrequent = new HashMap<>();

        // for each class of words
        for (int i = 0; i < classes.size() - 1; i = i + 2)

            // find all the pairs in the class
            for (int j = classes.get(i); j <= classes.get(i + 1); j++)
                for (int k = j + 1; k <= classes.get(i + 1); k++) {
                    // obtain the suffix pair <s1,s2> of the words <wj,wk>
                    String suffixPair = getSuffixPair(words[j], words[k], l);

                    // update the frequency of the suffix pair
                    Integer frequency = alphaFrequent.get(suffixPair);
                    if (frequency != null)
                        alphaFrequent.put(suffixPair, frequency + 1);
                    else {
                        frequency = nonFrequent.get(suffixPair);
                        if (frequency == null) frequency = 0;
                        frequency++;

                        // if the frequency is alpha the suffix pair become alpha-frequent
                        if (frequency == alpha) {
                            nonFrequent.remove(suffixPair);
                            alphaFrequent.put(suffixPair, frequency);
                        } else
                            nonFrequent.put(suffixPair, frequency);
                    }
                }

        if (debug) System.out.println("Frequent Pairs:\t\t" + alphaFrequent.size());
        if (debug) System.out.println("Non Frequent:\t\t" + nonFrequent.size());
        nonFrequent.clear();
        return alphaFrequent;
    }

    /**
     * Returns the suffix pair <s1,s2> of the words <w1,w2> with the same prefix l
     *
     * @param w1 word 1
     * @param w2 word 2
     * @param l  l parameter (shortest possible common prefix)
     */
    private static String getSuffixPair(String w1, String w2, int l) {
        // calculate the LCP of the two words
        int r = longestCommonPrefix(w1, w2, l);

        // build the suffix pair <s1,s2> of <w1,w2>
        String suffixPair;

        if (w1.length() == r)
            suffixPair = "NULL";
        else
            suffixPair = w1.substring(r, w1.length());

        suffixPair = suffixPair.concat(",");

        if (w2.length() == r)
            suffixPair = suffixPair.concat("NULL");
        else
            suffixPair = suffixPair.concat(w2.substring(r, w2.length()));

        return suffixPair;
    }

    /**
     * Returns the longest common prefix (LCP) of two words that have a common prefix l
     *
     * @param w1 word 1
     * @param w2 word 2
     * @param l  l parameter (shortest possible common prefix)
     */
    private static int longestCommonPrefix(String w1, String w2, int l) {
        int i = l;
        while (i < Math.min(w1.length(), w2.length()))
            if (w1.charAt(i) == w2.charAt(i))
                i++;
            else
                break;
        return i;
    }

    /**
     * Identifies the real class of word (class of stem)
     *
     * @param words               words array
     * @param classes             classes list
     * @param frequentSuffixPairs frequent suffix pairs
     * @param l                   l parameter (shortest possible common prefix)
     * @param delta               graph edge cutoff threshold
     */
    private static String[] identifyClass(String[] words, List<Integer> classes, Map<String, Integer> frequentSuffixPairs, int l, double delta) {
        String[] stems = new String[words.length];

        // for each class of words
        for (int i = 0; i < classes.size() - 1; i = i + 2) {
            int first = classes.get(i);
            int last = classes.get(i + 1);

            // build the graph of the class
            Graph G = buildGraph(first, last, words, frequentSuffixPairs, l);

            // if there isn't any node G.getNodeWithMaxDegree returns -1
            while ((G.getNodeWithMaxDegree()) >= 0) {
                // let p be the pivotal node with maximum degree
                int p = G.getNodeWithMaxDegree();
                int[] adjacentP = G.getAdjacentList(p);

                // S in the class of pivotal node
                List<Integer> S = new ArrayList<>();
                S.add(p);

                // visit all the adjacent nodes of the pivot, in decreasing order of edge weight
                for (int anAdjacentP : adjacentP) {
                    int[] adjP = G.getAdjacentList(p);
                    int[] adjV = G.getAdjacentList(anAdjacentP);

                    // compute the cohesion between the pivot and the visited node v
                    if (getCohesion(adjP, adjV) >= delta)
                        S.add(anAdjacentP);
                    else
                        G.removeEdge(p, anAdjacentP);
                }

                // find the stem word for the class S
                String stem = getStem(S, words, first, l);

                // remove from G all the vertices in S and their incident edges
                for (int j : S) {
                    stems[first + j] = stem;
                    G.removeNode(j);
                }
            }
        }
        return stems;
    }

    /**
     * returns a Graph G=(V,E): V=words in the class, E={(u,v): w(u,v)>=alpha}, where w(u,v) is the frequency of the suffix pair <s1,s2> of <u,v>
     *
     * @param first               class lower limit
     * @param last                class upper limit
     * @param words               words array
     * @param frequentSuffixPairs frequent suffix pairs
     * @param l                   l parameter (shortest possible common prefix)
     */
    private static Graph buildGraph(int first, int last, String[] words, Map<String, Integer> frequentSuffixPairs, int l) {

        // build a graph G with a node for each word in the class
        Graph G = new Graph(last - first + 1);

        // for each couple of words (nodes)
        for (int j = first; j <= last; j++)
            for (int k = j + 1; k <= last; k++) {
                // obtain the suffix pair of the two words
                String suffixPair = getSuffixPair(words[j], words[k], l);

                // if the frequency of the suffix pair is >= alpha, create a weighted edge w(u,v)
                Integer frequency = frequentSuffixPairs.get(suffixPair);
                if (frequency != null) {
                    G.addEdge(j - first, k - first, frequency);
                }
            }
        return G;
    }

    /**
     * Computes the cohesion between two nodes (p,v), given their adjacency lists
     *
     * @param adjP adjacency list of node p
     * @param adjV adjacency list of node v
     */
    private static double getCohesion(int[] adjP, int[] adjV) {
        double cohesion;
        int intersection = 0, j = 0, i = 0;
        Arrays.sort(adjP);
        Arrays.sort(adjV);

        // compute the cardinality of intersection between adjP and adjV
        while (i < adjP.length && j < adjV.length)
            if (adjP[i] == adjV[j]) {
                intersection++;
                i++;
                j++;
            } else if (adjP[i] < adjV[j])
                i++;
            else
                j++;

        // cohesion formula
        cohesion = (1. + intersection) / adjV.length;
        return cohesion;
    }

    /**
     * Returns word stem for the class S
     *
     * @param S     class
     * @param words words array
     * @param first class lower limit
     * @param l     l parameter (shortest possible common prefix)
     */
    private static String getStem(List<Integer> S, String[] words, int first, int l) {
        // assume the first word of the class like a candidate stem
        String stem = words[S.get(0) + first];
        int leng = stem.length();

        // check the candidate for all the words in the class
        for (int i = 1; i < S.size(); i++) {
            String word = words[S.get(i) + first];

            // if the words is shorter or not compatible with the candidate stem, compute the LCP
            if ((word.length() < leng) || !(word.substring(0, leng).equals(stem))) {
                // the LCP is the new candidate stem
                leng = longestCommonPrefix(stem, word, l);
                stem = stem.substring(0, leng);
            }
        }
        return stem;
    }
}
