package com.pokemonbattlearena.android.engine.database;

public enum ElementalType {
    NORMAL("Normal"),
    FIRE("Fire"),
    WATER("Water"),
    ELECTRIC("Electric"),
    GRASS("Grass"),
    ICE("Ice"),
    FIGHTING("Fighting"),
    POISON("Poison"),
    GROUND("Ground"),
    FLYING("Flying"),
    PSYCHIC("Psychic"),
    BUG("Bug"),
    ROCK("Rock"),
    GHOST("Ghost"),
    DRAGON("Dragon");

    private String type;

    ElementalType(String type) {
        this.type = type;
    }
}
