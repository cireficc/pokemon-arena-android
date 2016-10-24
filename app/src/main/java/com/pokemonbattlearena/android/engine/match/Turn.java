package com.pokemonbattlearena.android.engine.match;

public class Turn {

    BattlePokemonPlayer attacker;
    BattlePokemonPlayer defender;
    Command command;

    public Turn(BattlePokemonPlayer attacker, BattlePokemonPlayer defender, Command command) {
        this.attacker = attacker;
        this.defender = defender;
        this.command = command;
    }

    public void executeCommand() {
        command.execute();
    }
}
