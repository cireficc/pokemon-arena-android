package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

public class SwitchResult extends CommandResult {

    private transient static final String TAG = SwitchResult.class.getName();

    private int positionOfPokemon;

    private SwitchResult(Builder builder) {

        super();

        this.targetInfo = builder.targetInfo;
        this.positionOfPokemon = builder.positionOfPokemon;
    }

    public int getPositionOfPokemon() {
        return positionOfPokemon;
    }

    protected static class Builder {

        private TargetInfo targetInfo;
        private int positionOfPokemon;

        protected Builder(TargetInfo targetInfo) {
            this.targetInfo = targetInfo;
        }

        protected Builder setPositionOfPokemon(int positionOfPokemon) {
            this.positionOfPokemon = positionOfPokemon;
            return this;
        }

        protected SwitchResult build() {

            //Log.i(TAG, "Building SwitchResult");
            return new SwitchResult(this);
        }
    }
}
