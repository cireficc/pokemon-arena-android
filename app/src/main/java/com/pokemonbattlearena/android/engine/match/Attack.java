package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatType;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.calculators.DamageCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.HealingCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.RecoilCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.StageChangeCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.StatusEffectCalculator;

import static com.pokemonbattlearena.android.engine.Logging.logAttackExecute;
import static com.pokemonbattlearena.android.engine.Logging.logExecuteAttack;

public class Attack extends Command {

    private Move move;
    private BattlePokemonPlayer attackingPlayer;
    private BattlePokemonPlayer defendingPlayer;

    private transient static DamageCalculator damageCalculator = DamageCalculator.getInstance();
    private transient static StatusEffectCalculator statusEffectCalculator = StatusEffectCalculator.getInstance();
    private transient static HealingCalculator healingCalculator = HealingCalculator.getInstance();
    private transient static RecoilCalculator recoilCalculator = RecoilCalculator.getInstance();
    private transient static StageChangeCalculator stageChangeCalculator = StageChangeCalculator.getInstance();

    public Attack(BattlePokemonPlayer attacker, BattlePokemonPlayer defender, Move move) {
        this.attackingPlayer = attacker;
        this.defendingPlayer = defender;
        this.move = move;
    }

    protected Move getMove() {
        return move;
    }

    public BattlePokemonPlayer getAttackingPlayer() {
        return attackingPlayer;
    }

    public BattlePokemonPlayer getDefendingPlayer() {
        return defendingPlayer;
    }


    /*
     * id. When serializing and sending a Command, the player's team info is lost (as
     * the host, who has access to the actual objects, would be the one queueing commands).
     * I can't think of a better way to do it though, because allowing consumers of the
     * Battle Engine to create Command themselves cleans up the logic in the BE quite a bit.
     *
     * Fixed by giving an instance of battle where it is needed
     */
    protected BattlePokemon getAttackingPokemon(Battle battle) {

        return battle.getPlayerFromId(attackingPlayer.getId()).getBattlePokemonTeam().getCurrentPokemon();
    }

    protected BattlePokemon getDefendingPokemon(Battle battle) {

        return battle.getPlayerFromId(defendingPlayer.getId()).getBattlePokemonTeam().getCurrentPokemon();
    }


    @Override
    public AttackResult execute(Battle battle) {

        BattlePokemon attackingPokemon = getAttackingPokemon(battle);
        BattlePokemon defendingPokemon = getDefendingPokemon(battle);
        TargetInfo targetInfo =
                new TargetInfo(attackingPlayer, defendingPlayer, attackingPokemon, defendingPokemon);
        AttackResult.Builder builder = new AttackResult.Builder(targetInfo, move.getId());

        if (move.isChargingMove()) {
            builder.setChargingTurns(move.getChargingTurns());
        }

        if (move.isRechargeMove()) {
            builder.setRechargingTurns(move.getRechargeTurns());
        }

        // If a Pokemon is confused, see if it hurts itself and finish the attack
        if (attackingPokemon.isConfused()) {
            if (statusEffectCalculator.isHurtByConfusion()) {
                builder.setConfusionDamageTaken(statusEffectCalculator.getConfusionDamage(attackingPokemon));
                return builder.build();
            }
        }

        int damageDone = 0;
        for (int i = 0; i < damageCalculator.getTimesHit(move); i++) {
            int partialDamage = damageCalculator.calculateDamage(attackingPokemon, move, defendingPokemon);
            damageDone += partialDamage;
        }

        builder.setDamageDone(damageDone);

        int remainingHp = defendingPokemon.getCurrentHp() - damageDone;

        // If the defender faints, we can return early and skip other calculations
        if (remainingHp <= 0) {
            builder.setFainted(true);
            return builder.build();
        }

        boolean flinched = statusEffectCalculator.doesApplyFlinch(move);
        builder.setFlinched(flinched);

        boolean applyStatusEffect = statusEffectCalculator.doesApplyStatusEffect(move, defendingPokemon);

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

        }

        if (move.isSelfHeal()) {

            int toHeal = healingCalculator.getHealAmount(attackingPokemon, move, damageDone);
            builder.setHealingDone(toHeal);

        }

        if (move.isRecoil()) {
            int recoilTaken = recoilCalculator.getRecoilAmount(attackingPokemon, move, damageDone);
            builder.setRecoilTaken(recoilTaken);
        }

        boolean doStageChange = stageChangeCalculator.doesApplyStageChange(move);

        if (doStageChange) {
            int stageChange = move.getStageChange();
            StatType stageChangeStatType = move.getStageChangeStatType();
            switch (stageChangeStatType) {
                case ATTACK:
                    builder.setAttackStageChange(stageChange);
                    break;
                case DEFENSE:
                    builder.setDefenseStageChange(stageChange);
                    break;
                case SPECIALATTACK:
                    builder.setSpAttackStageChange(stageChange);
                    break;
                case SPECIALDEFENSE:
                    builder.setSpDefenseStageChange(stageChange);
                    break;
                case SPEED:
                    builder.setSpeedStageChange(stageChange);
                    break;
                case CRITICALHIT:
                    builder.setCritStageChange(stageChange);
                    break;
            }
        }

        if (move.getName().equals("Haze")) {
            builder.setIsHaze(true);
        }

        if(logExecuteAttack) {
            logAttackExecute(move, remainingHp, flinched, defendingPokemon, attackingPokemon, applyStatusEffect, statusEffectCalculator, healingCalculator, recoilCalculator, stageChangeCalculator);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return this.move.getName();
    }
}
