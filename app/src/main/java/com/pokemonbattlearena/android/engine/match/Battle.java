package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
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

    public BattlePhaseResult executeCurrentBattlePhase() {

        Log.i(TAG, "Executing current battle phase from Battle");

        Log.i(TAG, "Sorting commands by priority");
        Collections.sort(currentBattlePhase.getCommands(), BattlePhase.getCommandComparator());
        BattlePhaseResult battlePhaseResult = new BattlePhaseResult();

        for (Command command : currentBattlePhase.getCommands()) {
            Log.i(TAG, "Executing command of type: " + command.getClass());

            CommandResult commandResult = command.execute();

            Log.i(TAG, "Adding command result to battle phase result");
            battlePhaseResult.addCommandResult(commandResult);
        }

        Log.i(TAG, "Setting battle phase result on current battle phase");
        currentBattlePhase.setBattlePhaseResult(battlePhaseResult);

        Log.i(TAG, "Setting finished");
        setFinished();
        Log.i(TAG, "Battle finished? " + isFinished);

        return battlePhaseResult;
    }

    public void applyCommandResult(CommandResult commandResult) {

        if (commandResult instanceof AttackResult) {
            Log.i(TAG, "Applying command result of type AttackResult");
            applyAttackResult((AttackResult) commandResult);
        }
    }

    private void applyAttackResult(AttackResult attackResult) {

        TargetInfo targetInfo = attackResult.getTargetInfo();

        String attackingPlayerId = targetInfo.getAttackingPlayer().getId();
        BattlePokemonPlayer attackingPlayer = getPlayerFromId(attackingPlayerId);
        BattlePokemon attackingPokemon = attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        String defendingPlayerId = targetInfo.getDefendingPlayer().getId();
        BattlePokemonPlayer defendingPlayer = getPlayerFromId(defendingPlayerId);
        BattlePokemon defendingPlayerPokemon = defendingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        Log.i(TAG, "Attacking player: " + attackingPlayerId);
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());
        Log.i(TAG, "Defending player: " + defendingPlayerId);
        Log.i(TAG, "Defending player pkmn: " + defendingPlayerPokemon.getOriginalPokemon().getName());
        
        // TODO: Finish implementing this method

    }

    private BattlePokemonPlayer getPlayerFromId(String id) {
        if (self.getId().equals(id)) {
            return self;
        } else {
            return opponent;
        }
    }
}
