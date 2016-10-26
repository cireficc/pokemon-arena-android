package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Database;

public class AiPlayer extends PokemonPlayer {

    private Database database;

    public AiPlayer(Database database) {
        this.database = database;
    }
}
