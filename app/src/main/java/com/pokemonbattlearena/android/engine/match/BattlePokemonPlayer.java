package com.pokemonbattlearena.android.engine.match;

import java.util.UUID;

public class BattlePokemonPlayer {

    // Unique id to determine source/target player when applying CommandResult
    private String id;

    private transient BattlePokemonTeam battlePokemonTeam;

    public BattlePokemonPlayer(String id, BattlePokemonTeam battlePokemonTeam) {
        this.id = id;
        this.battlePokemonTeam = battlePokemonTeam;
    }

    public BattlePokemonPlayer(PokemonPlayer player) {
        this (
                player.getPlayerId(),
                new BattlePokemonTeam(player.getPokemonTeam())
        );
    }

    public String getId() {
        return id;
    }

    public BattlePokemonTeam getBattlePokemonTeam() {
        return battlePokemonTeam;
    }
}
