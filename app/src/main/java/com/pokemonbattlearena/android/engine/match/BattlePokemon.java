package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Pokemon;

public class BattlePokemon extends Pokemon {

    private int currentHp;

    public BattlePokemon() {
        this.currentHp = getHp();
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
}
