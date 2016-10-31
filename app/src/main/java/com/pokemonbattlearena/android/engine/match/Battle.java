package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Battle {

    private transient static final String TAG = Battle.class.getName();

    // NOTE: self is always the host of the battle
    BattlePokemonPlayer self;
    BattlePokemonPlayer opponent;
    List<BattlePhase> finishedBattlePhases;
    transient BattlePhase currentBattlePhase;
    transient boolean isFinished;

    public Battle() { }

    public Battle(PokemonPlayer player1, PokemonPlayer player2) {
        this.self = new BattlePokemonPlayer(player1);
        this.opponent = new BattlePokemonPlayer(player2);
        this.finishedBattlePhases = new ArrayList<>();
        this.currentBattlePhase = new BattlePhase(self, opponent);
        this.isFinished = false;
    }

    public BattlePokemonPlayer getSelf() {
        return self;
    }

    public BattlePokemonPlayer getOpponent() {
        return opponent;
    }

    public List<BattlePhase> getFinishedBattlePhases() {
        return finishedBattlePhases;
    }

    public BattlePhase getCurrentBattlePhase() {
        return currentBattlePhase;
    }

    public boolean isFinished() {
        return isFinished;
    }

    private void setFinished() {
        isFinished = self.getBattlePokemonTeam().allFainted() || opponent.getBattlePokemonTeam().allFainted();
    }

    public void startNewBattlePhase() {

        Log.i(TAG, "Starting new battle phase");
        finishedBattlePhases.add(currentBattlePhase);
        Log.i(TAG, "Added current battle phase to finished phases");
        Log.i(TAG, "Created new battle phase");
        currentBattlePhase = new BattlePhase(self, opponent);
    }
}
