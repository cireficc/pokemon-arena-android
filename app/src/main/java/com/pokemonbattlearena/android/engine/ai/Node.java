package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;

/**
 * Created by nathan on 10/25/16.
 */

public class Node {

    protected Node[] children;
    protected StatePokemon[] aiTeam;
    protected StatePokemon[] huTeam;
    protected double hValue;
    protected Command command;
    protected Node bestChild;


    public Node(BattlePokemonTeam currentAI, BattlePokemonTeam currentHu, Command command) {

        children = new Node[81];
        this.aiTeam = new StatePokemon[6];
        this.huTeam = new StatePokemon[6];
        this.command = command;

        int i = 0;
        for (BattlePokemon bp: currentAI.getBattlePokemons()) {
            aiTeam[i] = new StatePokemon(bp);
           i++;
        }
        i = 0;
        for (BattlePokemon bp: currentHu.getBattlePokemons()) {
            huTeam[i] = new StatePokemon(bp);
            i++;
        }
    }

    public void setChildAt(int i, Node n) {
        children[i] = n;
    }

    public Node getChild(int i){
        return children[i];
    }

    public void setBestChild(Node bestChild) {
        this.bestChild = bestChild;
    }

    public Node getBestChild() {
        return bestChild;
    }

    public int numberOfChildren() {
        return children.length;
    }

    public Node[] getChildren() {
        return children;
    }

    public boolean isLeaf() {
        if (children.length ==0) {
            return true;
        }
        return false;
    }

    public double getHValue() {
        return hValue;
    }

    public void setHValue(double h) {
       this.hValue = h;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Command getCommand() { return command; }
}
