package com.pokemonbattlearena.android.util;

/**
 * Created by Spencer Amann on 10/27/16.
 */

public enum CustomTypeMatch {
    NONE("none", 0b0),
    BUG("bug",          0b00000000000000010),
    DRAGON("dragon",    0b00000000000000100),
    ICE("ice",          0b00000000000001000),
    FIGHTING("fighting",0b00000000000010000),
    FIRE("fire",        0b00000000000100000),
    FLYING("flying",    0b00000000001000000),
    GRASS("grass",      0b00000000010000000),
    GHOST("ghost",      0b00000000100000000),
    GROUND("ground",    0b00000001000000000),
    ELECTRIC("electric",0b00000010000000000),
    NORMAL("normal",    0b00000100000000000),
    POISON("poison",    0b00001000000000000),
    PSYCHIC("psychic",  0b00010000000000000),
    ROCK("rock",        0b00100000000000000),
    WATER("water",      0b01000000000000000);

    private String type;
    private int flag;

    CustomTypeMatch(String type, int flag) {
        this.type = type;
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}


