package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;

/**
 * Created by nathan on 10/29/16.
 */

public class DummyCommandResult {

    public String moveName;
    public double movePower;

    public DummyCommandResult(Move move) {
        this.moveName = move.getName();
        this.movePower = move.getPower();
    }

    public double getMovePower() {
        return movePower;
    }

    public String getMoveName() {
        return moveName;
    }

    public void setMoveName(String moveName) {
        this.moveName = moveName;
    }

    public void setMovePower(double movePower) {
        this.movePower = movePower;
    }

    @Override
    public String toString() {
        return "Move: " + moveName + ", Power: " + movePower;
    }
}
