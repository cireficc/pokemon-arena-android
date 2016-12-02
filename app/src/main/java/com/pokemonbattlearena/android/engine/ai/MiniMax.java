package com.pokemonbattlearena.android.engine.ai;

import android.util.Log;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.CommandResult;
import com.pokemonbattlearena.android.engine.match.Switch;

import static android.content.ContentValues.TAG;

/**
 * Created by nathan on 10/2/16.
 */

public class MiniMax {

    private static final double MIN_VALUE = -999999;
    protected GameTree gamePossibilities;

    protected Node root;

    protected BattlePokemonPlayer ai;
    protected BattlePokemonPlayer human;
    protected BattlePokemonTeam aiTeam;
    protected BattlePokemonTeam huTeam;
    protected BattlePokemon aiCurrent;
    protected BattlePokemon huCurrent;
    protected int maxAIHP;
    protected int maxHuHP;

    protected Command playerCommand;


    protected int depth = 0;

    MiniMax(BattlePokemonPlayer aiPlayer, BattlePokemonPlayer humanPlayer, int maxAIHp, int maxHuHp, Command playerMove) {
        this.gamePossibilities = new GameTree();

        this.ai = aiPlayer;
        this.human = humanPlayer;

        this.aiTeam = aiPlayer.getBattlePokemonTeam();
        this.huTeam = humanPlayer.getBattlePokemonTeam();

        this.aiCurrent = aiTeam.getCurrentPokemon();
        this.huCurrent = huTeam.getCurrentPokemon();

        this.maxAIHP = maxAIHp;
        this.maxHuHP = maxHuHp;

        this.playerCommand = playerMove;


        root = new Node(aiTeam, huTeam, null);
        root.setHValue(MIN_VALUE);
        gamePossibilities.setRoot(buildTree(depth, root));
        Log.e(TAG, "----------------------------------------------------");
        Log.e(TAG, "Total size: " + gamePossibilities.getRoot().numDominating);
    }

    public static int calculateTeamHP(StatePokemon[] team) {
        int totalHP = 0;
        for (StatePokemon bp : team) {
            totalHP += bp.getCurrentHp();
        }
        return totalHP;
    }


    public Node buildTree(int d, Node n){
        double siblingPower = MIN_VALUE;
        if ( d < 0 ) {
            Log.e(TAG, "buildTree: Hit the depth" );
            return n;
        }

        if (d == depth) {
            for (int i = 0; i < 9; i++) {

                BattlePokemonTeam aiTeam = new BattlePokemonTeam(n.aiTeam);
                BattlePokemonTeam huTeam = new BattlePokemonTeam(n.huTeam);

                BattlePokemonPlayer aiPlayer = new BattlePokemonPlayer(ai.getId(), aiTeam);
                BattlePokemonPlayer huPlayer = new BattlePokemonPlayer(human.getId(), huTeam);

                Battle childState = new Battle(huPlayer, aiPlayer);

                Command huCommand = playerCommand;
                Command aiCommand;

                if (i < 4) {
                    aiCommand = new Attack(aiPlayer, huPlayer, aiTeam.getCurrentPokemon().getMoveSet().get(i));
                } else {
                    aiCommand = new Switch(aiPlayer, i - 4);
                }


                childState.getCurrentBattlePhase().queueCommand(aiCommand);
                childState.getCurrentBattlePhase().queueCommand(huCommand);

                Log.e(TAG, "buildTree: New child");
                BattlePhaseResult res = childState.executeCurrentBattlePhase();
                for (CommandResult commandResult : res.getCommandResults()) {
                    childState.applyCommandResult(commandResult);
                }

                Log.e(TAG, "AI current Pokemon: " + childState.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon().getName());

                Node ne = new Node(aiTeam, huTeam, aiCommand);

                Log.e(TAG, "AI Command: " + ne.getCommandName());
                Log.e(TAG, "Human Max: " + maxHuHP + " Human HP: " + calculateTeamHP(ne.huTeam));
                Log.e(TAG, "AI Max: " + maxAIHP + " AI HP: " + calculateTeamHP(ne.aiTeam));

                double humanMaxDamageReceived = hFunction(maxHuHP, calculateTeamHP(ne.huTeam));
                double aiMinDamageReceived = hFunction(maxAIHP, calculateTeamHP(ne.aiTeam));
                double curValue = humanMaxDamageReceived - aiMinDamageReceived;

                Log.e(TAG, "RES: " + curValue);

                ne.setHValue(curValue);

                if (curValue >= siblingPower) {
                    siblingPower = curValue;
                    Node child = buildTree(d - 1, ne);
                    n.addChild(child);
                    n.numDominating += child.numDominating;
                    chooseBestMove(n);
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {

                    BattlePokemonTeam aiTeam = new BattlePokemonTeam(n.aiTeam);
                    BattlePokemonTeam huTeam = new BattlePokemonTeam(n.huTeam);

                    BattlePokemonPlayer aiPlayer = new BattlePokemonPlayer(ai.getId(), aiTeam);
                    BattlePokemonPlayer huPlayer = new BattlePokemonPlayer(human.getId(), huTeam);

                    Battle childState = new Battle(huPlayer, aiPlayer);

                    Command aiCommand;
                    Command huCommand;

                    if (i < 4) {
                        aiCommand = new Attack(aiPlayer, huPlayer, aiTeam.getCurrentPokemon().getMoveSet().get(i));
                    } else {
                        aiCommand = new Switch(aiPlayer, i - 4);
                    }

                    if (j < 4) {
                        huCommand = new Attack(huPlayer, aiPlayer, huTeam.getCurrentPokemon().getMoveSet().get(j));
                    } else {
                        huCommand = new Switch(huPlayer, j - 4);
                    }

                    childState.getCurrentBattlePhase().queueCommand(aiCommand);
                    childState.getCurrentBattlePhase().queueCommand(huCommand);

                    Log.e(TAG, "buildTree: New child");
                    BattlePhaseResult res = childState.executeCurrentBattlePhase();


                    Node ne = new Node(aiTeam, huTeam, aiCommand);

                    double humanMaxDamageReceived = hFunction(maxHuHP, calculateTeamHP(ne.huTeam));
                    double aiMinDamageReceived = hFunction(maxAIHP, calculateTeamHP(ne.aiTeam));
                    double curValue = humanMaxDamageReceived - aiMinDamageReceived;
                    ne.setHValue(curValue);

                    if (curValue >= siblingPower) {
                        siblingPower = curValue;
                        Node child = buildTree(d - 1, ne);
                        n.addChild(child);
                        chooseBestMove(n);
                        n.numDominating += child.numDominating;
                    }
                }
            }
        }
        return n;
    }

    public double hFunction(int maxHP, int currentHP) {
        return maxHP - currentHP;
    }

    public Node choose() {
       // Node choice = chooseBestMove(gamePossibilities.getRoot()).getBestChild();
        gamePossibilities.getRoot().printTree();
        Log.e(TAG, "CHOSEN MOVE: " + gamePossibilities.getRoot().getBestChild().getCommandName());
        Log.e(TAG, "HUMAN MOVE: " + playerCommand);
        return gamePossibilities.getRoot().getBestChild();
    }

    public Node chooseBestMove(Node n) {

        double curValue = n.getHValue();

        for (Node child : n.children) {
            double childValue = chooseBestMove(child).getHValue();

            if (curValue <= childValue) {
                curValue = childValue;
                n.setHValue(childValue);
                n.setBestChild(child);
            }
        }
        return n;
    }
}
