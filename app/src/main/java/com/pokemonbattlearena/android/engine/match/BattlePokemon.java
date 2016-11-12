package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatType;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

import java.util.List;

public class BattlePokemon {

    private transient Pokemon originalPokemon;
    private transient int currentHp;
    private transient StatusEffect statusEffect;
    private transient int statusEffectTurns;
    private transient boolean confused;
    private transient int confusedTurns;
    private transient boolean flinched;
    private transient int chargingForTurns;
    private transient int rechargingForTurns;
    private transient boolean fainted;
    private transient int attackStage;
    private transient int defenseStage;
    private transient int spAttackStage;
    private transient int spDefenseStage;
    private transient int speedStage;
    private transient int critStage;
    private transient List<Move> moveSet;

    public BattlePokemon(Pokemon pokemon) {
        this.originalPokemon = pokemon;
        this.currentHp = pokemon.getHp();
        this.confused = false;
        this.flinched = false;
        this.fainted = false;
        this.attackStage = 0;
        this.defenseStage = 0;
        this.spAttackStage = 0;
        this.spDefenseStage = 0;
        this.speedStage = 0;
        this.critStage = 0;
        this.moveSet = pokemon.getActiveMoveList();
    }

    public Pokemon getOriginalPokemon() {
        return originalPokemon;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public void setStatusEffect(StatusEffect statusEffect) {
        this.statusEffect = statusEffect;
    }

    public boolean hasStatusEffect() {
        return statusEffect != null;
    }

    public int getStatusEffectTurns() {
        return statusEffectTurns;
    }

    public void setStatusEffectTurns(int statusEffectTurns) {
        this.statusEffectTurns = statusEffectTurns;
    }

    public boolean isConfused() {
        return confused;
    }

    public void setConfused(boolean confused) {
        this.confused = confused;
    }

    public int getConfusedTurns() {
        return confusedTurns;
    }

    public void setConfusedTurns(int confusedTurns) {
        this.confusedTurns = confusedTurns;
    }

    public boolean isFlinched() {
        return flinched;
    }

    public void setFlinched(boolean flinched) {
        this.flinched = flinched;
    }

    public int getChargingForTurns() {
        return chargingForTurns;
    }

    public void setChargingForTurns(int chargingForTurns) {
        this.chargingForTurns = chargingForTurns;
    }

    public boolean isCharging() {
        return this.chargingForTurns > 0;
    }

    public int getRechargingForTurns() {
        return rechargingForTurns;
    }

    public void setRechargingForTurns(int rechargingForTurns) {
        this.rechargingForTurns = rechargingForTurns;
    }

    public boolean isRecharging() {
        return this.rechargingForTurns > 0;
    }

    public boolean isFainted() { return this.fainted; }

    public void setFainted(boolean fainted) { this.fainted = fainted; }

    public void setMoveSet(List<Move> moves) {
        this.moveSet = moves;
    }

    public List<Move> getMoveSet() { return moveSet; }

    public int getAttackStage() {
        return attackStage;
    }

    public void setAttackStage(int attackStage) {
        this.attackStage = attackStage;
    }

    public int getDefenseStage() {
        return defenseStage;
    }

    public void setDefenseStage(int defenseStage) {
        this.defenseStage = defenseStage;
    }

    public int getSpAttackStage() {
        return spAttackStage;
    }

    public void setSpAttackStage(int spAttackStage) {
        this.spAttackStage = spAttackStage;
    }

    public int getSpDefenseStage() {
        return spDefenseStage;
    }

    public void setSpDefenseStage(int spDefenseStage) {
        this.spDefenseStage = spDefenseStage;
    }

    public int getSpeedStage() {
        return speedStage;
    }

    public void setSpeedStage(int speedStage) {
        this.speedStage = speedStage;
    }

    public int getCritStage() {
        return critStage;
    }

    public void setCritStage(int critStage) {
        this.critStage = critStage;
    }
}
