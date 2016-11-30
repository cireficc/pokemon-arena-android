package com.pokemonbattlearena.android.engine.ai;

/**
 * Created by nathan on 10/2/16.
 * Will edit and optimize once basic functionality is working.
 */

public final class GameTree {

    private Node root;

    public GameTree() {
        setRoot(null);
    }

    public GameTree(Node n) {
        setRoot(n);
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node n) {
        root = n;
    }

    public boolean isEmpty() {
        return root == null;
    }


}
