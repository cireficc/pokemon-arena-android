package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;

/**
 * Created by nathan on 10/25/16.
 */

public class Node {

    protected Node[] children;
    protected StatePokemon[] stateTeam;
    protected double hValue;
    protected Command command;


    public Node(BattlePokemonTeam currentState, Command command) {

        children = new Node[81];
        this.stateTeam = new StatePokemon[6];
        this.command = command;

        int i = 0;
        for (BattlePokemon bp: currentState.getBattlePokemons()) {
            stateTeam[i] = new StatePokemon(bp);
           i++;
        }
    }

    public void setChildAt(int i, Node n) {
        children[i] = n;
    }

    public Node getChild(int i){
        return children[i];
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

    public Command getCommand() { return command; }
}
