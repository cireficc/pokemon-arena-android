package com.pokemonbattlearena.android.engine.database;

public enum MoveType {
    PHYSICAL("Physical"),
    SPECIAL("Special"),
    STATUS("Status");

    private String type;

    MoveType(String type) {
        this.type = type;
    }
}
