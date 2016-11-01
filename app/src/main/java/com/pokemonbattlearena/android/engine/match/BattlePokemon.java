package com.pokemonbattlearena.android.engine.match;

import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;

import java.util.ArrayList;
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
    private transient List<Move> moveSet;

    public BattlePokemon(Pokemon pokemon) {
        this.originalPokemon = pokemon;
        this.currentHp = pokemon.getHp();
        this.confused = false;
        this.flinched = false;
        this.fainted = false;
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

    public List<Move> getMoveSet() { return moveSet; }
}
