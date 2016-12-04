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
             if (rnd == 9 || rnd == 10 || rnd == 12 || rnd == 13 || rnd == 128) {
             rnd = new Random().nextInt(8);
            }
             tmp.addPokemon(db.getPokemons().get(rnd));
        //tmp.addPokemon(db.getPokemons().get(5));
        //tmp.addPokemon(db.getPokemons().get(2));
        //tmp.addPokemon(db.getPokemons().get(0));
        //tmp.addPokemon(db.getPokemons().get(3));
        //tmp.addPokemon(db.getPokemons().get(6));
          //tmp.addPokemon(db.getPokemons().get(8));
        }
        this.setPokemonTeam(tmp);

    }

    public void aiBattler() {
        aiBattler = new BattlePokemonPlayer(this);
        setMoves();

    }

    public void setMoves() {

        for (int i = 0; i < 6; i++) {
            Pokemon org = aiBattler.getBattlePokemonTeam().getBattlePokemons().get(i).getOriginalPokemon();
            List<Move> moves = new ArrayList<>(4);
            for (int j = 0; j < 4; j++) {
                int rnd = new Random().nextInt(db.getMovesForPokemon(org).size());
                moves.add(db.getMovesForPokemon(org).get(rnd));
            }
            aiBattler.getBattlePokemonTeam().getBattlePokemons().get(i).setMoveSet(moves);
            org.setActiveMoveList(moves);

        }
    }

    public BattlePokemonPlayer getAiBattler() {
        return aiBattler;
    }
}
