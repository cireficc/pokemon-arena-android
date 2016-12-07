package com.pokemonbattlearena.android.engine.match.calculators;

import com.pokemonbattlearena.android.engine.database.Move;

import java.util.concurrent.ThreadLocalRandom;

public class StageChangeCalculator {

    private static StageChangeCalculator instance = null;

    private static final String TAG = StageChangeCalculator.class.getName();

    private static final int MAX_CHANCE = 100;

    protected StageChangeCalculator() {
    }

    public static StageChangeCalculator getInstance() {

        if (instance == null) {
            instance = new StageChangeCalculator();
        }

        return instance;
    }

    public boolean doesApplyStageChange(Move move) {
        int rolled = ThreadLocalRandom.current().nextInt(MAX_CHANCE);
        return (rolled >= (MAX_CHANCE - move.getStageChangeChance()));
    }


}

