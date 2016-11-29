package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.SelfHealAmount;
import com.pokemonbattlearena.android.engine.database.SelfHealType;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import static com.pokemonbattlearena.android.engine.Logging.logAbsorbHealAmount;
import static com.pokemonbattlearena.android.engine.Logging.logDirectHealAmount;
import static com.pokemonbattlearena.android.engine.Logging.logGetHealAmount;

public class HealingCalculator {


    private static HealingCalculator instance = null;

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
        }

        if (healType == SelfHealType.ABSORB) {
            healed = getAbsorbHealAmount(move, damageDone);
        }

        if(logGetHealAmount) {
            logGetHealAmount(healType, healed, move.getName());
        }
        return healed;
    }

    private int getDirectHealAmount(BattlePokemon attacker, Move move) {

        SelfHealAmount healAmount = move.getSelfHealAmount();

        if (healAmount == SelfHealAmount.HALF) {
            return Math.round(attacker.getOriginalPokemon().getHp() / 2);
        }

        if(logDirectHealAmount) {
            logDirectHealAmount(healAmount);
        }
        return 0;
    }

    private int getAbsorbHealAmount(Move move, int damageDone) {

        SelfHealAmount healAmount = move.getSelfHealAmount();

        if (healAmount == SelfHealAmount.HALF) {
            return Math.round(damageDone / 2);
        }

        if(logAbsorbHealAmount) {
            logAbsorbHealAmount(healAmount);
        }
        return 0;
    }
}
