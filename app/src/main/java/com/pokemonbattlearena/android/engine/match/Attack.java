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

public class Attack extends Command {

    private transient static final String TAG = Attack.class.getName();

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

    public Move getMove() { return move; }

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

        return battle.getPlayerFromId(defendingPlayer.getId()).getBattlePokemonTeam().getPokemonOnDeck();
    }

    @Override
    public AttackResult execute(Battle battle) {

        BattlePokemon attackingPokemon = getAttackingPokemon(battle);
        BattlePokemon defendingPokemon = getDefendingPokemon(battle);
        TargetInfo targetInfo =
                new TargetInfo(attackingPlayer, defendingPlayer, attackingPokemon, defendingPokemon);
        AttackResult.Builder builder = new AttackResult.Builder(targetInfo, move);

        if (move.isChargingMove()) {
            Log.i(TAG, move.getName() + " is charging move (for " + move.getChargingTurns() + " turns)");
            builder.setChargingTurns(move.getChargingTurns());
        }

        if (move.isRechargeMove()) {
            Log.i(TAG, move.getName() + " is recharge move (for " + move.getRechargeTurns() + " turns)");
            builder.setRechargingTurns(move.getRechargeTurns());
        }

        // If a Pokemon is affected by a status effect, finish the attack
        boolean frozen = statusEffectCalculator.isAffectedByFreeze(attackingPokemon);
        boolean paralyzed = statusEffectCalculator.isAffectedByParalysis(attackingPokemon);
        boolean sleeping = statusEffectCalculator.isAffectedBySleep(attackingPokemon);

        // If the Pokemon is not affected by the freeze, it means it unfroze
        if (attackingPokemon.getStatusEffect() == StatusEffect.FREEZE && !frozen) {
            builder.setUnfroze(true);
        }

        if (attackingPokemon.isFlinched() || frozen || paralyzed || sleeping) {
            builder.setSuccumbedToStatusEffect(true);
        }

        // If a Pokemon is confused, see if it hurts itself and finish the attack
        if (attackingPokemon.isConfused()) {
            if (statusEffectCalculator.isHurtByConfusion()) {
                builder.setConfusionDamageTaken(statusEffectCalculator.getConfusionDamage(attackingPokemon));
            }
        }

        if (attackingPokemon.getStatusEffect() == StatusEffect.BURN) {
            builder.setBurnDamageTaken(statusEffectCalculator.getBurnDamage(attackingPokemon));
        }
        if (attackingPokemon.getStatusEffect() == StatusEffect.POISON) {
            builder.setPoisonDamageTaken(statusEffectCalculator.getPoisonDamage(attackingPokemon));
        }

        // Set whether or not the move hit
        builder.setMoveHit(damageCalculator.moveHit(move));

        int damageDone = 0;
        for (int i = 0; i < damageCalculator.getTimesHit(move); i++) {
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

        boolean doStageChange = stageChangeCalculator.doesApplyStageChange(move);
        Log.i(TAG, "Apply Stage change? " + doStageChange);

        if (doStageChange) {
            int stageChange = move.getStageChange();
            StatType stageChangeStatType = move.getStageChangeStatType();
            Log.i(TAG, stageChange + " is the amount");
            Log.i(TAG, stageChangeStatType + " is the stage type");
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

        return builder.build();
    }
}
