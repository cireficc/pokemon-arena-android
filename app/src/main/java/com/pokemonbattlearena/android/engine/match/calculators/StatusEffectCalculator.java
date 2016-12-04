package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import java.util.concurrent.ThreadLocalRandom;

public class StatusEffectCalculator {

    private static StatusEffectCalculator instance = null;

    private static final String TAG = StatusEffectCalculator.class.getName();

    private static final int MAX_CHANCE = 100;
    private static final int HURT_SELF_IN_CONFUSION_CHANCE = 50;

    protected StatusEffectCalculator() {
    }

    public static StatusEffectCalculator getInstance() {

        if (instance == null) {
            instance = new StatusEffectCalculator();
        }

        return instance;
    }

    public boolean doesApplyFlinch(Move move) {

        if (!move.canFlinch()) {
            return false;
        } else {
            // Always 15% chance to flinch
            final int FLINCH_CHANCE = 15;
            int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
            return (rolled >= (MAX_CHANCE - FLINCH_CHANCE));
        }
    }

    /*
     * Calculate any status effect produced by using a move against a target Pokemon
     * http://bulbapedia.bulbagarden.net/wiki/Status_move
     */
    public boolean doesApplyStatusEffect(Move move, BattlePokemon target) {

        boolean alreadyConfused = (target.isConfused() && move.getStatusEffect() == StatusEffect.CONFUSION);

        if (alreadyConfused || target.hasStatusEffect() || move.getStatusEffect() == null) {
            return false;
        } else {
            int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
            return (rolled >= (MAX_CHANCE - move.getStatusEffectChance()));
        }
    }

    /*
     * Calculate the number of turns that a status effect will last
     */
    public int getStatusEffectTurns(StatusEffect effect) {

        final int MIN_TURNS = 1;
        final int MAX_TURNS_SLEEP = 3;
        final int MAX_TURNS_CONFUSION = 4;
        int turns = 0;

        switch (effect) {
            // Burn, Freeze, Paralyze and Poison are all infinitely-lasting effects until removed
            // either by chance, by a Pokemon move, or by an item
            case BURN:
            case FREEZE:
            case PARALYZE:
            case POISON:
                turns = Integer.MAX_VALUE;
                break;
            case SLEEP:
                turns = ThreadLocalRandom.current().nextInt(MIN_TURNS, MAX_TURNS_SLEEP + 1) + 1;
                break;
            case CONFUSION:
                turns = ThreadLocalRandom.current().nextInt(MIN_TURNS, MAX_TURNS_CONFUSION + 1) + 1;
                break;
        }

        return turns;
    }

    public boolean isAffectedByFreeze(BattlePokemon attackingPokemon) {

        if (attackingPokemon.getStatusEffect() != StatusEffect.FREEZE) return false;

        int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
        Log.i(TAG, "Freeze roll (need > 80 to be able to attack): " + rolled);
        // There is an 80% chance to be affected by freeze
        final int FROZEN_THRESHOLD = 80;

        return (rolled <= FROZEN_THRESHOLD);
    }

    public boolean isAffectedByParalysis(BattlePokemon attackingPokemon) {

        if (attackingPokemon.getStatusEffect() != StatusEffect.PARALYZE) return false;

        int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
        Log.i(TAG, "Paralysis roll (need > 75 to be able to attack): " + rolled);
        // There is a 75% chance to be affected by paralysis
        final int PARALYSIS_THRESHOLD = 75;

        return (rolled <= PARALYSIS_THRESHOLD);
    }

    public boolean isAffectedBySleep(BattlePokemon attackingPokemon) {

        return (attackingPokemon.getStatusEffect() == StatusEffect.SLEEP && attackingPokemon.getStatusEffectTurns() > 0);
    }

    public boolean isHurtByConfusion() {

        int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
        Log.i(TAG, "Confusion self hurt roll (> 50 = hurt self): " + rolled);

        return rolled >= HURT_SELF_IN_CONFUSION_CHANCE;
    }

    public int getConfusionDamage(BattlePokemon attackingPokemon) {

        double attack = attackingPokemon.getOriginalPokemon().getAttack();
        double defense = attackingPokemon.getOriginalPokemon().getDefense();
        final double POKEMON_LEVEL = 100;

        // Formula: https://www.math.miami.edu/~jam/azure/attacks/comp/confuse.htm
        double damage = ((((((2 * POKEMON_LEVEL) / 5.0) + 2) * attack * 40.0) / defense) / 50.0) + 2;

        return (int) Math.round(damage);
    }

    public int getBurnDamage(BattlePokemon attackingPokemon) {

        return (int) Math.round(attackingPokemon.getOriginalPokemon().getHp() / 8.0);
    }

    public int getPoisonDamage(BattlePokemon attackingPokemon) {

        return (int) Math.round(attackingPokemon.getOriginalPokemon().getHp() / 8.0);
    }
}
