package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.ElementalType;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.MoveType;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import java.util.concurrent.ThreadLocalRandom;

import static com.pokemonbattlearena.android.engine.Logging.logCalculateDamage;
import static com.pokemonbattlearena.android.engine.Logging.logGetTimesHit;

public class DamageCalculator {

    //Log flags
    public static boolean logGetTimesHit = false;
    public static boolean logCalculateDamage = false;

    private static DamageCalculator instance = null;

    private static final String TAG = DamageCalculator.class.getName();

    //Row type effectiveness vs Column type
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

    public double getType1Effectiveness(Move move, BattlePokemon target) {

        int moveIndex = move.getElementalType1().ordinal();
        int targetIndex = target.getOriginalPokemon().getElementalType1().ordinal();

        return TYPE_MULTIPLIERS[moveIndex][targetIndex];
    }

    public double getType2Effectiveness(Move move, BattlePokemon target) {

        int moveIndex = move.getElementalType1().ordinal();
        ElementalType type2 = target.getOriginalPokemon().getElementalType2();

        if (type2 != null) {
            int targetIndex = type2.ordinal();
            return TYPE_MULTIPLIERS[moveIndex][targetIndex];
        } else {
            return 1;
        }
    }

    public double getOverallTypeEffectiveness(Move move, BattlePokemon target) {
        return getType1Effectiveness(move, target) * getType2Effectiveness(move, target);
    }

    public boolean moveHit(Move move, BattlePokemon target) {

        // TODO: Add support for accuracy stages

        // Database converts nulls to 0; null accuracy means it always hits
        if (move.getAccuracy() == 0) {
            return true;
        } else {
            int rolled = ThreadLocalRandom.current().nextInt(MAX_ACCURACY);
            return (rolled >= (MAX_ACCURACY - move.getAccuracy()));
        }
    }

    public int getTimesHit(Move move){
        int hits = ThreadLocalRandom.current().nextInt(move.getMinHits(), move.getMaxHits() + 1);

        if (logGetTimesHit) {
            logGetTimesHit(hits, move.getMinHits(), move.getMaxHits());
        }

        return hits;
    }

    public int calculateDamage(BattlePokemon attacker, Move move, BattlePokemon target) {

        Pokemon originalAttacker = attacker.getOriginalPokemon();
        Pokemon originalTarget = target.getOriginalPokemon();
        final int POKEMON_LEVEL = 100;


        switch(move.getName()) {
            case "Night Shade":
            case "Seismic Toss":
                return POKEMON_LEVEL;
            case "Dragon Rage":
                return 40;
            case "Sonic Boom":
                return 20;
            case "Psywave":
                //damage = Attacking Pokemon's Level * (50% to 150%)
                return Math.round(POKEMON_LEVEL * (ThreadLocalRandom.current().nextInt(50, 150 + 1) / 100));
            case "Super Fang":
                return Math.round(target.getCurrentHp() / 2);
        }

        // Database converts nulls to 0; null power means it does 0 damage
        if (move.getPower() == 0) {
            Log.d(TAG, "Move power was 0; does 0 damage");
            return 0;
        }

        double attack = (move.getMoveType() == MoveType.PHYSICAL) ? originalAttacker.getAttack() : originalAttacker.getSpecialAttack();
        double defense = (move.getMoveType() == MoveType.PHYSICAL) ? originalTarget.getDefense() : originalTarget.getSpecialDefense();

        double levelCalc = ((2 * POKEMON_LEVEL) + 10) / 250.0;
        double statCalc = attack / defense;
        double basePower = move.getPower();

        double damage = (levelCalc * statCalc * basePower) + 2;

        boolean type1Match = (move.getElementalType1() == originalAttacker.getElementalType1());
        boolean type2Match = (move.getElementalType1() == originalAttacker.getElementalType2());
        double stabBonus = (type1Match || type2Match) ? 1.5 : 1;

        double typeEffectiveness = getOverallTypeEffectiveness(move, target);

        double critMultiplier = 1;
        final int MAX = 100;
        final int MIN = 85;
        double roll = (ThreadLocalRandom.current().nextInt((MAX - MIN) + 1) + MIN) / 100.0;

        double modifier = stabBonus * typeEffectiveness * critMultiplier * roll;

        double totalDamage = damage * modifier;
        int totalDamageRounded = (int) Math.round(totalDamage);

        if(logCalculateDamage) {
            logCalculateDamage(move.getName(), originalTarget.getName(), attack, defense, levelCalc, statCalc, basePower, damage, stabBonus, getType1Effectiveness(move, target), getType2Effectiveness(move, target), typeEffectiveness, critMultiplier, roll, modifier, totalDamage, totalDamageRounded);
        }

        return totalDamageRounded;
    }
}
