package com.pokemonbattlearena.android.engine.match;

public class Turn {

    PokemonPlayer attacker;
    PokemonPlayer defender;
    Command command;

    public Turn(PokemonPlayer attacker, PokemonPlayer defender, Command command) {
        this.attacker = attacker;
        this.defender = defender;
        this.command = command;
    }

    public void executeCommand() {
        command.execute();
    }
}
