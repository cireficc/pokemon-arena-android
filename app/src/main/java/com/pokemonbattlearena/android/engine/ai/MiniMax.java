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
import com.pokemonbattlearena.android.engine.match.NoP;
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

    //For Nodes as we iterate through
    protected BattlePokemonPlayer aiPlayer;
    protected BattlePokemonPlayer huPlayer;
    protected BattlePokemonTeam nAiTeam;
    protected BattlePokemonTeam nHuTeam;
    protected double siblingPower;
    protected Battle childState;

    protected int maxAIHP;
    protected int maxHuHP;
    protected boolean haveToSwitch = false;

    protected Command playerCommand;

    protected Battle actualBattle;


    protected int depth = 0;

    MiniMax(BattlePokemonPlayer aiPlayer,
            BattlePokemonPlayer humanPlayer,
            int maxAIHp,
            int maxHuHp,
            Command playerMove,
            Battle actualBattle,
            boolean haveToSwitch) {

        if (haveToSwitch) {
            this.depth = 0;
            this.haveToSwitch = haveToSwitch;
        }

        this.gamePossibilities = new GameTree();

        this.actualBattle = actualBattle;

        this.ai = aiPlayer;
        this.human = humanPlayer;

        this.aiTeam = aiPlayer.getBattlePokemonTeam();
        this.huTeam = humanPlayer.getBattlePokemonTeam();

        this.aiCurrent = aiTeam.getCurrentPokemon();
        this.huCurrent = huTeam.getCurrentPokemon();

        this.maxAIHP = maxAIHp;
        this.maxHuHP = maxHuHp;

        this.playerCommand = playerMove;


        root = new Node(aiTeam, huTeam, new NoP());
        root.setBestChild(new Node(aiTeam, huTeam, new NoP()));
        root.setHValue(MIN_VALUE);
        //oot.getBestChild().setHValue(MIN_VALUE + 1);
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
        siblingPower = MIN_VALUE;
        if ( d < 0 ) {
            Log.e(TAG, "buildTree: Hit the depth" );
            return n;
        }

        if (d == depth) {
            for (int i = 0; i <= 9; i++) {

                init(n);

                Command huCommand = playerCommand;
                Command aiCommand;

                if (i < 4 && !haveToSwitch) {
                    aiCommand = new Attack(aiPlayer, huPlayer, nAiTeam.getCurrentPokemon().getMoveSet().get(i));
                } else {
                    if (i < 4) {
                        aiCommand = new NoP(aiPlayer);
                    } else {
                        if (actualBattle.getOpponent().getBattlePokemonTeam().getBattlePokemons().get(i - 4).isFainted()
                                || actualBattle.getOpponent().getBattlePokemonTeam().getBattlePokemons().get(i - 4).isCurrentPokemon()) {
                            aiCommand = new NoP(aiPlayer);
                        } else {
                            aiCommand = new Switch(aiPlayer, i - 4);
                        }
                    }
                }
                resolveBuild(n, aiCommand, huCommand, siblingPower, d);
            }
        } else {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {

                    init(n);

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
                    resolveBuild(n, aiCommand, huCommand, siblingPower, d);
                }
            }
        }
        return n;
    }

    //TODO make the hFunction real smartlike and add different difficulties
    public double hFunction(int maxHP, int currentHP) {
        return maxHP - currentHP;
    }

    private double teamHPDifferenceCalculation (int maxHP, int currentHP) { return maxHP - currentHP; }

    private void init(Node n) {
         setAiTeam(new BattlePokemonTeam(n.aiTeam));
         setHuTeam(new BattlePokemonTeam(n.huTeam));
         setAiPlayer(new BattlePokemonPlayer(ai.getId(), nAiTeam));
         setHuPlayer(new BattlePokemonPlayer(human.getId(), nHuTeam));
         setChildState(new Battle(huPlayer, aiPlayer));
    }

    private void setAiTeam(BattlePokemonTeam t) { this.nAiTeam = t;}
    private void setHuTeam(BattlePokemonTeam t) {this.nHuTeam = t;}
    private void setAiPlayer(BattlePokemonPlayer p) {this.aiPlayer = p;}
    private void setHuPlayer(BattlePokemonPlayer p) {this.huPlayer = p;}
    private void setChildState(Battle childState) {this.childState = childState;}
    private void setSiblingPower(double siblingPower) { this.siblingPower = siblingPower;}

    private void resolveBuild(Node n, Command aiCommand, Command huCommand, double siblingPower, int d) {

        double curValue;

        childState.getCurrentBattlePhase().queueCommand(aiCommand);
        childState.getCurrentBattlePhase().queueCommand(huCommand);

        Log.e(TAG, "buildTree: New child");
        BattlePhaseResult res = childState.executeCurrentBattlePhase();
        for (CommandResult commandResult : res.getCommandResults()) {
            childState.applyCommandResult(commandResult);
        }

        Node ne = new Node(nAiTeam, nHuTeam, aiCommand);


        if (ne.getCommand() instanceof NoP) {
            curValue = Integer.MIN_VALUE;
        } else {
            double humanMaxDamageReceived = teamHPDifferenceCalculation(maxHuHP, calculateTeamHP(ne.huTeam));
            double aiMinDamageReceived = teamHPDifferenceCalculation(maxAIHP, calculateTeamHP(ne.aiTeam));
            curValue = humanMaxDamageReceived - aiMinDamageReceived;
            if (ne.getCommand() instanceof Switch) {
                curValue -= 20;
            }
        }

        Log.e(TAG, "RES: " + curValue);
        ne.setHValue(curValue);
        if (curValue >= siblingPower) {
            setSiblingPower(curValue);
            Node child = buildTree(d - 1, ne);
            n.addChild(child);
            n.numDominating += child.numDominating;
            chooseBestMove(n);
        }
    }


    public Node choose() {
        // Node choice = chooseBestMove(gamePossibilities.getRoot()).getBestChild();
        gamePossibilities.getRoot().printTree();
    //    Log.e(TAG, "CHOSEN MOVE: " + gamePossibilities.getRoot().getBestChild().getCommandName());
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