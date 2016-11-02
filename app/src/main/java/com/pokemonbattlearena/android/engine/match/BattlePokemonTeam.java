package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class BattlePokemonTeam {

    public List<BattlePokemon> battlePokemons;

    public BattlePokemonTeam(PokemonTeam pokemonTeam) {

        this.battlePokemons = new ArrayList<>();
        for (Pokemon p : pokemonTeam.getPokemons()) {
            this.battlePokemons.add(new BattlePokemon(p));
        }
    }

    public List<BattlePokemon> getBattlePokemons() {
        return battlePokemons;
    }

    public BattlePokemon getCurrentPokemon() {
        return battlePokemons.get(0);
    }

    public boolean allFainted() {

        for (BattlePokemon pokemon : battlePokemons) {
            if (!pokemon.isFainted()) {
                return false;
            }
        }

        return true;
    }
}
