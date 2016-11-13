package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AiPlayer extends PokemonPlayer {

    protected PokemonPlayer opponent;
    public Database db;
    protected BattlePokemonPlayer aiBattler;

    public AiPlayer(Database db, PokemonPlayer opponent) {
        super("AI");
        this.opponent = opponent;
        this.db = db;
        chooseTeam();
        aiBattler();
    }

    public void chooseTeam() {
        PokemonTeam tmp = new PokemonTeam(6);

        for (int i = 0; i < 6; i++) {
            int rnd = new Random().nextInt(db.getPokemons().size());
            tmp.addPokemon(db.getPokemons().get(i));
        }
        this.setPokemonTeam(tmp);

    }

    public void aiBattler() {
        aiBattler = new BattlePokemonPlayer(this);
        setMoves();

    }

    public void setMoves() {
        //TODO Set moves for all Pokemon
        Pokemon org = aiBattler.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
        List<Move> moves = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            int rnd = new Random().nextInt(db.getMovesForPokemon(org).size());
            moves.add(i, db.getMovesForPokemon(org).get(rnd));
        }

        aiBattler.getBattlePokemonTeam().getCurrentPokemon().setMoveSet(moves);
    }

    public BattlePokemonPlayer getAiBattler() {
        return aiBattler;
    }
}
