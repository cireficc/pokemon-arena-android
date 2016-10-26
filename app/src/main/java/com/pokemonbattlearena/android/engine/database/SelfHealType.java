package com.pokemonbattlearena.android.engine.database;

public enum SelfHealType {
    DIRECT("Direct"),
    ABSORB("Absorb");

    private String type;

    SelfHealType(String type) {
        this.type = type;
    }
}
