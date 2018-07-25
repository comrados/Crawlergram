package com.crawlergram.topicextractor.gras;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private Node[] G; // array containing the nodes of the graph. Node's id is its index in the array
    private List<Integer> sortedNodes; // list of nodes ordered by ascending degree

    // Class representing a single node
    private class Node {

        // adjacency list ordered by descending weight
        private List<Adjacency> AdjacencyList;

        Node() {
            AdjacencyList = new ArrayList<>();
        }

        /**
         * Adds an adjacency to the node with the specified weight
         *
         * @param node   node
         * @param weight weight
         */
        void addAdjacency(int node, int weight) {
            int i = 0;
            while (i < AdjacencyList.size() && weight < AdjacencyList.get(i).w) i++;
            AdjacencyList.add(i, new Adjacency(node, weight));
        }

        /**
         * return an array of adjacent nodes, sorted by descending weight
         */
        int[] getAdjacencyList() {
            int[] res = new int[AdjacencyList.size()];
            for (int i = 0; i < res.length; i++)
                res[i] = AdjacencyList.get(i).a;
            return res;
        }

        /**
         * removes the adjacency to a specific node
         *
         * @param node node
         */
        void removeAdjacency(int node) {
            int i = 0;
            while (i < AdjacencyList.size() && AdjacencyList.get(i).a != node) i++;
            if (i < AdjacencyList.size())
                AdjacencyList.remove(i);
        }

        /**
         * removes all the adjacencies
         */
        void removeAllAdjacency() {
            AdjacencyList.clear();
        }

        /**
         * gets node's degree
         */
        int getDegree() {
            return AdjacencyList.size();
        }
    }

    // Class representing an adjacency
    private class Adjacency {
        int a;    // adjacent node
        int w;    // weight

        Adjacency(int a, int w) {
            this.a = a;
            this.w = w;
        }
    }

    // Graph constructor
    // n - number of nodes
    Graph(int n) {
        G = new Node[n];
        for (int i = 0; i < n; i++)
            G[i] = new Node();
    }

    /**
     * adds a weighted edge to the graph
     *
     * @param n1     node 1
     * @param n2     node 2
     * @param weight weight
     */
    public void addEdge(int n1, int n2, int weight) {
        sortedNodes = null;
        G[n1].addAdjacency(n2, weight);
        G[n2].addAdjacency(n1, weight);
    }

    /**
     * sorts nodes by ascending degree
     */
    private void sortNodes() {
        sortedNodes = new ArrayList<>();
        sortedNodes.add(0);
        for (int i = 1; i < G.length; i++)
            sortedNodes.add(searchDegreePosition(G[i].getDegree(), 0, sortedNodes.size()), i);
    }

    /**
     * gets the starting index of a degree in the sorted nodes list
     *
     * @param degree degree
     * @param from   starting point
     * @param to     ending point
     */
    private int searchDegreePosition(int degree, int from, int to) {
        if (sortedNodes.size() == 0)
            return 0;

        int position = (from + to) / 2;

        if (from >= to || G[sortedNodes.get(position)].getDegree() == degree) {
            if (from >= to)
                position = from;

            while (position > 0 && G[sortedNodes.get(position - 1)].getDegree() >= degree) position--;
            while (position < sortedNodes.size() && G[sortedNodes.get(position)].getDegree() < degree) position++;
            return position;
        }

        if (G[sortedNodes.get(position)].getDegree() > degree)
            return searchDegreePosition(degree, from, position - 1);
        else
            return searchDegreePosition(degree, position + 1, to);
    }

    /**
     * searches the position of a node in the sorted nodes list
     *
     * @param n node
     */
    private int searchNodePosition(int n) {
        int d = G[n].getDegree();
        int i = searchDegreePosition(d, 0, sortedNodes.size()); // get the first position where the degree of the node appears
        // find the node
        while (i < sortedNodes.size() && G[sortedNodes.get(i)].getDegree() == d && sortedNodes.get(i) != n)
            i++;

        if (i < sortedNodes.size() && sortedNodes.get(i) == n)
            return i;
        return -1;
    }

    /**
     * removes adjacency to n2 from node n1
     *
     * @param n1 node 1
     * @param n2 node 2
     */
    private void removeAdjacency(int n1, int n2) {
        // get the position of n1 in the sorted list
        int pos = searchNodePosition(n1);
        // remove the adjacency
        G[n1].removeAdjacency(n2);
        // remove the node from the list
        sortedNodes.remove(pos);
        // find the new position of the node in the sorted list
        int newPosition = searchDegreePosition(G[n1].getDegree(), 0, sortedNodes.size());
        sortedNodes.add(newPosition, n1);
    }

    /**
     * returns the id of the node with max degree
     */
    public int getNodeWithMaxDegree() {
        if (sortedNodes == null)    // if the list isn't initialized
            sortNodes();        // initialize it

        if (sortedNodes.size() > 0)
            return sortedNodes.get(sortedNodes.size() - 1);    // the node is the last of the sorted list
        else
            return -1;
    }

    /**
     * removes the node n from the graph
     *
     * @param n node
     */
    public void removeNode(int n) {
        int pos = searchNodePosition(n);    // get node's position in the sorted list
        if (pos >= 0) {
            sortedNodes.remove(pos);    // remove from the list
            int[] list = G[n].getAdjacencyList();
            // remove the node from each adjacent node's adjacency list
            for (int aList : list) {
                // remove the adjacency
                removeAdjacency(aList, n);
                // and update the sorted list
                pos = searchNodePosition(aList);
                G[aList].removeAdjacency(n);
                sortedNodes.remove(pos);
                sortedNodes.add(searchDegreePosition(G[aList].getDegree(), 0, sortedNodes.size()), aList);
            }
            G[n].removeAllAdjacency();
        }
    }

    /**
     * removes the edge between two nodes
     *
     * @param n1 node 1
     * @param n2 node 2
     */
    public void removeEdge(int n1, int n2) {
        removeAdjacency(n1, n2);
        removeAdjacency(n2, n1);
    }

    /**
     * return the nodes in n's adjacencies list
     *
     * @param n node
     */
    public int[] getAdjacentList(int n) {
        return G[n].getAdjacencyList();
    }
}
