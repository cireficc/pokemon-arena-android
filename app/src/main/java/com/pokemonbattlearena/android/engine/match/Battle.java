package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.ai.ArtificialPlayer;

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
        this.opponent = new BattlePokemonPlayer(player2);
        this.turns = new ArrayList<>();
        this.turnOwner = new BattlePokemonPlayer(player1);
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

    protected void changeTurn() {
        this.turnOwner = (turnOwner == self) ? opponent : self;
    }
}
