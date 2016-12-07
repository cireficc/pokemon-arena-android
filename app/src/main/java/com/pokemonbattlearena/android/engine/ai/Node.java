package com.pokemonbattlearena.android.engine.ai;

import android.util.Log;

import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.NoP;
import com.pokemonbattlearena.android.engine.match.Switch;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by nathan on 10/25/16.
 */

public class Node {

    protected List<Node> children = new ArrayList<>();
    protected StatePokemon[] aiTeam;
    protected StatePokemon[] huTeam;
    protected double hValue;
    protected Command command;
    protected Node bestChild;
    protected int numDominating = 1;


    public Node(BattlePokemonTeam currentAI, BattlePokemonTeam currentHu, Command command) {

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

    public void addChild(Node n) {
        children.add(n);
    }

    public Node getChild(int i){
        return children.get(i);
    }

    public void setBestChild(Node bestChild) {
        this.bestChild = bestChild;
    }

    public Node getBestChild() {
        return bestChild;
    }

    public int numberOfChildren() {
        return children.size();
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        if (children.isEmpty()) {
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

    public String getCommandName() {
        if (command instanceof Attack) {
            return ((Attack) command).getMove().getName();
        } else if (command instanceof Switch) {
            return "Switching";
        } else if (command instanceof NoP){
            return "NoP - Attempt to switch to Pokemon that is fainted or current";
        } else {
            return "Root node";
        }
    }

    public int getNumDominating() {
        return numDominating;
    }

    public void setNumDominating(int numDominating) {
        this.numDominating = numDominating;
    }


    public void printTree() {
        toStringHelper(0, this);
    }

    private static void toStringHelper(int i, Node node) {
        final StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append("  ");
        }
        sb.append("Node: hValue - " + node.hValue + " Command - " + node.getCommandName());
        sb.append("\n");
        Log.d(TAG, sb.toString());
        for (Node child: node.getChildren()) {
            toStringHelper(i+1, child);
        }
    }

}
