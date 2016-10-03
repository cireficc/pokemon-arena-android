package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.engine.match.Turn;

/**
 * Created by nathan on 10/2/16.
 */

//will extend player eventually
public class ArtificialPlayer extends PokemonPlayer {
    BattlePokemonTeam myTeam;
    BattlePokemonTeam opTeam;


    public ArtificialPlayer(PokemonPlayer opponent) {
        GameTree playerOptions = new GameTree();
        myTeam = getBattlePokemonTeam();
        this.opTeam = opponent.getBattlePokemonTeam();
        //MiniMax decision = new MiniMax(playerOptions, myTeam, opTeam, this);
    }
}
