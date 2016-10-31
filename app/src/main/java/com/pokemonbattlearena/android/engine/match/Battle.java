package com.pokemonbattlearena.android.engine.match;

import java.util.ArrayList;
import java.util.List;

public class Battle {

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

    public boolean isFinished() {
        return isFinished;
    }

    private void setFinished() {
        isFinished = self.getBattlePokemonTeam().allFainted() || opponent.getBattlePokemonTeam().allFainted();
    }
}
