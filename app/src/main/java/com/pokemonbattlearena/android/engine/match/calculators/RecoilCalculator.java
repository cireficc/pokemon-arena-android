package com.pokemonbattlearena.android.engine.match.calculators;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.RecoilAmount;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;


public class RecoilCalculator {

    private static RecoilCalculator instance = null;
    private static final String TAG = RecoilCalculator.class.getName();

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
            Log.i(TAG, move.getName() + " is a crash move. Attacker takes " + recoiled + " damage");
            return recoiled;
        }

        Log.i(TAG, move.getName() + " is a recoil move. Attacker takes " + recoiled + " damage");
        return recoiled;
    }

    private int getCrashAmount(BattlePokemon attacker){
        return Math.round(attacker.getOriginalPokemon().getHp() / 2);
    }
}
