package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.ai.AiBattle;
import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.Database;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Battle {

    private transient static final String TAG = Battle.class.getName();

    // NOTE: self is always the host of the battle
    BattlePokemonPlayer self;
    BattlePokemonPlayer opponent;
    List<BattlePhase> finishedBattlePhases = new ArrayList<>();
    transient BattlePhase currentBattlePhase;
    transient boolean isFinished;

    public Battle() {
    }

    protected Battle(PokemonPlayer player1, PokemonPlayer player2) {
        this(
                new BattlePokemonPlayer(player1),
                (player2 instanceof  AiPlayer ? ((AiPlayer) player2).getAiBattler() : new BattlePokemonPlayer(player2))
        );
    }

    //AI is always opponent.
    public Battle(BattlePokemonPlayer self, BattlePokemonPlayer opponent) {
        this.self = self;
        this.opponent = opponent;
        this.currentBattlePhase = new BattlePhase(self, opponent);
    }

    public static Battle createBattle(PokemonPlayer player1, PokemonPlayer player2) {
        return new Battle (player1, player2);
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

        Log.e(TAG, "Starting new battle phase");
        finishedBattlePhases.add(currentBattlePhase);
        Log.i(TAG, "Added current battle phase to finished phases");
        Log.i(TAG, "Created new battle phase");
        currentBattlePhase = new BattlePhase(self, opponent);
    }

    /*
     * A custom Comparator to determine the order of commands (player actions).
     * Pokemon switching always occurs first. Attack order is determined by the
     * Pokemon's speed - the faster Pokemon attacks first. However, some moves
     * such as Quick Attack will give the attacker priority in the queue.
     */
    private transient Comparator<Command> commandComparator = new Comparator<Command>() {
        @Override
        public int compare(Command c1, Command c2) {

            // Pokemon switching always happens first
            if (c1 instanceof Switch ) {
                return Integer.MIN_VALUE;
            }
            else if (c2 instanceof Switch) {
                return Integer.MAX_VALUE;
            } else if (c1 instanceof NoP|| c2 instanceof NoP) {
                return Integer.MAX_VALUE;
            }

            Attack a1 = (Attack) c1;
            Attack a2 = (Attack) c2;
            int pokemon1Speed = a1.getAttackingPokemon(Battle.this).getOriginalPokemon().getSpeed();
            int pokemon2Speed = a2.getAttackingPokemon(Battle.this).getOriginalPokemon().getSpeed();
            Log.i(TAG, "Pokemon 1 speed: " + pokemon1Speed + " || Pokemon 2 speed: " + pokemon2Speed);

            return pokemon2Speed - pokemon1Speed;
        }
    };


    public BattlePhaseResult executeCurrentBattlePhase() {

        Log.i(TAG, "Executing current battle phase from Battle");

        Log.i(TAG, "Sorting commands by priority");
        Collections.sort(currentBattlePhase.getCommands(), commandComparator);
        BattlePhaseResult battlePhaseResult = new BattlePhaseResult();
        CommandResult commandResult;
        boolean skipFaintedPokemon = false;

        verifySwitchAttack();

        for (Command command : currentBattlePhase.getCommands()) {
            if (!isFinished()) {
                Log.i(TAG, "Executing command of type: " + command.getClass());
                if (command instanceof NoP) {
                    commandResult = command.execute(this);
                }
                if (command instanceof Switch) {
                    commandResult = command.execute(this);
                } else {
                    commandResult = command.execute(this);
                }

                if (commandResult instanceof AttackResult) {
                    if (skipFaintedPokemon) {
                        Log.i(TAG, "Attacking Pokemon is fainted, do not add its attack");
                    } else if (((AttackResult) commandResult).isFainted() && !skipFaintedPokemon) {
                        skipFaintedPokemon = true;
                        Log.i(TAG, "Adding command result to battle phase result");
                        battlePhaseResult.addCommandResult(commandResult);
                    } else {
                        battlePhaseResult.addCommandResult(commandResult);
                    }
                } else {
                    battlePhaseResult.addCommandResult(commandResult);
                }

                Log.i(TAG, "Checking if battle is finished");
                setFinished();
            }
        }

        Log.i(TAG, "Setting battle phase result on current battle phase");
        currentBattlePhase.setBattlePhaseResult(battlePhaseResult);

        Log.i(TAG, "Battle finished? " + isFinished);

        return battlePhaseResult;
    }

    private void verifySwitchAttack() {
        if (currentBattlePhase.getCommands().get(0) instanceof Switch) {
            if (!(currentBattlePhase.getCommands().get(1) instanceof Switch)) {
                Switch switchAction = (Switch)currentBattlePhase.getCommands().get(0);
                Log.i(TAG, "Pokemon on Deck: " + switchAction.pokemonSwitchingTo(this));
                int indexOfDefender = switchAction.getPositionToSwitchTo();
                switchAction.getAttackingBattlePlayer(this).getBattlePokemonTeam().setPokemonOnDeck(indexOfDefender);
            }
        }
    }

    public void applyCommandResult(CommandResult commandResult) {

        if (commandResult instanceof NoPResult) {
            return;
        }
        if (commandResult instanceof AttackResult) {
            Log.i(TAG, "Applying command result of type AttackResult");
            applyAttackResult((AttackResult) commandResult);
        } else if (commandResult instanceof SwitchResult) {
            Log.i(TAG, "Applying command result of type SwitchResult");
            applySwitchResult((SwitchResult) commandResult);
        }
        setFinished();
    }

    private void applyAttackResult(AttackResult res) {

        TargetInfo targetInfo = res.getTargetInfo();

        String attackingPlayerId = targetInfo.getAttackingPlayer().getId();
        BattlePokemonPlayer attackingPlayer = getPlayerFromId(attackingPlayerId);
        BattlePokemon attackingPokemon = attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        String defendingPlayerId = targetInfo.getDefendingPlayer().getId();
        BattlePokemonPlayer defendingPlayer = getPlayerFromId(defendingPlayerId);
        BattlePokemon defendingPokemon = defendingPlayer.getBattlePokemonTeam().getCurrentPokemon();


        // Log the important things for the user
        Pokemon attackingOriginal = attackingPokemon.getOriginalPokemon();
        Pokemon defendingOriginal = defendingPokemon.getOriginalPokemon();
        res.appendToLog(attackingOriginal.getName() + " used " + res.getMoveUsed().getName());

        if (!res.isMoveHit()) {
            res.appendToLog(attackingOriginal.getName() + "'s attack missed!");
        }

        if (res.isFlinched()) {
            res.appendToLog(attackingOriginal.getName() + " flinched!");
        }
        else if (res.isSuccumbedToStatusEffect()) {
            res.appendToLog(attackingOriginal.getName() + " succumbed to " + attackingPokemon.getStatusEffect());
        }

        if (res.getConfusionDamageTaken() > 0) {
            res.appendToLog(attackingOriginal.getName() + " is confused. It hurt itself in confusion for " + res.getConfusionDamageTaken() + " damage");
        }

        if (!res.isSuccumbedToStatusEffect() && res.getDamageDone() > 0) {
            res.appendToLog(attackingOriginal.getName() + " did " + res.getDamageDone() + " damage");
        }

        if (res.getStatusEffectApplied() != null) {
            res.appendToLog(defendingOriginal.getName() + " had "  + res.getStatusEffectApplied() + " applied to it");
        }

        if (res.isConfused()) {
            res.appendToLog(defendingOriginal.getName() + " had confusion applied to it");
        }

        if (res.getHealingDone() > 0) {
            res.appendToLog(attackingOriginal.getName() + " healed itself for " + res.getHealingDone() + " HP");
        }

        if (res.getRecoilTaken() > 0 ) {
            res.appendToLog(attackingOriginal.getName() + " took " + res.getRecoilTaken() + " recoil damage");
        }

        if (res.getBurnDamageTaken() > 0) {
            res.appendToLog(attackingOriginal.getName() + " took " + res.getBurnDamageTaken() + " burn damage");
        }

        if (res.getPoisonDamageTaken() > 0) {
            res.appendToLog(attackingOriginal.getName() + " took " + res.getPoisonDamageTaken() + " poison damage");
        }

        Log.i(TAG, "Attacking player: " + attackingPlayerId);
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());
        Log.i(TAG, "Defending player ID: " + defendingPlayer.getId() + "Defending ID: " + defendingPlayerId);
        Log.i(TAG, "Defending player pkmn: " + defendingPokemon.getOriginalPokemon().getName());

        if (res.isSuccumbedToStatusEffect()) {
            Log.i(TAG, attackingPokemon.getOriginalPokemon().getName() + " succumbed to " + attackingPokemon.getStatusEffect());
            return;
        }

        if (res.isUnfroze()) {
            Log.i(TAG, attackingPokemon.getOriginalPokemon().getName() + " unfroze!");
            attackingPokemon.setStatusEffect(null);
            attackingPokemon.setStatusEffectTurns(0);
        }

        Log.i(TAG, "Move hit: " + res.isMoveHit());

        if (!res.isMoveHit()) {
            Log.i(TAG, "Move missed!");
             return;
        }

        int damageDone = res.getDamageDone();
        StatusEffect statusEffectApplied = res.getStatusEffectApplied();
        int statusEffectTurns = res.getStatusEffectTurns();
        boolean confused = res.isConfused();
        int confusedTurns = res.getConfusedTurns();
        int confusionDamageTaken = res.getConfusionDamageTaken();
        boolean flinched = res.isFlinched();
        int chargingTurns = res.getChargingTurns();
        int rechargingTurns = res.getRechargingTurns();
        int healingDone = res.getHealingDone();
        int recoilTaken = res.getRecoilTaken();

        Log.i(TAG, "Applying damage done: " + damageDone);
        defendingPokemon.setCurrentHp(defendingPokemon.getCurrentHp() - damageDone);

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
            defendingPokemon.setConfused(confused);
            defendingPokemon.setConfusedTurns(confusedTurns);
        }

        Log.i(TAG, "Pokemon hurt itself in confusion? Damage taken: " + confusionDamageTaken);
        attackingPokemon.setCurrentHp(attackingPokemon.getCurrentHp() - confusionDamageTaken);

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
        attackingPokemon.setCurrentHp(attackingPokemon.getCurrentHp() - recoilTaken);

        int attackStage = res.getAttackStageChange();
        int defenseStage = res.getDefenseStageChange();
        int spAttackStage = res.getSpAttackStageChange();
        int spDefenseStage = res.getSpDefenseStageChange();
        int speedStage = res.getSpeedStageChange();
        int critStage = res.getCritStageChange();

        if (attackStage >= 0) {
            attackingPokemon.setAttackStage(attackingPokemon.getAttackStage() + attackStage);
            Log.i(TAG, "Attack Stage +" + attackStage);
            Log.i(TAG, "Attack Stage =" + attackingPokemon.getAttackStage());
        } else {
            defendingPokemon.setAttackStage(defendingPokemon.getAttackStage() + attackStage);
            Log.i(TAG, "Attack Stage " + attackStage);
            Log.i(TAG, "Attack Stage =" + defendingPokemon.getAttackStage());
        }

        if (defenseStage >= 0) {
            attackingPokemon.setDefenseStage(attackingPokemon.getDefenseStage() + defenseStage);
            Log.i(TAG, "Defense Stage +" + defenseStage + " Defense Stage =" + attackingPokemon.getDefenseStage());
        } else {
            defendingPokemon.setDefenseStage(defendingPokemon.getDefenseStage() + defenseStage);
            Log.i(TAG, "Defense Stage " + defenseStage + " Defense Stage =" + defendingPokemon.getDefenseStage());
        }

        if (spAttackStage >= 0) {
            attackingPokemon.setSpAttackStage(attackingPokemon.getSpAttackStage() + spAttackStage);
            Log.i(TAG, "SpAttack Stage +" + spAttackStage + " SpAttack Stage =" + attackingPokemon.getSpAttackStage());
        } else {
            defendingPokemon.setSpAttackStage(defendingPokemon.getSpAttackStage() + spAttackStage);
            Log.i(TAG, "SpAttack Stage " + spAttackStage + " SpAttack Stage =" + defendingPokemon.getSpAttackStage());
        }

        if (spDefenseStage >= 0) {
            attackingPokemon.setSpDefenseStage(attackingPokemon.getSpDefenseStage() + spDefenseStage);
            Log.i(TAG, "SpDefense Stage +" + spDefenseStage + " SpDefense Stage =" + attackingPokemon.getSpDefenseStage());
        } else {
            defendingPokemon.setSpDefenseStage(defendingPokemon.getSpDefenseStage() + spDefenseStage);
            Log.i(TAG, "SpDefense Stage " + spDefenseStage + " SpDefense Stage =" + defendingPokemon.getSpDefenseStage());
        }

        if (speedStage >= 0) {
            attackingPokemon.setSpeedStage(attackingPokemon.getSpeedStage() + speedStage);
            Log.i(TAG, "Speed Stage +" + speedStage + " Speed Stage =" + attackingPokemon.getSpeedStage());
        } else {
            defendingPokemon.setSpeedStage(defendingPokemon.getSpeedStage() + speedStage);
            Log.i(TAG, "Speed Stage " + speedStage + " Speed Stage =" + defendingPokemon.getSpeedStage());
        }

        if (critStage >= 0) {
            attackingPokemon.setCritStage(attackingPokemon.getCritStage() + critStage);
            Log.i(TAG, "Crit Stage +" + critStage);
            Log.i(TAG, "Crit Stage =" + attackingPokemon.getCritStage());
        } else {
            defendingPokemon.setCritStage(defendingPokemon.getCritStage() + critStage);
            Log.i(TAG, "Crit Stage " + critStage);
            Log.i(TAG, "Crit Stage =" + defendingPokemon.getCritStage());
        }

        if (res.isHaze()) {
            Log.i(TAG, "Haze reset all Pokemon stat stages to 0");
            attackingPokemon.setAttackStage(attackingPokemon.getAttackStage() + (attackingPokemon.getAttackStage() * (-1)));
            attackingPokemon.setDefenseStage(attackingPokemon.getDefenseStage() + (attackingPokemon.getDefenseStage() * (-1)));
            attackingPokemon.setSpAttackStage(attackingPokemon.getSpAttackStage() + (attackingPokemon.getSpAttackStage() * (-1)));
            attackingPokemon.setSpDefenseStage(attackingPokemon.getSpDefenseStage() + (attackingPokemon.getSpDefenseStage() * (-1)));
            attackingPokemon.setSpeedStage(attackingPokemon.getSpeedStage() + (attackingPokemon.getSpeedStage() * (-1)));
            attackingPokemon.setCritStage(attackingPokemon.getCritStage() + (attackingPokemon.getCritStage() * (-1)));
            defendingPokemon.setAttackStage(defendingPokemon.getAttackStage() + (defendingPokemon.getAttackStage() * (-1)));
            defendingPokemon.setDefenseStage(defendingPokemon.getDefenseStage() + (defendingPokemon.getDefenseStage() * (-1)));
            defendingPokemon.setSpAttackStage(defendingPokemon.getSpAttackStage() + (defendingPokemon.getSpAttackStage() * (-1)));
            defendingPokemon.setSpDefenseStage(defendingPokemon.getSpDefenseStage() + (defendingPokemon.getSpDefenseStage() * (-1)));
            defendingPokemon.setSpeedStage(defendingPokemon.getSpeedStage() + (defendingPokemon.getSpeedStage() * (-1)));
            defendingPokemon.setCritStage(defendingPokemon.getCritStage() + (defendingPokemon.getCritStage() * (-1)));
        }

        decrementStatusEffectTurns(attackingPokemon);
        decrementStatusEffectTurns(defendingPokemon);
        applyStatusEffectDamage(attackingPokemon, res);
        applyStatusEffectDamage(defendingPokemon, res);
        applyFainting(attackingPokemon);
        applyFainting(defendingPokemon);

        if (attackingPokemon.isFainted()) {
            res.appendToLog(attackingOriginal.getName() + " fainted");
        }

        if (defendingPokemon.isFainted()) {
            res.appendToLog(defendingOriginal.getName() + " fainted");
        }
    }

    private void decrementStatusEffectTurns(BattlePokemon pokemon) {

        Log.i(TAG, "Decrementing status effect and confusion turns and resolving those states for: " + pokemon.getOriginalPokemon().getName());

        pokemon.setStatusEffectTurns(pokemon.getStatusEffectTurns() - 1);
        pokemon.setConfusedTurns(pokemon.getConfusedTurns() - 1);

        if (pokemon.getStatusEffectTurns() <= 0) {
            pokemon.setStatusEffect(null);
        }
        if (pokemon.getConfusedTurns() <= 0) {
            pokemon.setConfused(false);
        }
    }

    private void applyStatusEffectDamage(BattlePokemon pokemon, AttackResult result) {

        int burnDamage = result.getBurnDamageTaken();
        int poisonDamage = result.getPoisonDamageTaken();

        if (pokemon.getStatusEffect() == StatusEffect.BURN) {
            Log.i(TAG, "Pokemon " + pokemon.getOriginalPokemon().getName() + " took " + burnDamage + " burn damage");
            pokemon.setCurrentHp(pokemon.getCurrentHp() - burnDamage);
        }

        if (pokemon.getStatusEffect() == StatusEffect.POISON) {
            Log.i(TAG, "Pokemon " + pokemon.getOriginalPokemon().getName() + " took " + poisonDamage + " poison damage");

            pokemon.setCurrentHp(pokemon.getCurrentHp() - poisonDamage);
        }
    }

    private void applyFainting(BattlePokemon pokemon) {

        boolean attackerFainted = pokemon.getCurrentHp() <= 0;

        Log.i(TAG, "Applying fainted status. Pokemon fainted? " + attackerFainted);
        pokemon.setFainted(attackerFainted);
    }

    private void applySwitchResult(SwitchResult res) {

        TargetInfo targetInfo = res.getTargetInfo();

        BattlePokemonPlayer attackingPlayer = getPlayerFromId(targetInfo.getAttackingPlayer().getId());
        BattlePokemon attackingPokemon = attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        Log.i(TAG, "Attacking player: " + targetInfo.getAttackingPlayer().getId());
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());

        attackingPlayer.getBattlePokemonTeam().switchPokemonAtPosition(res.getPositionOfPokemon());
    }

    public BattlePokemonPlayer getPlayerFromId(String id) {

        if (self.getId().equals(id)) {
            return self;
        } else {
            return opponent;
        }

    }

    public boolean oppPokemonFainted() {
        return opponent.getBattlePokemonTeam().getCurrentPokemon().isFainted();
    }

    public boolean selfPokemonFainted() {
        return self.getBattlePokemonTeam().getCurrentPokemon().isFainted();
    }

    public String getBattleLog() {

        StringBuilder sb = new StringBuilder();

        for (BattlePhase battlePhase : finishedBattlePhases) {

            BattlePhaseResult battlePhaseResult = battlePhase.getBattlePhaseResult();

            for (CommandResult commandResult : battlePhaseResult.getCommandResults()) {
                sb.append(commandResult.getLog() + "\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
