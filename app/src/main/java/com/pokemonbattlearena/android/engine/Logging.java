package com.pokemonbattlearena.android.engine;

import android.util.Log;

import com.pokemonbattlearena.android.engine.database.RecoilAmount;
import com.pokemonbattlearena.android.engine.database.SelfHealAmount;
import com.pokemonbattlearena.android.engine.database.SelfHealType;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.TargetInfo;
import com.pokemonbattlearena.android.engine.match.calculators.DamageCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.HealingCalculator;
import com.pokemonbattlearena.android.engine.match.calculators.RecoilCalculator;

import static android.content.ContentValues.TAG;

/**
 * Created by nathan on 11/27/16.
 */

public class Logging {

    //BATTLE CLASS LOGS
    public static void logStartNewBattlePhase() {
        Log.e(TAG, "Starting new battle phase");
        Log.i(TAG, "Added current battle phase to finished phases");
        Log.i(TAG, "Created new battle phase");
    }

    public static void logComparator(int pokemon1Speed, int pokemon2Speed) {
        Log.i(TAG, "There was a Switch command - prioritizing it");
        Log.i(TAG, "Pokemon 1 speed: " + pokemon1Speed + " || Pokemon 2 speed: " + pokemon2Speed);
        Log.i(TAG, "Sorting commands by priority");
    }

    public static void logExecuteCommand(Command command) {
        Log.i(TAG, "Executing command from current BattlePhase of type: " + command.getClass());
        Log.i(TAG, "Adding command result to battle phase result");
    }
    public static void logExecuteCurrentBattlePhase(boolean isFinished) {
         Log.i(TAG, "Setting battle phase result on current battle phase");
         Log.i(TAG, "Setting finished");
         Log.i(TAG, "Battle finished? " + isFinished);
    }

