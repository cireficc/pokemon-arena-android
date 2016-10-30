package com.pokemonbattlearena.android.engine.match.calculators;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import java.util.concurrent.ThreadLocalRandom;

public class StatusEffectCalculator {

    private static StatusEffectCalculator instance = null;

    private static final String TAG = StatusEffectCalculator.class.getName();

    private static final int MAX_CHANCE = 100;

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
                turns = ThreadLocalRandom.current().nextInt(MIN_TURNS, MAX_TURNS_SLEEP + 1);
                break;
            case CONFUSION:
                turns = ThreadLocalRandom.current().nextInt(MIN_TURNS, MAX_TURNS_CONFUSION + 1);
                break;
        }

        return turns;
    }
}
