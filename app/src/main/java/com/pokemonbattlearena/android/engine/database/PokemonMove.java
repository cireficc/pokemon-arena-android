package com.pokemonbattlearena.android.engine.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "pokemon_moves")
public class PokemonMove {

    protected final static String ID_FIELD_NAME = "id";
    protected final static String POKEMON_ID_FIELD_NAME = "pokemon_id";
    protected final static String MOVE_ID_FIELD_NAME = "move_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    int id;
    @DatabaseField(columnName = POKEMON_ID_FIELD_NAME)
    int pokemonId;
    @DatabaseField(columnName = MOVE_ID_FIELD_NAME)
    int moveId;

    // Foreign objects (i.e. INNER JOIN objects)
    @DatabaseField(foreign = true, columnName = POKEMON_ID_FIELD_NAME)
    Pokemon pokemon;
    @DatabaseField(foreign = true, columnName = MOVE_ID_FIELD_NAME)
    Move move;

    public PokemonMove() {
        // Constructor for ORMLite
    }

    public int getId() {
        return id;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public Move getMove() {
        return move;
    }
}
