package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;

public class Attack implements Command {

    private Move move;
    private BattlePokemon receiver;

    public Attack(Move move, BattlePokemon receiver) {
        this.move = move;
        this.receiver = receiver;
    }

    @Override
    public void execute() {

        // TODO: Actually use real damage/effect calculations
        int damage = move.getPower();
        int remainingHp = receiver.getCurrentHp() - damage;
        receiver.setCurrentHp(remainingHp);
    }
}