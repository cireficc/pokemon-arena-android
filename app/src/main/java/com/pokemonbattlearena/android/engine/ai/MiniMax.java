package com.pokemonbattlearena.android.engine.ai;

import android.util.Log;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import static android.content.ContentValues.TAG;

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
    protected int maxAIHP;
    protected int maxHuHP;


    protected int depth = 1;

    MiniMax(BattlePokemonPlayer aiPlayer, BattlePokemonPlayer humanPlayer) {
        this.gamePossibilities = new GameTree();

        this.ai = aiPlayer;
        this.human = humanPlayer;

        this.aiTeam = aiPlayer.getBattlePokemonTeam();
        this.huTeam = humanPlayer.getBattlePokemonTeam();

        this.aiCurrent = aiTeam.getCurrentPokemon();
        this.huCurrent = huTeam.getCurrentPokemon();

        maxAIHP = calculateTeamHP(aiTeam);
        maxHuHP = calculateTeamHP(huTeam);


        gamePossibilities.setRoot(buildTree(depth, new Node(aiTeam, huTeam, null)));
        Log.e(TAG, "----------------------------------------------------");
        Log.e(TAG, "Total size: " + gamePossibilities.getRoot().numDominating);
        gamePossibilities.getRoot().printTree();
    }

    public static int calculateTeamHP(BattlePokemonTeam team) {
        int totalHP = 0;
        for (BattlePokemon bp : team.getBattlePokemons()) {
            totalHP += bp.getCurrentHp();
        }
        return totalHP;
    }


    public Node buildTree(int d, Node n){
        if ( d < 0 ) {
            Log.e(TAG, "buildTree: Hit the depth" );
            return n;
        }
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {

                BattlePokemonTeam aiTeam = new BattlePokemonTeam(n.aiTeam);
                BattlePokemonTeam huTeam = new BattlePokemonTeam(n.huTeam);

                BattlePokemonPlayer aiPlayer = new BattlePokemonPlayer(ai.getId(), aiTeam);
                BattlePokemonPlayer huPlayer = new BattlePokemonPlayer(human.getId(), huTeam);

                Battle childState = new Battle(huPlayer, aiPlayer);
                Command aiCommand = new Attack(aiPlayer, huPlayer, aiTeam.getCurrentPokemon().getMoveSet().get(j));
                Command huCommand = new Attack(huPlayer, aiPlayer, huTeam.getCurrentPokemon().getMoveSet().get(k));

                childState.getCurrentBattlePhase().queueCommand(aiCommand);
                childState.getCurrentBattlePhase().queueCommand(huCommand);

                Log.e(TAG, "buildTree: New child");
                BattlePhaseResult res = childState.executeCurrentBattlePhase();
                // for (CommandResult cmd : res.getCommandResults()) {}

                Node ne = new Node(aiTeam, huTeam, aiCommand);
                Node child = buildTree(d-1, ne);
                n.addChild(child);
                n.numDominating += child.numDominating;
            }
        }
        return n;
    }

    public double hFunction(int maxHP, int currentHP) {
        return maxHP - currentHP;
    }

    public Node choose() {
        return chooseBestMove(gamePossibilities.getRoot()).getBestChild();
    }

    public Node chooseBestMove(Node n) {

        double humanMaxDamageReceived = hFunction(maxHuHP, calculateTeamHP(new BattlePokemonTeam(n.huTeam)));
        double aiMinDamageReceived = hFunction(maxAIHP, calculateTeamHP(new BattlePokemonTeam(n.aiTeam)));
        double curValue = humanMaxDamageReceived - aiMinDamageReceived;
        n.setHValue(curValue);

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
