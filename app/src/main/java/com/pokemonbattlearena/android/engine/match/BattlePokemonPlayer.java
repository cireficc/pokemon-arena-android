package com.pokemonbattlearena.android.engine.match;

import java.util.UUID;

public class BattlePokemonPlayer {

    // Unique id to determine source/target player when applying CommandResult
    private String id;

    private transient BattlePokemonTeam battlePokemonTeam;

    public BattlePokemonPlayer(PokemonPlayer player) {

        this.id = UUID.randomUUID().toString();
        this.battlePokemonTeam = new BattlePokemonTeam(player.getPokemonTeam());
    }

    public String getId() {
        return id;
    }

    public BattlePokemonTeam getBattlePokemonTeam() {
        return battlePokemonTeam;
    }
}
