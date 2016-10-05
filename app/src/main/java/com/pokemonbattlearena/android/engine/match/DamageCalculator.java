package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.BattleEngine;
import com.pokemonbattlearena.android.engine.database.ElementalType;
import com.pokemonbattlearena.android.engine.database.Move;

public class DamageCalculator {

    private static DamageCalculator instance = null;

    private static final String TAG = DamageCalculator.class.getName();

    private static final double[][] TYPE_MULTIPLIERS = new double[][] {
            {1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1, 0.5,   0,   1},
            {1, 0.5, 0.5,   1,   2,   2,   1,   1,   1,   1,   1,   2, 0.5,   1, 0.5},
            {1,   2, 0.5,   1, 0.5,   1,   1,   1,   2,   1,   1,   1,   2,   1, 0.5},
            {1,   1,   2, 0.5, 0.5,   1,   1,   1,   0,   2,   1,   1,   1,   1, 0.5},
            {1, 0.5,   2,   1, 0.5,   1,   1, 0.5,   2, 0.5,   1, 0.5,   2,   1, 0.5},
            {1, 0.5, 0.5,   1,   2, 0.5,   1,   1,   2,   2,   1,   1,   1,   1,   2},
            {2,   1,   1,   1,   1,   2,   1, 0.5,   1, 0.5, 0.5, 0.5,   2,   0,   1},
            {1,   1,   1,   1,   2,   1,   1, 0.5, 0.5,   1,   1,   1, 0.5, 0.5,   1},
            {1,   2,   1,   2, 0.5,   1,   1,   2,   1,   0,   1, 0.5,   2,   1,   1},
            {1,   1,   1, 0.5,   2,   1,   2,   1,   1,   1,   1,   2, 0.5,   1,   1},
            {1,   1,   1,   1,   1,   1,   2,   2,   1,   1, 0.5,   1,   1,   1,   1},
            {1, 0.5,   1,   1,   2,   1, 0.5, 0.5,   1, 0.5,   2,   1,   1, 0.5,   1},
            {1,   2,   1,   1,   1,   2, 0.5,   1, 0.5,   2,   1,   2,   1,   1,   1},
            {0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   2,   1,   1,   2,   1},
            {1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   2}
    };

    protected DamageCalculator() {
    }

    public static DamageCalculator getInstance() {

        if (instance == null) {
            instance = new DamageCalculator();
        }

        return instance;
    }

    protected double getType1Effectiveness(Move move, BattlePokemon target) {

        int moveIndex = move.getElementalType1().ordinal();
        int targetIndex = target.getOriginalPokemon().getElementalType1().ordinal();

        return TYPE_MULTIPLIERS[moveIndex][targetIndex];
    }

    protected double getType2Effectiveness(Move move, BattlePokemon target) {

        int moveIndex = move.getElementalType1().ordinal();
        ElementalType type2 = target.getOriginalPokemon().getElementalType2();

        if (type2 != null) {
            int targetIndex = type2.ordinal();
            return TYPE_MULTIPLIERS[moveIndex][targetIndex];
        } else {
            return 1;
        }
    }

    protected double getOverallTypeEffectiveness(Move move, BattlePokemon target) {
        return getType1Effectiveness(move, target) * getType2Effectiveness(move, target);
    }
}
