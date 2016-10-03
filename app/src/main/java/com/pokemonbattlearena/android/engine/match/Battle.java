package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.ai.ArtificialPlayer;

import java.util.ArrayList;
import java.util.List;

public class Battle {

    PokemonPlayer self;
    PokemonPlayer opponent;
    List<Turn> turns;
    PokemonPlayer turnOwner;
    private boolean isAiMatch;


    public Battle(PokemonPlayer player1, PokemonPlayer player2) {

        this.isAiMatch= false;
        this.self = player1;
        this.opponent = player2;
        this.opponent = player2;
        this.turns = new ArrayList<>();
        this.turnOwner = player1;
    }

    public Battle(PokemonPlayer player1, ArtificialPlayer AI){
        this.isAiMatch= true;
        this.self = player1;
        this.opponent = new ArtificialPlayer(self);
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
