package topicextractor.maths.ldadmm.models;

import topicextractor.maths.ldadmm.utility.FuncUtils;
import topicextractor.structures.message.TEMessage;
import topicextractor.structures.results.TEResults;
import topicextractor.structures.results.TEResultsParameters;

import java.util.*;

/**
 * jLDADMM: A Java package for the LDA and DMM topic models
 * <p>
 * Implementation of the one-topic-per-document Dirichlet Multinomial Mixture
 * model, using collapsed Gibbs sampling, as described in:
 * <p>
 * Jianhua Yin and Jianyong Wang. 2014. A Dirichlet Multinomial Mixture
 * Model-based Approach for Short Text Clustering. In Proceedings of the 20th
 * ACM SIGKDD International Conference on Knowledge Discovery and Data Mining,
 * pages 233–242.
 *
 * @author: Dat Quoc Nguyen
 */

public class GSDMM {
    private final boolean debug = true; // debug

    public double alpha; // Hyper-parameter alpha
    public double beta; // Hyper-parameter alpha
    public int numTopics; // Number of topics
    public int numIterations; // Number of Gibbs sampling iterations
    public int topWords; // Number of most probable words for each topic

    public double alphaSum; // alpha * numTopics
    public double betaSum; // beta * vocabularySize

    public List<List<Integer>> corpus; // Word ID-based corpus
    public List<Integer> topicAssignments; // Topics assignments for documents
    public int numDocuments; // Number of documents in the corpus
    public int numWordsInCorpus; // Number of words in the corpus

    public TreeMap<String, Integer> word2IdVocabulary; // Vocabulary to get ID
    // given a word
    public TreeMap<Integer, String> id2WordVocabulary; // Vocabulary to get word
    // given an ID
    public int vocabularySize; // The number of word types in the corpus

    // Number of documents assigned to a topic
    public int[] docTopicCount;
    // numTopics * vocabularySize matrix
    // Given a topic: number of times a word type assigned to the topic
    public int[][] topicWordCount;
    // Total number of words assigned to a topic
    public int[] sumTopicWordCount;

    // Double array used to sample a topic
    public double[] multiPros;

    // Given a document, number of times its i^{th} word appearing from
    // the first index to the i^{th}-index in the document
    // Example: given a document of "a a b a b c d c". We have: 1 2 1 3 2 1 1 2
    public List<List<Integer>> occurenceToIndexCount;

    /**
     * DMM with random topic initialization. Data is read from list of TEMessage instances.
     * @param msgs messages (with stemmed text)
     * @param inNumTopics number of topics
     * @param inAlpha alpha parameter, for short text - 0.1, for long text - 0.01
     * @param inBeta beta parameter, 0.1 both for short and long texts
     * @param inNumIterations number of iterations
     * @param inTopWords top topic words for output
     */
    public GSDMM(List<TEMessage> msgs, int inNumTopics,
                 double inAlpha, double inBeta, int inNumIterations, int inTopWords){
        alpha = inAlpha;
        beta = inBeta;
        numTopics = inNumTopics;
        numIterations = inNumIterations;
        topWords = inTopWords;

        if (debug) System.out.println("DMM: reading topic modeling corpus from messages");

        word2IdVocabulary = new TreeMap<>();
        id2WordVocabulary = new TreeMap<>();
        corpus = new ArrayList<>();
        occurenceToIndexCount = new ArrayList<>();
        numDocuments = 0;
        numWordsInCorpus = 0;

        int indexWord = -1;

        for (TEMessage msg : msgs) {
            String doc = msg.getStemmedText();
            if (doc.trim().length() == 0) continue;

            String[] words = doc.trim().split("\\s+");
            List<Integer> document = new ArrayList<>();

            List<Integer> wordOccurrenceToIndexInDoc = new ArrayList<>();
            HashMap<String, Integer> wordOccurrenceToIndexInDocCount = new HashMap<>();

            for (String word : words) {
                if (word2IdVocabulary.containsKey(word)) {
                    document.add(word2IdVocabulary.get(word));
                } else {
                    indexWord += 1;
                    word2IdVocabulary.put(word, indexWord);
                    id2WordVocabulary.put(indexWord, word);
                    document.add(indexWord);
                }

                int times = 0;
                if (wordOccurrenceToIndexInDocCount.containsKey(word)) {
                    times = wordOccurrenceToIndexInDocCount.get(word);
                }
                times += 1;
                wordOccurrenceToIndexInDocCount.put(word, times);
                wordOccurrenceToIndexInDoc.add(times);
            }
            numDocuments++;
            numWordsInCorpus += document.size();
            corpus.add(document);
            occurenceToIndexCount.add(wordOccurrenceToIndexInDoc);
        }

        vocabularySize = word2IdVocabulary.size();
        docTopicCount = new int[numTopics];
        topicWordCount = new int[numTopics][vocabularySize];
        sumTopicWordCount = new int[numTopics];

        multiPros = new double[numTopics];
        for (int i = 0; i < numTopics; i++) {
            multiPros[i] = 1.0 / numTopics;
        }

        alphaSum = numTopics * alpha;
        betaSum = vocabularySize * beta;

        initialize();

        if (debug) System.out.println("Corpus size: " + numDocuments + " docs, "
                + numWordsInCorpus + " words");
        if (debug) System.out.println("Vocabulary size: " + vocabularySize);
        if (debug) System.out.println("Number of topics: " + numTopics);
        if (debug) System.out.println("alpha: " + alpha);
        if (debug) System.out.println("beta: " + beta);
        if (debug) System.out.println("Number of sampling iterations: " + numIterations);
        if (debug) System.out.println("Number of top topical words: " + topWords);
    }

