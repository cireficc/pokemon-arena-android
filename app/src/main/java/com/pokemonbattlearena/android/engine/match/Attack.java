package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.calculators.DamageCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.HealingCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.StatusEffectCalculator;

public class Attack implements Command {

    private static final String TAG = Attack.class.getName();

    private Move move;
    private BattlePokemon attacker;
    private BattlePokemon target;

    private static DamageCalculator damageCalculator = DamageCalculator.getInstance();
    private static StatusEffectCalculator statusEffectCalculator = StatusEffectCalculator.getInstance();
    private static HealingCalculator healingCalculator = HealingCalculator.getInstance();

    public Attack(BattlePokemon attacker, Move move, BattlePokemon target) {
        this.move = move;
        this.attacker = attacker;
        this.target = target;
    }

    public Move getMove() {
        return move;
    }

    public BattlePokemon getAttacker() {
        return attacker;
    }

    public BattlePokemon getTarget() {
        return target;
    }

    @Override
    public void execute() {

        if (move.isChargingMove()) {
            Log.i(TAG, move.getName() + " is charging move (for " + move.getChargingTurns() + " turns)");
            attacker.setChargingForTurns(move.getChargingTurns());
        }

        if (move.isRechargeMove()) {
            Log.i(TAG, move.getName() + " is recharge move (for " + move.getRechargeTurns() + " turns)");
            attacker.setRechargingForTurns(move.getRechargeTurns());
        }

        int damage = 0;
        for (int i = 0; i <= damageCalculator.getTimesHit(move); i++){
            int partialDamage = damageCalculator.calculateDamage(attacker, move, target);
            Log.i(TAG, "Partial damage: " + partialDamage);
            damage += partialDamage;
        }

        int remainingHp = target.getCurrentHp() - damage;
        target.setCurrentHp(remainingHp);
        boolean flinched = statusEffectCalculator.doesApplyFlinch(move);
        boolean applyStatusEffect = statusEffectCalculator.doesApplyStatusEffect(move, target);

        Log.i(TAG, move.getName() + " caused flinch? " + flinched);
        Log.i(TAG, move.getName() + " applied status effect? " + applyStatusEffect);

        target.setFlinched(true);

        if (applyStatusEffect) {
            StatusEffect effect = move.getStatusEffect();
            int turns = statusEffectCalculator.getStatusEffectTurns(effect);

            // Confusion can be applied separately from other status effects
            if (effect == StatusEffect.CONFUSION) {
                target.setConfused(true);
                target.setConfusedTurns(turns);
            } else {
                target.setStatusEffect(effect);
                target.setStatusEffectTurns(turns);
            }

            Log.i(TAG, "Effect: " + effect + " applied for " + turns + " turns");
        }

        if (move.isSelfHeal()) {
            Log.i(TAG, move.getName() + " is self heal of type " + move.getSelfHealType());

            int toHeal = healingCalculator.getHealAmount(attacker, move, damage);
            int maxHp = attacker.getOriginalPokemon().getHp();
            int hpAfterHeal = attacker.getCurrentHp() + toHeal;

            Log.i(TAG, "Max HP: " + attacker.getOriginalPokemon().getHp() + "; HP with healing: " + hpAfterHeal);

            if (hpAfterHeal >= maxHp) {
                attacker.setCurrentHp(maxHp);
                Log.i(TAG, "Healed to max HP");
            } else {
                attacker.setCurrentHp(hpAfterHeal);
                Log.i(TAG, "HP after healing: " + attacker.getCurrentHp());
            }
        }

        if (target.getCurrentHp() <= 0) {
            Log.d(TAG, target.getOriginalPokemon().getName() + " fainted! (" + target.getCurrentHp() + " hp)");
            target.setFainted(true);
        }
    }
}
