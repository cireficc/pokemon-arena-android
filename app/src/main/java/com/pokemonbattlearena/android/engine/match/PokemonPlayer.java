package com.pokemonbattlearena.android.engine.match;

public class PokemonPlayer {

    private PokemonTeam pokemonTeam;
    private String playerId;

    public PokemonPlayer() {

    }
    public PokemonPlayer(String playerId) {
        this.playerId = playerId;
    }

    public PokemonTeam getPokemonTeam() {
        return pokemonTeam;
    }

    public void setPokemonTeam(PokemonTeam pokemonTeam) {
        this.pokemonTeam = pokemonTeam;
    }

    public String getPlayerId() {
        return playerId;
    }
}
