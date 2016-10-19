package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;

import java.util.Random;

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

    protected boolean doesApplyFlinch(Move move) {

        if (!move.canFlinch()) {
            return false;
        } else {
            // Always 15% chance to flinch
            final int FLINCH_CHANCE = 15;
            Random random = new Random();
            int rolled = random.nextInt(MAX_CHANCE);
            return (rolled >= (MAX_CHANCE - FLINCH_CHANCE));
        }
    }
}
