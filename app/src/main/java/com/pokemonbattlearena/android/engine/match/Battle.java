package com.pokemonbattlearena.android.engine.match;

import java.util.ArrayList;
import java.util.List;

public class Battle {

    PokemonPlayer self;
    PokemonPlayer opponent;
    List<Turn> turns;
    PokemonPlayer turnOwner;

    public Battle(PokemonPlayer player1, PokemonPlayer player2) {
        this.self = player1;
        this.opponent = player2;
        this.turns = new ArrayList<>();
        this.turnOwner = player1;
    }

    public void takeTurn(Command command) {
        Turn turn = new Turn(turnOwner, opponent, command);
        turns.add(turn);
        turn.executeCommand();
        changeTurn();
    }

    private void changeTurn() {
        this.turnOwner = (turnOwner == self) ? opponent : self;
    }
}
