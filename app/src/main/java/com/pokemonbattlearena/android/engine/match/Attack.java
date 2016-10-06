package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;

public class Attack implements Command {

    private Move move;
    private BattlePokemon attacker;
    private BattlePokemon target;

    private static DamageCalculator damageCalculator = DamageCalculator.getInstance();

    public Attack(BattlePokemon attacker, Move move, BattlePokemon target) {
        this.move = move;
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute() {

        // TODO: Actually use real damage/effect calculations
        int damage = move.getPower();
        int remainingHp = target.getCurrentHp() - damage;
        target.setCurrentHp(remainingHp);
    }
}
