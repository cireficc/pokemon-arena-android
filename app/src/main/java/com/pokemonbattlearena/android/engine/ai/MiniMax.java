package com.pokemonbattlearena.android.engine.ai;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Integer.MAX_VALUE;

/**
 * Created by nathan on 10/2/16.
 */

public class MiniMax {

    protected GameTree gamePossibilities;

    protected BattlePokemonPlayer ai;
    protected BattlePokemonPlayer human;
    protected BattlePokemonTeam aiTeam;
    protected BattlePokemonTeam huTeam;
    protected BattlePokemon aiCurrent;
    protected BattlePokemon huCurrent;

    protected int depth = 7;

    MiniMax(BattlePokemonPlayer aiPlayer, BattlePokemonPlayer humanPlayer) {
        this.gamePossibilities = new GameTree();

        this.ai = aiPlayer;
        this.human = humanPlayer;

        this.aiTeam = aiPlayer.getBattlePokemonTeam();
        this.huTeam = humanPlayer.getBattlePokemonTeam();

        this.aiCurrent = aiTeam.getCurrentPokemon();
        this.huCurrent = huTeam.getCurrentPokemon();

        boolean isAi = true;

        gamePossibilities.setRoot(buildTree(depth, new Node(new DummyCommandResult(new Move())), isAi));

      //  for (int i = 0; i < 4; i++) {
      //      Log.d(TAG, gamePossibilities.getRoot().getChild(i).toString());
      //  }
     //   Log.d(TAG, "" + chooseBestMove(gamePossibilities.getRoot(), depth, isAi));
    }


    public Node buildTree(int d, Node n, boolean isAi){
        if ( d < 0 ) {
            return n;
        }
            int i = 0;
            while( i < 4) {
                if (isAi) {
                    Node ne = new Node(new DummyCommandResult(aiCurrent.getMoveSet().get(i)));
                    n.setChildAt(i, (buildTree(d - 1, ne, !isAi)));
                } else {
                    Node ne = new Node(new DummyCommandResult(huCurrent.getMoveSet().get(i)));
                    n.setChildAt(i, (buildTree(d - 1, ne, !isAi)));
                }
                i++;
            }
            return n;
    }

    public double hFunction(Node n) {
        //return new Random().nextInt(1000);
        return n.getValue().movePower;
    }


    public Node choose() {
        return chooseBestMove(gamePossibilities.getRoot(), depth, true);
    }

    public Node chooseBestMove(Node n, int depth, boolean isAi) {
        if (depth == 0 || n.isLeaf()) {
                n.setHValue(hFunction(n));
                return n;
        }

        if (isAi) {
            n.setHValue(MIN_VALUE);
            double curValue;

            for (Node child : n.children) {
               // Log.d(TAG, child.toString());
                curValue = chooseBestMove(child, depth - 1, !isAi).getHValue();
                if (n.getHValue() > curValue) {
                } else {
                     n.setHValue(curValue);
                     n.setValue(child.getValue());
                }
            }
            return n;

        } else {
            n.setHValue(MAX_VALUE);
            double curValue;

            for (Node child : n.children) {
                curValue = chooseBestMove(child, depth - 1, !isAi).getHValue();
                if (n.getHValue() < curValue) {
                } else {
                    n.setHValue(curValue);
                    n.setValue(child.getValue());
                }
            }
            return n;
        }
    }
}
