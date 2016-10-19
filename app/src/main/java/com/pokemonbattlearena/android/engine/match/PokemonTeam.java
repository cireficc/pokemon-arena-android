package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class PokemonTeam {

    protected int size;
    protected List<Pokemon> pokemons;

    public PokemonTeam(int size) {
        this.size = size;
        this.pokemons = new ArrayList<>(size);
    }

    public List<Pokemon> getPokemons() {
        return pokemons;
    }

    public void addPokemon(Pokemon p) {
        pokemons.add(p);
    }

    public void removePokemon(Pokemon p) {
        pokemons.remove(p);
    }
}
