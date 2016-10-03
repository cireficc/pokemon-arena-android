package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.Turn;

import java.util.List;

/**
 * Created by nathan on 10/2/16.
 */

public class ArtificialTester extends Battle {
    PokemonPlayer self;
    public PokemonPlayer opponent;
    List<Turn> turns;
    PokemonPlayer turnOwner;

    ArtificialTester(PokemonPlayer player1, PokemonPlayer player2) {
        super(player1, player2);
    }
}
