package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatType;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

import java.util.List;

public class BattlePokemon {

    private transient final int MIN_STAGE = -6;
    private transient final int MAX_STAGE = 6;
    private transient Pokemon originalPokemon;
    private transient int currentHp;
    private transient StatusEffect statusEffect;
    private transient int statusEffectTurns;
    private transient boolean confused = false;
    private transient int confusedTurns;
    private transient boolean flinched = false;
    private transient int chargingForTurns;
    private transient int rechargingForTurns;
    private transient boolean fainted = false;
    private transient int attackStage = 0;
    private transient int defenseStage = 0;
    private transient int spAttackStage = 0;
    private transient int spDefenseStage = 0;
    private transient int speedStage = 0;
    private transient int critStage = 0;
    private transient List<Move> moveSet;
    private transient boolean isCurrentPokemon = false;
    private transient boolean isPokemonOnDeck = false;

    public BattlePokemon(Pokemon pokemon) {
        this.originalPokemon = pokemon;
        this.currentHp = pokemon.getHp();
        this.moveSet = pokemon.getActiveMoveList();
    }

    public boolean isCurrentPokemon() { return isCurrentPokemon; }

    public void setAsCurrentPokemon(boolean currentPokemon) { isCurrentPokemon = currentPokemon; }

    public boolean isPokemonOnDeck() { return isPokemonOnDeck; }

    public void setAsPokemonOnDeck(boolean pokemonOnDeck) { isPokemonOnDeck = pokemonOnDeck; }

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
        this.attackStage = Math.min(Math.max(this.attackStage + attackStage, MIN_STAGE), MAX_STAGE);
    }

    public int getDefenseStage() {
        return defenseStage;
    }

    public void setDefenseStage(int defenseStage) {
        this.defenseStage = Math.min(Math.max(this.defenseStage + defenseStage, MIN_STAGE), MAX_STAGE);
    }

    public int getSpAttackStage() {
        return spAttackStage;
    }

    public void setSpAttackStage(int spAttackStage) {
        this.spAttackStage = Math.min(Math.max(this.spAttackStage + spAttackStage, MIN_STAGE), MAX_STAGE);
    }

    public int getSpDefenseStage() {
        return spDefenseStage;
    }

    public void setSpDefenseStage(int spDefenseStage) {
        this.spDefenseStage = Math.min(Math.max(this.spDefenseStage + spDefenseStage, MIN_STAGE), MAX_STAGE);
    }

    public int getSpeedStage() {
        return speedStage;
    }

    public void setSpeedStage(int speedStage) {
        this.speedStage = Math.min(Math.max(this.speedStage + speedStage, MIN_STAGE), MAX_STAGE);
    }

    public int getCritStage() {
        return critStage;
    }

    public void setCritStage(int critStage) {
        this.critStage = Math.min(Math.max(this.critStage + critStage, MIN_STAGE), MAX_STAGE);
    }
}
