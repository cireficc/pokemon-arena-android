package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by nathan on 12/3/16.
 */

public class NoPResult extends CommandResult {

    private NoPResult (Builder builder) {
        super();
        this.targetInfo = builder.targetInfo;
    }
    protected static class Builder {

        private TargetInfo targetInfo;

        protected Builder(TargetInfo targetInfo) {
            this.targetInfo = targetInfo;
        }

        protected NoPResult build() {

            Log.i(TAG, "Building NoPResult");
            return new NoPResult(this);
        }
    }
}
