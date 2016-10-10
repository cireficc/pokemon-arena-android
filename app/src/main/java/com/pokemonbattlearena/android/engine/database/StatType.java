package com.pokemonbattlearena.android.engine.database;

public enum StatType {
    HP("Hp"),
    ATTACK("Attack"),
    DEFENSE("Defense"),
    SPECIALATTACK("SpecialAttack"),
    SPECIALDEFENSE("SpecialDefense"),
    SPEED("Speed"),
    CRIT("Crit");

    private String type;

    StatType(String type) {
        this.type = type;
    }
}
