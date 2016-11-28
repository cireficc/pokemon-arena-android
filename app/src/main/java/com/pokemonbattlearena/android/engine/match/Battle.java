package com.pokemonbattlearena.android.engine.match;

import android.util.Log;

import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.pokemonbattlearena.android.engine.Logging.logApplyAttackResult;
import static com.pokemonbattlearena.android.engine.Logging.logApplySwitchResult;
import static com.pokemonbattlearena.android.engine.Logging.logComparator;
import static com.pokemonbattlearena.android.engine.Logging.logExecuteCommand;
import static com.pokemonbattlearena.android.engine.Logging.logExecuteCurrentBattlePhase;
import static com.pokemonbattlearena.android.engine.Logging.logHealing;
import static com.pokemonbattlearena.android.engine.Logging.logStages;
import static com.pokemonbattlearena.android.engine.Logging.logStartNewBattlePhase;
import static com.pokemonbattlearena.android.engine.Logging.logStatusEffects;

public class Battle {

    //Logging Flags
    public static boolean logStartNewBattlePhase = false;
    public static boolean logComparator = false;
    public static boolean logExecuteCommand = false;
    public static boolean logExecuteCurrentBattlePhase = false;
    public static boolean logApplyAttackResult = false;
    public static boolean logStatusEffects = false;
    public static boolean logHealing = false;
    public static boolean logStages = false;
    public static boolean logApplySwitchResult = false;

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

        finishedBattlePhases.add(currentBattlePhase);
        currentBattlePhase = new BattlePhase(self, opponent);

        if (logStartNewBattlePhase) {
            logStartNewBattlePhase();
        }
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
            if (c1 instanceof Switch || c2 instanceof Switch) {
                return Integer.MIN_VALUE;
            }

            Attack a1 = (Attack) c1;
            Attack a2 = (Attack) c2;
            int pokemon1Speed = a1.getAttackingPokemon(Battle.this).getOriginalPokemon().getSpeed();
            int pokemon2Speed = a2.getAttackingPokemon(Battle.this).getOriginalPokemon().getSpeed();

            if (logComparator) {
                logComparator(pokemon1Speed, pokemon2Speed);
            }

