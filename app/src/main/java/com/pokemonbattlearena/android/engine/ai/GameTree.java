package com.pokemonbattlearena.android.engine.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by nathan on 10/2/16.
 * Drawn from http://www.keithschwarz.com/interesting/code/kosaraju/DirectedGraph.java.html
 * Will edit and optimize once basic functionality is working.
 */

public final class GameTree<T> implements Iterable {

    //map from nodes to sets of outgoing edges
    private final Map<T, Set<T>> mGraph = new HashMap<T, Set<T>>();

    /*
    * add a new node to the graph if the node is not in the graph already
    *
    */
    public boolean addNode(T node) {
        if (mGraph.containsKey(node)) {
            return false;
        }
        mGraph.put(node, new HashSet<T>());
        return true;
    }

    /*
    * Get the node values
    */
    public Set<T> nodeValues() {
        return mGraph.keySet();
    }

    /*
    * Add an edge to the graph if both the start and destination exist
    *
    */
    public void addEdge(T start, T dest) {
        if (!mGraph.containsKey(start) || !mGraph.containsKey(dest)) {
            throw new NoSuchElementException("Both nodes must exist");
        }

        mGraph.get(start).add(dest);
    }

    /*
    * Remove edge if both nodes exist in the graph
    *
    */
    public void removeEdge(T start, T dest) {
        if (!mGraph.containsKey(start) || !mGraph.containsKey(dest)) {
            throw new NoSuchElementException("Both nodes must exist");
        }

        mGraph.get(start).remove(dest);
    }

    /*
    * Check to see if there is an edge between two nodes in the graph
    *
    */
    public boolean edgeExists(T start, T end) {
        if (!mGraph.containsKey(start) || !mGraph.containsKey(end)) {
            throw new NoSuchElementException("Both nodes must exist");
        }

        return mGraph.get(start).contains(end);
    }

    /*
    * Return all of the edges leaving the node
    *
    */
    public Set<T> edgesFrom(T node) {
        Set<T> edges = mGraph.get(node);
        if (edges == null) {
            throw new NoSuchElementException("Source node does not exist");
        }
        return Collections.unmodifiableSet(edges);
    }

    public Iterator<T> iterator() {
        return mGraph.keySet().iterator();
    }

}
