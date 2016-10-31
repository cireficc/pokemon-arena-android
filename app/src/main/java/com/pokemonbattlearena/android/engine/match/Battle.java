package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class Battle {

    BattlePokemonPlayer self;
    BattlePokemonPlayer opponent;
    List<Turn> turns;
    BattlePokemonPlayer turnOwner;

    public Battle() { }

    public Battle(PokemonPlayer player1, PokemonPlayer player2) {
        this.self = new BattlePokemonPlayer(player1);
        if (player2 instanceof AiPlayer) {
            this.opponent = ((AiPlayer) player2).getAiBattler();
            //TODO actually get player moves into the Pokemon Player
            setPlayerMoves(((AiPlayer) player2).db);
        } else {
            this.opponent = new BattlePokemonPlayer(player2);
        }
        this.turns = new ArrayList<>();
        this.turnOwner = new BattlePokemonPlayer(player1);
    }

    public void takeTurn(Command command) {
        Turn turn = new Turn(turnOwner, opponent, command);
        turns.add(turn);
        turn.executeCommand();
        changeTurn();
    }

    //TODO kill this method at all costs.
    private void setPlayerMoves(Database db) {
        Pokemon org = self.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
        Move[] moves = new Move[4];
        for (int i = 0; i < 4; i++) {
            moves[i] = db.getMovesForPokemon(org).get(i);
        }
        self.getBattlePokemonTeam().getCurrentPokemon().setMoveSet(moves);
    }

    public BattlePokemonPlayer getSelf() {
        return self;
    }

    public BattlePokemonPlayer getOpponent() {
        return opponent;
    }

    public BattlePokemonPlayer getTurnOwner() {
        return turnOwner;
    }

    protected void changeTurn() {
        this.turnOwner = (turnOwner == self) ? opponent : self;
    }
}