    /**
     * Randomly initialize topic assignments
     */
    private void initialize(){
        if (debug) System.out.println("Randomly initializing topic assignments ...");
        topicAssignments = new ArrayList<>();
        for (int i = 0; i < numDocuments; i++) {
            int topic = FuncUtils.nextDiscrete(multiPros); // Sample a topic
            docTopicCount[topic] += 1;
            int docSize = corpus.get(i).size();
            for (int j = 0; j < docSize; j++) {
                topicWordCount[topic][corpus.get(i).get(j)] += 1;
                sumTopicWordCount[topic] += 1;
            }
            topicAssignments.add(topic);
        }
    }

    /**
     * Inference topic models
     */
    public TEResults inference(){
        if (debug) System.out.println("Running Gibbs sampling inference: ");

        for (int iter = 1; iter <= numIterations; iter++) {
            if (debug && iter % 100 == 0) System.out.println("\tSampling iteration: " + (iter));
            sampleInSingleIteration();
        }

        if (debug) System.out.println("Writing output from the last sample ...");

        return write();
    }

    private void sampleInSingleIteration() {
        for (int dIndex = 0; dIndex < numDocuments; dIndex++) {
            int topic = topicAssignments.get(dIndex);
            List<Integer> document = corpus.get(dIndex);
            int docSize = document.size();

            // Decrease counts
            docTopicCount[topic] -= 1;
            for (Integer aDocument1 : document) {
                int word = aDocument1;
                topicWordCount[topic][word] -= 1;
                sumTopicWordCount[topic] -= 1;
            }

            // Sample a topic
            for (int tIndex = 0; tIndex < numTopics; tIndex++) {
                multiPros[tIndex] = (docTopicCount[tIndex] + alpha);
                for (int wIndex = 0; wIndex < docSize; wIndex++) {
                    int word = document.get(wIndex);
                    multiPros[tIndex] *= (topicWordCount[tIndex][word] + beta
                            + occurenceToIndexCount.get(dIndex).get(wIndex) - 1)
                            / (sumTopicWordCount[tIndex] + betaSum + wIndex);
                }
            }
            topic = FuncUtils.nextDiscrete(multiPros);

            // Increase counts
            docTopicCount[topic] += 1;
            for (Integer aDocument : document) {
                int word = aDocument;
                topicWordCount[topic][word] += 1;
                sumTopicWordCount[topic] += 1;
            }
            // Update topic assignments
            topicAssignments.set(dIndex, topic);
        }
    }

    private TEResultsParameters writeParameters(){
        return new TEResultsParameters("DMM", numTopics, alpha, beta, numIterations, topWords);
    }

