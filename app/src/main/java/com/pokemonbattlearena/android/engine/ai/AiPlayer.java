package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;

import java.util.Random;
import java.util.RandomAccess;

public class AiPlayer extends PokemonPlayer {

    protected PokemonPlayer opponent;
    public Database db;
    protected BattlePokemonPlayer aiBattler;

    public AiPlayer(Database db, PokemonPlayer opponent) {
        this.opponent = opponent;
        this.db = db;
        chooseTeam();
        aiBattler();
    }

    public void chooseTeam() {
        PokemonTeam tmp = new PokemonTeam(1);

        int rnd = new Random().nextInt(db.getPokemons().size());
        tmp.addPokemon(db.getPokemons().get(rnd));
        this.setPokemonTeam(tmp);

    }

    public void aiBattler() {
        aiBattler = new BattlePokemonPlayer(this);
        setMoves();

    }

    public void setMoves() {
        Pokemon org = aiBattler.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
        Move[] moves = new Move[4];

        for (int i = 0; i < 4; i++) {
            int rnd = new Random().nextInt(db.getMovesForPokemon(org).size());
            moves[i] = db.getMovesForPokemon(org).get(rnd);
        }

        aiBattler.getBattlePokemonTeam().getCurrentPokemon().setMoveSet(moves);

    }

    public BattlePokemonPlayer getAiBattler() {
        return aiBattler;
    }
}