            return pokemon2Speed - pokemon1Speed;
        }

    };
    public BattlePhaseResult executeCurrentBattlePhase() {

        Collections.sort(currentBattlePhase.getCommands(), commandComparator);
        BattlePhaseResult battlePhaseResult = new BattlePhaseResult();

        for (Command command : currentBattlePhase.getCommands()) {
            if (logExecuteCommand) {
                logExecuteCommand(command);
            }
            CommandResult commandResult = command.execute(this);
            if (commandResult instanceof AttackResult && (selfPokemonFainted() || oppPokemonFainted())) {

            } else {
                    applyCommandResult(commandResult);
                    battlePhaseResult.addCommandResult(commandResult);
            }
            setFinished();
        }
        currentBattlePhase.setBattlePhaseResult(battlePhaseResult);

        if(logExecuteCurrentBattlePhase) {
            logExecuteCurrentBattlePhase(isFinished);
        }
        return battlePhaseResult;
    }

    public void applyCommandResult(CommandResult commandResult) {

        if (commandResult instanceof AttackResult) {
            applyAttackResult((AttackResult) commandResult);
        } else if (commandResult instanceof SwitchResult) {
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

        defendingPokemon.setCurrentHp(defendingPokemon.getCurrentHp() - damageDone);


        // If the Pokemon doesn't already have a StatusEffect, we can apply one
        if (defendingPokemon.getStatusEffect() == null && statusEffectApplied != null) {
            defendingPokemon.setStatusEffect(statusEffectApplied);
            defendingPokemon.setStatusEffectTurns(res.getStatusEffectTurns());
        }

        if (!defendingPokemon.isConfused() && confused) {
            defendingPokemon.setConfused(confused);
            defendingPokemon.setConfusedTurns(confusedTurns);
        }

        defendingPokemon.setFlinched(flinched);

        if (!defendingPokemon.isCharging()) {}
        if (!defendingPokemon.isRecharging()) {}

        if(logStatusEffects) {
            logStatusEffects(defendingPokemon, statusEffectApplied, confused, flinched, statusEffectTurns, confusedTurns, chargingTurns, rechargingTurns);
        }

        int maxHp = attackingPokemon.getOriginalPokemon().getHp();
        int currentHp = attackingPokemon.getCurrentHp();
        int healedTo = currentHp + healingDone;

        if (healedTo >= maxHp) {
            attackingPokemon.setCurrentHp(maxHp);
        } else {
            attackingPokemon.setCurrentHp(healedTo);
        }
        attackingPokemon.setCurrentHp(currentHp - recoilTaken);

        if (logHealing) {
            logHealing(healingDone, maxHp, healedTo);
        }

        int attackStage = res.getAttackStageChange();
        int defenseStage = res.getDefenseStageChange();
        int spAttackStage = res.getSpAttackStageChange();
        int spDefenseStage = res.getSpDefenseStageChange();
        int speedStage = res.getSpeedStageChange();
        int critStage = res.getCritStageChange();

        if (attackStage >= 0) {
            attackingPokemon.setAttackStage(attackingPokemon.getAttackStage() + attackStage);
        } else {
            defendingPokemon.setAttackStage(defendingPokemon.getAttackStage() + attackStage);
        }

        if (defenseStage >= 0) {
            attackingPokemon.setDefenseStage(attackingPokemon.getDefenseStage() + defenseStage);
        } else {
            defendingPokemon.setDefenseStage(defendingPokemon.getDefenseStage() + defenseStage);
        }

        if (spAttackStage >= 0) {
            attackingPokemon.setSpAttackStage(attackingPokemon.getSpAttackStage() + spAttackStage);
        } else {
            defendingPokemon.setSpAttackStage(defendingPokemon.getSpAttackStage() + spAttackStage);
        }

        if (spDefenseStage >= 0) {
            attackingPokemon.setSpDefenseStage(attackingPokemon.getSpDefenseStage() + spDefenseStage);
        } else {
            defendingPokemon.setSpDefenseStage(defendingPokemon.getSpDefenseStage() + spDefenseStage);
        }

        if (speedStage >= 0) {
            attackingPokemon.setSpeedStage(attackingPokemon.getSpeedStage() + speedStage);
        } else {
            defendingPokemon.setSpeedStage(defendingPokemon.getSpeedStage() + speedStage);
        }

        if (critStage >= 0) {
            attackingPokemon.setCritStage(attackingPokemon.getCritStage() + critStage);
        } else {
            defendingPokemon.setCritStage(defendingPokemon.getCritStage() + critStage);
        }

        if(logStages) {
            logStages(attackStage, attackingPokemon, defendingPokemon, defenseStage, spAttackStage, spDefenseStage, speedStage, critStage);
        }

        if (res.isHaze()) {
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

        boolean attackerFainted = attackingPokemon.getCurrentHp() <= 0;
        boolean defenderFainted = defendingPokemon.getCurrentHp() <= 0;

        attackingPokemon.setFainted(attackerFainted);
        defendingPokemon.setFainted(defenderFainted);

        if(logApplyAttackResult) {
            logApplyAttackResult(attackingPlayerId, attackingPokemon, defendingPlayer, defendingPlayerId, defendingPokemon, damageDone, recoilTaken, res, attackerFainted, defenderFainted);
        }
    }

    private void applySwitchResult(SwitchResult res) {

        TargetInfo targetInfo = res.getTargetInfo();

        BattlePokemonPlayer attackingPlayer = getPlayerFromId(targetInfo.getAttackingPlayer().getId());
        BattlePokemon attackingPokemon = attackingPlayer.getBattlePokemonTeam().getCurrentPokemon();

        attackingPlayer.getBattlePokemonTeam().switchPokemonAtPosition(res.getPositionOfPokemon());

        if(logApplySwitchResult) {
            logApplySwitchResult(targetInfo, attackingPokemon);
        }
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
}
