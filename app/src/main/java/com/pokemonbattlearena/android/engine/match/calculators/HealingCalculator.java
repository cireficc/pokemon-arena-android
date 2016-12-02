package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.SelfHealAmount;
import com.pokemonbattlearena.android.engine.database.SelfHealType;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

public class HealingCalculator {

    private static HealingCalculator instance = null;
    private static final String TAG = HealingCalculator.class.getName();

    protected HealingCalculator() {
    }

    public static HealingCalculator getInstance() {

        if (instance == null) {
            instance = new HealingCalculator();
        }

        return instance;
    }

    public int getHealAmount(BattlePokemon attacker, Move move, int damageDone) {

        SelfHealType healType = move.getSelfHealType();
        int healed = 0;

        if (healType == SelfHealType.DIRECT) {
            healed = getDirectHealAmount(attacker, move);
            //Log.i(TAG, move.getName() + " is a direct heal. Healing for " + healed + " HP");
        }

        if (healType == SelfHealType.ABSORB) {
            healed = getAbsorbHealAmount(move, damageDone);
            //Log.i(TAG, move.getName() + " is an absorb heal. Healing for " + healed + " HP");
        }

        return healed;
    }

    private int getDirectHealAmount(BattlePokemon attacker, Move move) {

        SelfHealAmount healAmount = move.getSelfHealAmount();

        if (healAmount == SelfHealAmount.HALF) {
            //Log.i(TAG, "Direct heal is 50% of user's max HP");
            return Math.round(attacker.getOriginalPokemon().getHp() / 2);
        }

        return 0;
    }

    private int getAbsorbHealAmount(Move move, int damageDone) {

        SelfHealAmount healAmount = move.getSelfHealAmount();

        if (healAmount == SelfHealAmount.HALF) {
            //Log.i(TAG, "Absorb heal is 50% of damage done");
            return Math.round(damageDone / 2);
        }

        return 0;
    }
}
