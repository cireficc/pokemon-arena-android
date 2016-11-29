package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import static com.pokemonbattlearena.android.engine.Logging.logSwitchResult;
import static com.pokemonbattlearena.android.engine.Logging.logSwitchResults;

public class SwitchResult extends CommandResult {


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

            if (logSwitchResult) {
                logSwitchResults();
            }
            return new SwitchResult(this);
        }
    }
}
