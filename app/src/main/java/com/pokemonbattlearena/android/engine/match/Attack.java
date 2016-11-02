package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.calculators.DamageCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.HealingCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.RecoilCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.StatusEffectCalculator;

class Attack implements Command {

    private transient static final String TAG = Attack.class.getName();

    private Move move;
    private BattlePokemonPlayer attackingPlayer;
    private BattlePokemonPlayer defendingPlayer;

    private transient static DamageCalculator damageCalculator = DamageCalculator.getInstance();
    private transient static StatusEffectCalculator statusEffectCalculator = StatusEffectCalculator.getInstance();
    private transient static HealingCalculator healingCalculator = HealingCalculator.getInstance();
    private transient static RecoilCalculator recoilCalculator = RecoilCalculator.getInstance();

    Attack(BattlePokemonPlayer attacker, BattlePokemonPlayer defender, Move move) {
        this.attackingPlayer = attacker;
        this.defendingPlayer = defender;
        this.move = move;
    }

    protected Move getMove() {
        return move;
    }

    protected BattlePokemon getAttackingPokemon() {

        return attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();
    }

    protected BattlePokemon getDefendingPokemon() {

        return defendingPlayer.getBattlePokemonTeam().getCurrentPokemon();
    }


    @Override
    public AttackResult execute() {

        BattlePokemon attackingPokemon = getAttackingPokemon();
        BattlePokemon defendingPokemon = getDefendingPokemon();
        TargetInfo targetInfo =
                new TargetInfo(attackingPlayer, defendingPlayer, attackingPokemon, defendingPokemon);
        AttackResult.Builder builder = new AttackResult.Builder(targetInfo, move.getId());

        if (move.isChargingMove()) {
            Log.i(TAG, move.getName() + " is charging move (for " + move.getChargingTurns() + " turns)");
            builder.setChargingTurns(move.getChargingTurns());
        }

        if (move.isRechargeMove()) {
            Log.i(TAG, move.getName() + " is recharge move (for " + move.getRechargeTurns() + " turns)");
            builder.setRechargingTurns(move.getRechargeTurns());
        }

        int damageDone = 0;
        for (int i = 0; i <= damageCalculator.getTimesHit(move); i++){
            int partialDamage = damageCalculator.calculateDamage(attackingPokemon, move, defendingPokemon);
            Log.i(TAG, "Partial damage: " + partialDamage);
            damageDone += partialDamage;
        }

        Log.i(TAG, "Total damage: " + damageDone);
        builder.setDamageDone(damageDone);

        int remainingHp = defendingPokemon.getCurrentHp() - damageDone;

        // If the defender faints, we can return early and skip other calculations
        if (remainingHp <= 0) {
            Log.d(TAG, defendingPokemon.getOriginalPokemon().getName() + " fainted!");
            builder.setFainted(true);
            return builder.build();
        }

        boolean flinched = statusEffectCalculator.doesApplyFlinch(move);
        Log.i(TAG, move.getName() + " caused flinch? " + flinched);
        builder.setFlinched(flinched);

        boolean applyStatusEffect = statusEffectCalculator.doesApplyStatusEffect(move, defendingPokemon);
        Log.i(TAG, move.getName() + " applied status effect? " + applyStatusEffect);

        if (applyStatusEffect) {
            StatusEffect effect = move.getStatusEffect();
            int turns = statusEffectCalculator.getStatusEffectTurns(effect);

            // Confusion can be applied separately from other status effects
            if (effect == StatusEffect.CONFUSION) {
                builder.setConfused(true);
                builder.setConfusedTurns(turns);
            } else {
                builder.setStatusEffectApplied(effect);
                builder.setStatusEffectTurns(turns);
            }

            Log.i(TAG, "Effect: " + effect + " applied for " + turns + " turns");
        }

        if (move.isSelfHeal()) {
            Log.i(TAG, move.getName() + " is self heal of type " + move.getSelfHealType());

            int toHeal = healingCalculator.getHealAmount(attackingPokemon, move, damageDone);
            builder.setHealingDone(toHeal);

            Log.i(TAG, "Max HP: " + attackingPokemon.getOriginalPokemon().getHp() + "; HP to heal: " + toHeal);
        }

        if (move.isRecoil()) {
            Log.i(TAG, move.getName() + " is recoil type");
            int recoilTaken = recoilCalculator.getRecoilAmount(attackingPokemon, move, damageDone);
            Log.i(TAG, attackingPokemon.getOriginalPokemon().getName() + " takes " + recoilTaken + " recoil damage");
            builder.setRecoilTaken(recoilTaken);
        }

        return builder.build();
    }
}
