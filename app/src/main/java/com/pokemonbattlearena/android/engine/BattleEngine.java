package com.pokemonbattlearena.android.engine;

public class BattleEngine {

    private static BattleEngine instance = null;

    protected BattleEngine() {
    }

    public static BattleEngine getInstance() {

        if (instance == null) {
            instance = new BattleEngine();
        }

        return instance;
    }
}
