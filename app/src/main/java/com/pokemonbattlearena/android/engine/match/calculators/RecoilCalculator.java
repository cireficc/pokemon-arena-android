package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.RecoilAmount;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import static com.pokemonbattlearena.android.engine.Logging.logGetRecoilAmount;


public class RecoilCalculator {

    //Logging flags
    public static boolean logGetRecoilAmount = false;

    private static RecoilCalculator instance = null;

    protected RecoilCalculator() {
    }

    public static RecoilCalculator getInstance() {

        if (instance == null) {
            instance = new RecoilCalculator();
        }

        return instance;
    }

    public int getRecoilAmount(BattlePokemon attacker, Move move, int damageDone) {

        RecoilAmount recoilAmount = move.getRecoilAmount();
        int recoiled = 0;

        if (recoilAmount == RecoilAmount.ONEFOURTH) {
            recoiled = Math.round(damageDone / 4);
        }
        else if (recoilAmount == RecoilAmount.ONETHIRD) {
            recoiled = Math.round(damageDone / 3);
        }
        else {
            recoiled = getCrashAmount(attacker);
            return recoiled;
        }

        if(logGetRecoilAmount) {
            logGetRecoilAmount(recoilAmount, move.getName(), recoiled);
        }
        return recoiled;
    }

    private int getCrashAmount(BattlePokemon attacker){
        return Math.round(attacker.getOriginalPokemon().getHp() / 2);
    }
}
