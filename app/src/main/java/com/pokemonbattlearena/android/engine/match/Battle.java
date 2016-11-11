package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

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
        if (player2 instanceof AiPlayer) {
            this.opponent = ((AiPlayer) player2).getAiBattler();
            //TODO actually get player moves into the Pokemon Player
            setPlayerMoves(((AiPlayer) player2).db);
        } else {
            this.opponent = new BattlePokemonPlayer(player2);
        }

        this.finishedBattlePhases = new ArrayList<>();
        this.currentBattlePhase = new BattlePhase(self, opponent);
    }

    public void swap() {
        BattlePokemonPlayer temp = self;
        self = opponent;
        opponent = temp;
    }

    //TODO kill this method at all costs.
    private void setPlayerMoves(Database db) {
        Pokemon org = self.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
        List<Move> moves = new ArrayList<Move>();
        for (int i = 0; i < 4; i++) {
            moves.add(i, db.getMovesForPokemon(org).get(i));
        }
        self.getBattlePokemonTeam().getCurrentPokemon().setMoveSet(moves);
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

    private void applyAttackResult(AttackResult res) {

        TargetInfo targetInfo = res.getTargetInfo();

        String attackingPlayerId = targetInfo.getAttackingPlayer().getId();
        BattlePokemonPlayer attackingPlayer = getPlayerFromId(attackingPlayerId);
        BattlePokemon attackingPokemon = attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        String defendingPlayerId = targetInfo.getDefendingPlayer().getId();
        BattlePokemonPlayer defendingPlayer = getPlayerFromId(defendingPlayerId);
        BattlePokemon defendingPokemon = defendingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        Log.i(TAG, "Attacking player: " + attackingPlayerId);
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());
        Log.i(TAG, "Defending player ID: " + defendingPlayer.getId() + "Defending ID: " + defendingPlayerId);
        Log.i(TAG, "Defending player pkmn: " + defendingPokemon.getOriginalPokemon().getName());

        int damageDone = res.getDamageDone();
        StatusEffect statusEffectApplied = res.getStatusEffectApplied();
        int statusEffectTurns = res.getStatusEffectTurns();
        boolean confused = res.isConfused();
        int confusedTurns = res.getConfusedTurns();
        boolean flinched = res.isFlinched();
        int chargingTurns = res.getChargingTurns();
        int rechargingTurns = res.getRechargingTurns();
        int healingDone = res.getHealingDone();
        int recoilTaken = res.getRecoilTaken();

        Log.i(TAG, "Applying damage done: " + damageDone);
        defendingPokemon.setCurrentHp(attackingPokemon.getCurrentHp() - damageDone);

        Log.i(TAG, "Applying StatusEffect (maybe): " + statusEffectApplied);
        // If the Pokemon doesn't already have a StatusEffect, we can apply one
        if (defendingPokemon.getStatusEffect() == null && statusEffectApplied != null) {
            Log.i(TAG, "Pokemon doesn't already have a StatusEffect. Applying for " + statusEffectTurns + " turn(s)!");
            defendingPokemon.setStatusEffect(statusEffectApplied);
            defendingPokemon.setStatusEffectTurns(res.getStatusEffectTurns());
        }

        Log.i(TAG, "Applying Confusion (maybe): " + confused);
        if (!defendingPokemon.isConfused() && confused) {
            Log.i(TAG, "Pokemon is not already confused. Applying for " + confusedTurns + " turn(s)!");
        }

        Log.i(TAG, "Applying flinch: " + flinched);
        defendingPokemon.setFlinched(flinched);

        Log.i(TAG, "Applying charging (maybe): " + chargingTurns);
        if (!defendingPokemon.isCharging()) {
            Log.i(TAG, "Pokemon is not already charging. Applying for " + chargingTurns + " turn(s)!");
        }

        Log.i(TAG, "Applying recharging (maybe): " + rechargingTurns);
        if (!defendingPokemon.isRecharging()) {
            Log.i(TAG, "Pokemon is not already recharging. Applying for " + rechargingTurns + " turn(s)!");
        }

        int maxHp = attackingPokemon.getOriginalPokemon().getHp();
        int currentHp = attackingPokemon.getCurrentHp();
        int healedTo = currentHp + healingDone;

        Log.i(TAG, "Applying healing done: " + healingDone);

        if (healedTo >= maxHp) {
            Log.i(TAG, "Healing was over max HP; healing to max HP: " + maxHp);
            attackingPokemon.setCurrentHp(maxHp);
        } else {
            Log.i(TAG, "Healing was not over max HP; healing to " + healedTo);
            attackingPokemon.setCurrentHp(healedTo);
        }

        Log.i(TAG, "Applying recoil taken: " + recoilTaken);
        attackingPokemon.setCurrentHp(currentHp - recoilTaken);

        boolean attackerFainted = attackingPokemon.getCurrentHp() <= 0;
        boolean defenderFainted = defendingPokemon.getCurrentHp() <= 0;

        Log.i(TAG, "Applying fainted status. Attacker fainted? " + attackerFainted + " || defender fainted? " + defenderFainted);
        attackingPokemon.setFainted(attackerFainted);
        defendingPokemon.setFainted(defenderFainted);
    }

    private BattlePokemonPlayer getPlayerFromId(String id) {
        if (self.getId().equals(id)) {
            return self;
        } else {
            return opponent;
        }
    }
}
