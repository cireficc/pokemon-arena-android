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

    public BattlePokemonPlayer getAttacker() {
        return attacker;
    }

    public BattlePokemonPlayer getDefender() {
        return defender;
    }

    public Command getCommand() {
        return command;
    }

    public void executeCommand() {
        command.execute();
    }
}
