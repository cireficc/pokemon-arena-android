package com.pokemonbattlearena.android.engine.match;

public class BattlePokemonPlayer {

    private BattlePokemonTeam battlePokemonTeam;

    public BattlePokemonPlayer() { }

    public BattlePokemonPlayer(PokemonPlayer player) {

        this.battlePokemonTeam = new BattlePokemonTeam(player.getPokemonTeam());
    }

    public BattlePokemonTeam getBattlePokemonTeam() {
        return battlePokemonTeam;
    }
}
