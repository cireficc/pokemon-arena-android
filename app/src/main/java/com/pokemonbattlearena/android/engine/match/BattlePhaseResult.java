package com.pokemonbattlearena.android.engine.match;

import java.util.ArrayList;
import java.util.List;

public class BattlePhaseResult {

    private List<CommandResult> commandResults;

    BattlePhaseResult() {
        this.commandResults = new ArrayList<>();
    }

    public List<CommandResult> getCommandResults() {
        return commandResults;
    }

    public void addCommandResult(CommandResult result) {

        this.commandResults.add(result);
    }
}
