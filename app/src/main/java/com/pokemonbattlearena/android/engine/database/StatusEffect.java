package com.pokemonbattlearena.android.engine.database;

public enum StatusEffect {
    BURN("Burn"),
    CONFUSION("Confusion"),
    FREEZE("Freeze"),
    PARALYZE("Paralyze"),
    POISON("Poison"),
    SLEEP("Sleep");

    private String effect;

    StatusEffect(String effect) {
        this.effect = effect;
    }
}