    private List<List<Integer>> writeTopicAssignments(){
        List<List<Integer>> ta = new ArrayList<>();
        for (int dIndex = 0; dIndex < numDocuments; dIndex++) {
            List<Integer> t = new ArrayList<>();
            int docSize = corpus.get(dIndex).size();
            int topic = topicAssignments.get(dIndex);
            for (int wIndex = 0; wIndex < docSize; wIndex++) {
                t.add(topic);
            }
            ta.add(t);
        }
        return ta;
    }

    private List<Map<String, Double>> writeTopTopicalWords(){

        List<Map<String, Double>> topWordsList = new ArrayList<>();

        for (int tIndex = 0; tIndex < numTopics; tIndex++) {

            Map<String, Double> topicTopWords = new HashMap<>();

            Map<Integer, Integer> wordCount = new TreeMap<>();
            for (int wIndex = 0; wIndex < vocabularySize; wIndex++) {
                wordCount.put(wIndex, topicWordCount[tIndex][wIndex]);
            }
            wordCount = FuncUtils.sortByValueDescending(wordCount);

            Set<Integer> mostLikelyWords = wordCount.keySet();
            int count = 0;
            for (Integer index : mostLikelyWords) {
                if (count < topWords) {
                    double pro = (topicWordCount[tIndex][index] + beta)
                            / (sumTopicWordCount[tIndex] + betaSum);
                    pro = Math.round(pro * 1000000.0) / 1000000.0;
                    topicTopWords.put(id2WordVocabulary.get(index), pro);
                    count += 1;
                } else {
                    break;
                }
            }
            topWordsList.add(topicTopWords);
        }
        return topWordsList;
    }

    private List<List<Double>> writeTopicWordPros(){
        List<List<Double>> twp = new ArrayList<>();
        for (int i = 0; i < numTopics; i++) {
            List<Double> wp = new ArrayList<>();
            for (int j = 0; j < vocabularySize; j++) {
                double pro = (topicWordCount[i][j] + beta)
                        / (sumTopicWordCount[i] + betaSum);
                wp.add(pro);
            }
            twp.add(wp);
        }
        return twp;
    }

    private List<List<Integer>> writeTopicWordCount(){
        List<List<Integer>> twc = new ArrayList<>();
        for (int i = 0; i < numTopics; i++) {
            List<Integer> wc = new ArrayList<>();
            for (int j = 0; j < vocabularySize; j++) {
                wc.add(topicWordCount[i][j]);
            }
            twc.add(wc);
        }
        return twc;
    }

    private List<List<Double>> writeDocTopicPros(){
        List<List<Double>> dtp = new ArrayList<>();
        for (int i = 0; i < numDocuments; i++) {
            List<Double> tp = new ArrayList<>();
            int docSize = corpus.get(i).size();
            double sum = 0.0;
            for (int tIndex = 0; tIndex < numTopics; tIndex++) {
                multiPros[tIndex] = (docTopicCount[tIndex] + alpha);
                for (int wIndex = 0; wIndex < docSize; wIndex++) {
                    int word = corpus.get(i).get(wIndex);
                    multiPros[tIndex] *= (topicWordCount[tIndex][word] + beta)
                            / (sumTopicWordCount[tIndex] + betaSum);
                }
                sum += multiPros[tIndex];
            }
            for (int tIndex = 0; tIndex < numTopics; tIndex++) {
                tp.add(multiPros[tIndex] / sum);
            }
            dtp.add(tp);
        }
        return dtp;
    }

    private List<List<Integer>> writeDocTopicCount(){
        List<List<Integer>> dtc = new ArrayList<>();
        for (int i = 0; i < docTopicCount.length; i++) {
            List<Integer> tc = new ArrayList<>();
            tc.add(docTopicCount[i]);
            dtc.add(tc);
        }
        return dtc;
    }

    private TEResults write(){
        return new TEResults(writeParameters(), writeTopicAssignments(), writeTopTopicalWords(),
                writeTopicWordPros(), writeTopicWordCount(), writeDocTopicPros(), writeDocTopicCount());
    }

}
