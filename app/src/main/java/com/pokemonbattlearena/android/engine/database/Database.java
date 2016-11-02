package com.pokemonbattlearena.android.engine.database;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private final String TAG = this.getClass().getName();

    protected AndroidConnectionSource connection = new AndroidConnectionSource(this);
    private static final String DATABASE_NAME = "db-pokemon-arena.db";
    private static final int DATABASE_VERSION = 1;

    private static Dao<Pokemon, String> pokemonDao;
    private static Dao<Move, String> moveDao;
    private static Dao<PokemonMove, String> pokemonMoveDao;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            pokemonDao = DaoManager.createDao(connection, Pokemon.class);
            moveDao = DaoManager.createDao(connection, Move.class);
            pokemonMoveDao = DaoManager.createDao(connection, PokemonMove.class);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public List<Pokemon> getPokemons() {

        List<Pokemon> pokemons = new ArrayList<>();

        try {
            pokemons = pokemonDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        return pokemons;
    }

    public List<Move> getMoves() {

        List<Move> moves = new ArrayList<>();

        try {
            moves = moveDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        return moves;
    }

    public List<Move> getMovesForPokemon(Pokemon p) {

        List<Move> moves = new ArrayList<>();

        try {
            // Build the inner query for PokemonMove objects
            QueryBuilder<PokemonMove, String> pokemonMoveQb = pokemonMoveDao.queryBuilder();
            // Just select the Move id field
            pokemonMoveQb.selectColumns(PokemonMove.MOVE_ID_FIELD_NAME);
            SelectArg pokemonSelectArg = new SelectArg(p.getId());
            pokemonMoveQb.where().eq(PokemonMove.POKEMON_ID_FIELD_NAME, pokemonSelectArg);

            // Build the outer query for Move objects
            QueryBuilder<Move, String> moveQb = moveDao.queryBuilder();
            // Where the id matches in the Move id from the inner query
            moveQb.where().in(Move.ID_FIELD_NAME, pokemonMoveQb);

            moves = moveDao.query(moveQb.prepare());
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

        return moves;
    }

    public Dao<Pokemon, String> getPokemonDao() throws SQLException {
        return pokemonDao;
    }

    public Dao<Move, String> getMoveDao() throws SQLException {
        return moveDao;
    }

    public Dao<PokemonMove, String> getPokemonMoveDao() {
        return pokemonMoveDao;
    }

    @Override
    public void close() {
        super.close();
        pokemonDao = null;
    }
}