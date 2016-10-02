package com.pokemonbattlearena.android.engine.match;

public class PokemonPlayer {

    private PokemonTeam pokemonTeam;

    public PokemonPlayer() {

    }

    public PokemonTeam getPokemonTeam() {
        return pokemonTeam;
    }

    public void setPokemonTeam(PokemonTeam pokemonTeam) {
        this.pokemonTeam = pokemonTeam;
    }
}