    public static void logApplyAttackResult(String attackingPlayerId,
                                            BattlePokemon attackingPokemon,
                                            BattlePokemonPlayer defendingPlayer,
                                            String defendingPlayerId,
                                            BattlePokemon defendingPokemon,
                                            int damageDone,
                                            int recoilTaken,
                                            AttackResult res,
                                            boolean attackerFainted,
                                            boolean defenderFainted) {
        Log.i(TAG, "Applying command result of type AttackResult");
        Log.i(TAG, "Attacking player: " + attackingPlayerId);
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());
        Log.i(TAG, "Defending player ID: " + defendingPlayer.getId() + "Defending ID: " + defendingPlayerId);
        Log.i(TAG, "Defending player pkmn: " + defendingPokemon.getOriginalPokemon().getName());
        Log.i(TAG, "Applying damage done: " + damageDone);
        Log.i(TAG, "Applying recoil taken: " + recoilTaken);
        if (res.isHaze()) {
            Log.i(TAG, "Haze reset all Pokemon stat stages to 0");
        }
        Log.i(TAG, "Applying fainted status. Attacker fainted? " + attackerFainted + " || defender fainted? " + defenderFainted);
    }

    public static void logStatusEffects(BattlePokemon defendingPokemon,
                                        StatusEffect statusEffectApplied,
                                        boolean confused,
                                        boolean flinched,
                                        int statusEffectTurns,
                                        int confusedTurns,
                                        int chargingTurns,
                                        int rechargingTurns) {
        if (defendingPokemon.getStatusEffect() == null && statusEffectApplied != null) {
            Log.i(TAG, "Pokemon doesn't already have a StatusEffect. Applying for " + statusEffectTurns + " turn(s)!");
        }
        if (!defendingPokemon.isConfused() && confused) {
            Log.i(TAG, "Pokemon is not already confused. Applying for " + confusedTurns + " turn(s)!");
        }
        if (!defendingPokemon.isCharging()) {
            Log.i(TAG, "Pokemon is not already charging. Applying for " + chargingTurns + " turn(s)!");
        }
        if (!defendingPokemon.isRecharging()) {
            Log.i(TAG, "Pokemon is not already recharging. Applying for " + rechargingTurns + " turn(s)!");
        }

        Log.i(TAG, "Applying flinch: " + flinched);

        }

    public static void logHealing(int healingDone,
                                  int maxHp,
                                  int healedTo) {
        Log.i(TAG, "Applying healing done: " + healingDone);
        if (healedTo >= maxHp) {
            Log.i(TAG, "Healing was over max HP; healing to max HP: " + maxHp);
        } else {
            Log.i(TAG, "Healing was not over max HP; healing to " + healedTo);
        }
    }

    public static void logStages(int attackStage,
                                 BattlePokemon attackingPokemon,
                                 BattlePokemon defendingPokemon,
                                 int defenseStage,
                                 int spAttackStage,
                                 int spDefenseStage,
                                 int speedStage,
                                 int critStage) {
        if (attackStage >= 0) {
            Log.i(TAG, "Attack Stage +" + attackStage);
            Log.i(TAG, "Attack Stage =" + attackingPokemon.getAttackStage());
        } else {
            Log.i(TAG, "Attack Stage " + attackStage);
            Log.i(TAG, "Attack Stage =" + defendingPokemon.getAttackStage());
        }

        if (defenseStage >= 0) {
            Log.i(TAG, "Defense Stage +" + defenseStage + " Defense Stage =" + attackingPokemon.getDefenseStage());
        } else {
            Log.i(TAG, "Defense Stage " + defenseStage + " Defense Stage =" + defendingPokemon.getDefenseStage());
        }

        if (spAttackStage >= 0) {
            Log.i(TAG, "SpAttack Stage +" + spAttackStage + " SpAttack Stage =" + attackingPokemon.getSpAttackStage());
        } else {
            Log.i(TAG, "SpAttack Stage " + spAttackStage + " SpAttack Stage =" + defendingPokemon.getSpAttackStage());
        }

        if (spDefenseStage >= 0) {
            Log.i(TAG, "SpDefense Stage +" + spDefenseStage + " SpDefense Stage =" + attackingPokemon.getSpDefenseStage());
        } else {
            Log.i(TAG, "SpDefense Stage " + spDefenseStage + " SpDefense Stage =" + defendingPokemon.getSpDefenseStage());
        }

        if (speedStage >= 0) {
            Log.i(TAG, "Speed Stage +" + speedStage + " Speed Stage =" + attackingPokemon.getSpeedStage());
        } else {
            Log.i(TAG, "Speed Stage " + speedStage + " Speed Stage =" + defendingPokemon.getSpeedStage());
        }

        if (critStage >= 0) {
            Log.i(TAG, "Crit Stage +" + critStage);
            Log.i(TAG, "Crit Stage =" + attackingPokemon.getCritStage());
        } else {
            Log.i(TAG, "Crit Stage " + critStage);
            Log.i(TAG, "Crit Stage =" + defendingPokemon.getCritStage());
        }
    }

    public static void logApplySwitchResult(TargetInfo targetInfo, BattlePokemon attackingPokemon) {
        Log.i(TAG, "Applying command result of type SwitchResult");
        Log.i(TAG, "Attacking player: " + targetInfo.getAttackingPlayer().getId());
        Log.i(TAG, "Attacking player pkmn: " + attackingPokemon.getOriginalPokemon().getName());
    }

    //DAMAGE CALCULATOR LOGS
    public static void logGetTimesHit(int hits, int minHits, int maxHits) {
        String TAG = DamageCalculator.class.getName();
        Log.i(TAG, "Move hits " + hits + " times (min: " + minHits + "; max: " + maxHits + ")");
    }


    public static void logCalculateDamage(String name,
                                          String target,
                                          double attack,
                                          double defense,
                                          double levelCalc,
                                          double statCalc,
                                          double basePower,
                                          double damage,
                                          double stabBonus,
                                          double type1Effectiveness,
                                          double type2Effectiveness,
                                          double typeEffectiveness,
                                          double critMultiplier,
                                          double roll,
                                          double modifier,
                                          double totalDamage,
                                          int totalDamageRounded) {

        String TAG = DamageCalculator.class.getName();
        Log.d(TAG, "\n\nCalculating damage for " + name + " against " + target);
        Log.d(TAG, "Attack: " + attack);
        Log.d(TAG, "Defense: " + defense);
        Log.d(TAG, "Level Calc: " + levelCalc);
        Log.d(TAG, "Stat Calc: " + statCalc);
        Log.d(TAG, "Base Power: " + basePower);
        Log.d(TAG, "Damage (before modifier): " + damage);
        Log.d(TAG, "STAB: " + stabBonus);
        Log.d(TAG, "Type1 Effectiveness: " + type1Effectiveness);
        Log.d(TAG, "Type2 Effectiveness: " + type2Effectiveness);
        Log.d(TAG, "Overall Type Effectiveness: " + typeEffectiveness);
        Log.d(TAG, "Crit Multiplier: " + critMultiplier);
        Log.d(TAG, "Random % Modifier: " + roll);
        Log.d(TAG, "Modifier Calc:: " + modifier);
        Log.d(TAG, "Total Damage: " + totalDamage);
        Log.d(TAG, "Total Damage (rounded): " + totalDamageRounded);
    }


    //HEALING CALCULATOR LOGS
    public static void logGetHealAmount(SelfHealType healType, int healed, String moveName) {
        String TAG = HealingCalculator.class.getName();
        if (healType == SelfHealType.DIRECT) {
            Log.i(TAG, moveName + " is a direct heal. Healing for " + healed + " HP");
        }

        if (healType == SelfHealType.ABSORB) {
            Log.i(TAG, moveName + " is an absorb heal. Healing for " + healed + " HP");
        }
    }


    public static void logDirectHealAmount(SelfHealAmount healAmount) {
        String TAG = HealingCalculator.class.getName();
        if (healAmount == SelfHealAmount.HALF) {
            Log.i(TAG, "Direct heal is 50% of user's max HP");
        }

    }

    public static void logAbsorbHealAmount(SelfHealAmount healAmount) {
        String TAG = HealingCalculator.class.getName();
        if (healAmount == SelfHealAmount.HALF) {
            Log.i(TAG, "Absorb heal is 50% of damage done");
        }
    }


    //RECOIL CALCULATOR LOGS
    public static void logGetRecoilAmount(RecoilAmount recoilAmount, String moveName, int recoiled) {
        String TAG = RecoilCalculator.class.getName();

        if (recoilAmount == RecoilAmount.ONEFOURTH) {}
        else if (recoilAmount == RecoilAmount.ONETHIRD) {}
        else {
            Log.i(TAG, moveName + " is a crash move. Attacker takes " + recoiled + " damage");
        }
        Log.i(TAG, moveName + " is a recoil move. Attacker takes " + recoiled + " damage");

    }


}
