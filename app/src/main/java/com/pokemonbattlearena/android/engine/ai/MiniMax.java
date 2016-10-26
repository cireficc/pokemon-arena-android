package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.Turn;

/**
 * Created by nathan on 10/2/16.
 */

public class MiniMax {

    protected GameTree gamePossibilities;
    protected BattlePokemonTeam myTeam;
    protected BattlePokemonTeam opTeam;
    protected Turn curTurn;
    protected PokemonPlayer me;


    MiniMax(GameTree gamePossibilities, BattlePokemonTeam myTeam, BattlePokemonTeam opTeam, PokemonPlayer me) {
        this.gamePossibilities = gamePossibilities;
        this.myTeam = myTeam;
        this.opTeam = opTeam;
        this.me = me;
        buildTree();
    }

    public void buildTree() {
    }

    public int hFunction(Command command) {

        if (command instanceof Attack) {

            if (curTurn.getAttacker().equals(me)) {
                return ((Attack) command).getMove().getPower();
            }

            return -(((Attack) command).getMove().getPower());
            }
        return 0;
    }



}
