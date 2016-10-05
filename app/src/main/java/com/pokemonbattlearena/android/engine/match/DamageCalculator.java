package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.ElementalType;
import com.pokemonbattlearena.android.engine.database.Move;

import java.util.Random;

public class DamageCalculator {

    private static DamageCalculator instance = null;

    private static final String TAG = DamageCalculator.class.getName();

    private static final double[][] TYPE_MULTIPLIERS = new double[][] {
            {1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1, 0.5,   0,   1}, // Normal
            {1, 0.5, 0.5,   1,   2,   2,   1,   1,   1,   1,   1,   2, 0.5,   1, 0.5}, // Fire
            {1,   2, 0.5,   1, 0.5,   1,   1,   1,   2,   1,   1,   1,   2,   1, 0.5}, // Water
            {1,   1,   2, 0.5, 0.5,   1,   1,   1,   0,   2,   1,   1,   1,   1, 0.5}, // Electric
            {1, 0.5,   2,   1, 0.5,   1,   1, 0.5,   2, 0.5,   1, 0.5,   2,   1, 0.5}, // Grass
            {1, 0.5, 0.5,   1,   2, 0.5,   1,   1,   2,   2,   1,   1,   1,   1,   2}, // Ice
            {2,   1,   1,   1,   1,   2,   1, 0.5,   1, 0.5, 0.5, 0.5,   2,   0,   1}, // Fighting
            {1,   1,   1,   1,   2,   1,   1, 0.5, 0.5,   1,   1,   1, 0.5, 0.5,   1}, // Poison
            {1,   2,   1,   2, 0.5,   1,   1,   2,   1,   0,   1, 0.5,   2,   1,   1}, // Ground
            {1,   1,   1, 0.5,   2,   1,   2,   1,   1,   1,   1,   2, 0.5,   1,   1}, // Flying
            {1,   1,   1,   1,   1,   1,   2,   2,   1,   1, 0.5,   1,   1,   1,   1}, // Psychic
            {1, 0.5,   1,   1,   2,   1, 0.5, 0.5,   1, 0.5,   2,   1,   1, 0.5,   1}, // Bug
            {1,   2,   1,   1,   1,   2, 0.5,   1, 0.5,   2,   1,   2,   1,   1,   1}, // Rock
            {0,   1,   1,   1,   1,   1,   1,   1,   1,   1,   2,   1,   1,   2,   1}, // Ghost
            {1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   2}  // Dragon
     //      No   Fr   Wa   El   Ga   Ic   Fg   Po   Gr   Fl   Py   Bu   Ro   Gh   Dr
    };

    private static final int MAX_ACCURACY = 100;

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

    protected boolean moveHit(Move move, BattlePokemon target) {

        // TODO: Add support for accuracy stages

        // Database converts nulls to 0; null accuracy means it always hits
        if (move.getAccuracy() == 0) {
            return true;
        } else {
            Random random = new Random();
            int rolled = random.nextInt(MAX_ACCURACY);
            return (rolled >= (MAX_ACCURACY - move.getAccuracy()));
        }
    }
}
