package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;

public class BattlePokemon {

    private Pokemon originalPokemon;
    private int currentHp;
    private boolean fainted;
    private Move[] moveSet = new Move[4];

    public BattlePokemon(Pokemon pokemon) {

        this.originalPokemon = pokemon;
        this.currentHp = pokemon.getHp();
        this.fainted = false;
    }

    public Pokemon getOriginalPokemon() {
        return originalPokemon;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public boolean isFainted() { return this.fainted; }

    public void setFainted(boolean fainted) { this.fainted = fainted; }

    public Move[] getMoveSet() { return moveSet; }

    public void setMoveSet(Move[] chosenMoves) { this.moveSet = chosenMoves; }
}
