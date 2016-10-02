package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.List;

public class PokemonTeam {

    private List<Pokemon> pokemons;

    public PokemonTeam(List<Pokemon> pokemons) {
        this.pokemons = pokemons;
    }

    public List<Pokemon> getPokemons() {
        return pokemons;
    }

    public Pokemon currentPokemon() {
        return pokemons.get(0);
    }
}
