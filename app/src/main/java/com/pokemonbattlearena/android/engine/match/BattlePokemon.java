package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Pokemon;

public class BattlePokemon {

    private Pokemon originalPokemon;
    private int currentHp;

    public BattlePokemon(Pokemon pokemon) {

        this.originalPokemon = pokemon;
        this.currentHp = pokemon.getHp();
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
}
