package com.pokemonbattlearena.android.engine.ai;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

import java.util.BitSet;
import java.util.List;

/**
 * Created by nathan on 11/26/16.
 */

public class StatePokemon {

    private static final int CONFUSED = 1;
    private static final int FLINCHED = 2;
    private static final int FAINTED = 4;

    private final int currentHp;
    private final StatusEffect statusEffect;
    private final byte statusEffectTurns;
    private final byte confusedTurns;
    private final byte chargingForTurns;
    private final byte rechargingForTurns;
    private final short attackStage;
    private final short defenseStage;
    private final short spAttackStage;
    private final short spDefenseStage;
    private final short speedStage;
    private final short critStage;
    private byte finiteStatus = 0;
    private final Pokemon originalPokemon;

    private final boolean isCurrentPokemon;
    private final boolean isPokemonOnDeck;

    public StatePokemon(BattlePokemon bp) {
        this(
                bp.getCurrentHp(),
                bp.getStatusEffect(),
                (byte)bp.getStatusEffectTurns(),
                bp.isConfused(),
                (byte)bp.getConfusedTurns(),
                bp.isFlinched(),
                (byte)bp.getChargingForTurns(),
                (byte)bp.getRechargingForTurns(),
                bp.isFainted(),
                (short)bp.getAttackStage(),
                (short)bp.getDefenseStage(),
                (short)bp.getSpAttackStage(),
                (short)bp.getSpDefenseStage(),
                (short)bp.getSpeedStage(),
                (short)bp.getCritStage(),
                bp.getOriginalPokemon(),
                bp.isCurrentPokemon(),
                bp.isPokemonOnDeck()
        );
    }

    public StatePokemon(int currentHp, StatusEffect statusEffect, byte statusEffectTurns, boolean confused, byte confusedTurns, boolean flinched, byte chargingForTurns, byte rechargingForTurns, boolean fainted, short attackStage, short defenseStage, short spAttackStage, short spDefenseStage, short speedStage, short critStage, Pokemon originalPokemon, boolean isCurrentPokemon, boolean isPokemonOnDeck) {
        this.currentHp = currentHp;
        this.statusEffect = statusEffect;
        this.statusEffectTurns = statusEffectTurns;
        this.confusedTurns = confusedTurns;
        this.chargingForTurns = chargingForTurns;
        this.rechargingForTurns = rechargingForTurns;
        this.attackStage = attackStage;
        this.defenseStage = defenseStage;
        this.spAttackStage = spAttackStage;
        this.spDefenseStage = spDefenseStage;
        this.speedStage = speedStage;
        this.critStage = critStage;
        this.originalPokemon = originalPokemon;
        this.isCurrentPokemon = isCurrentPokemon;
        this.isPokemonOnDeck = isPokemonOnDeck;

        if(confused) {
            this.finiteStatus |=  CONFUSED;
        }
        if(flinched) {
            this.finiteStatus |= FLINCHED;
        }
        if (fainted) {
            this.finiteStatus |= FAINTED;
        }
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public byte getStatusEffectTurns() {
        return statusEffectTurns;
    }

    public boolean isConfused() {
        return (finiteStatus & CONFUSED) == CONFUSED;
    }

    public byte getConfusedTurns() {
        return confusedTurns;
    }

    public boolean isFlinched() {
        return (finiteStatus & FLINCHED) == FLINCHED;
    }

    public byte getChargingForTurns() {
        return chargingForTurns;
    }

    public byte getRechargingForTurns() {
        return rechargingForTurns;
    }

    public boolean isFainted() {
        return (finiteStatus & FAINTED) == FAINTED;
    }

    public short getAttackStage() {
        return attackStage;
    }

    public short getDefenseStage() {
        return defenseStage;
    }

    public short getSpAttackStage() {
        return spAttackStage;
    }

    public short getSpDefenseStage() {
        return spDefenseStage;
    }

    public short getSpeedStage() {
        return speedStage;
    }

    public short getCritStage() {
        return critStage;
    }

    public boolean isCurrentPokemon() { return isCurrentPokemon; }

    public boolean isPokemonOnDeck() { return  isPokemonOnDeck; }

    public BattlePokemon toBattle() {
        BattlePokemon toBattle = new BattlePokemon(originalPokemon);
        toBattle.setCurrentHp(getCurrentHp());
        toBattle.setStatusEffect(getStatusEffect());
        toBattle.setStatusEffectTurns(getStatusEffectTurns());
        toBattle.setConfusedTurns(getConfusedTurns());
        toBattle.setChargingForTurns(getChargingForTurns());
        toBattle.setRechargingForTurns(getRechargingForTurns());
        toBattle.setAttackStage(getAttackStage());
        toBattle.setDefenseStage(getDefenseStage());
        toBattle.setSpAttackStage(getSpAttackStage());
        toBattle.setSpDefenseStage(getSpDefenseStage());
        toBattle.setSpeedStage(getSpeedStage());
        toBattle.setCritStage(getCritStage());
        toBattle.setAsCurrentPokemon(isCurrentPokemon());
        toBattle.setAsPokemonOnDeck(isPokemonOnDeck());
        toBattle.setConfused(isConfused());
        toBattle.setFlinched(isFlinched());
        toBattle.setFainted(isFainted());
        return toBattle;
    }
}


