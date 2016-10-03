package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.ai.ArtificialPlayer;

import java.util.ArrayList;
import java.util.List;

public class Battle {

    PokemonPlayer self;
    PokemonPlayer opponent;
    List<Turn> turns;
    PokemonPlayer turnOwner;


    public Battle(PokemonPlayer player1, PokemonPlayer player2, BattleType type) {
        this.self = player1;

        if (type == BattleType.AI) {
            this.opponent = new ArtificialPlayer(self);
        }
        else {
            this.opponent = player2;
        }

        this.turns = new ArrayList<>();
        this.turnOwner = player1;
    }

    public void takeTurn(Command command) {
        //TODO AI needs to construct command and return here
        Turn turn = new Turn(turnOwner, opponent, command);
        turns.add(turn);
        turn.executeCommand();
        changeTurn();
    }

    private void changeTurn() {
        this.turnOwner = (turnOwner == self) ? opponent : self;
    }
}
