package com.pokemonbattlearena.android.engine.match;

public class PokemonPlayer {

    private PokemonTeam pokemonTeam;
    private BattlePokemonTeam battlePokemonTeam;

    public PokemonPlayer() {

    }

    public PokemonTeam getPokemonTeam() {
        return pokemonTeam;
    }

    public void setPokemonTeam(PokemonTeam pokemonTeam) {
        this.pokemonTeam = pokemonTeam;
    }

    public BattlePokemonTeam getBattlePokemonTeam() {
        return battlePokemonTeam;
    }

    public void setBattlePokemonTeam() {
        this.battlePokemonTeam = new BattlePokemonTeam(pokemonTeam);
    }
}
