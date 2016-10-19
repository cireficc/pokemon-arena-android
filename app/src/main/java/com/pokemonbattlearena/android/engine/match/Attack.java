package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;

public class Attack implements Command {

    private static final String TAG = Attack.class.getName();

    private Move move;
    private BattlePokemon attacker;
    private BattlePokemon target;

    private static DamageCalculator damageCalculator = DamageCalculator.getInstance();
    private static StatusEffectCalculator statusEffectCalculator = StatusEffectCalculator.getInstance();

    public Attack(BattlePokemon attacker, Move move, BattlePokemon target) {
        this.move = move;
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute() {

        // TODO: Actually use real damage/effect calculations
        int damage = damageCalculator.calculateDamage(attacker, move, target);
        int remainingHp = target.getCurrentHp() - damage;
        target.setCurrentHp(remainingHp);
        boolean flinched = statusEffectCalculator.doesApplyFlinch(move);
        Log.i(TAG, move.getName() + " caused flinch? " + flinched);

        if (target.getCurrentHp() <= 0) {
            Log.d(TAG, target.getOriginalPokemon().getName() + " fainted! (" + target.getCurrentHp() + " hp)");
            target.setFainted(true);
        }
    }
}
