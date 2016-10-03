package com.pokemonbattlearena.android.engine.ai;

/**
 * Created by nathan on 10/2/16.
 */

public class MiniMax {

    protected GameTree gamePossibilities;

    MiniMax(GameTree gamePossibilities) {
        this.gamePossibilities = gamePossibilities;
        buildTree();
    }

    public void buildTree(){

    }
}
