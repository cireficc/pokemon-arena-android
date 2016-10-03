package com.pokemonbattlearena.android.engine.ai;

/**
 * Created by nathan on 10/2/16.
 */

//will extend player eventually
public class ArtificialPlayer {
    public static void main(String[] args) {
        GameTree playerOptions = new GameTree();
        MiniMax decision = new MiniMax(playerOptions);

        //System.out.println(test.nodeValues());
    }


}
