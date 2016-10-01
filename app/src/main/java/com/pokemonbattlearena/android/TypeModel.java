package com.pokemonbattlearena.android;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class TypeModel {
    private final static String[] typeNames = {
            "bug",
            "dragon",
            "ice",
            "fighting",
            "fire",
            "flying",
            "grass",
            "ghost",
            "ground",
            "electric",
            "normal",
            "poison",
            "psychic",
            "rock",
            "water"
    };
    private final static int[] typeColorIds = {
            R.color.bug_type,
            R.color.dragon_type,
            R.color.ice_type,
            R.color.fighting_type,
            R.color.fire_type,
            R.color.flying_type,
            R.color.grass_type,
            R.color.ghost_type,
            R.color.ground_type,
            R.color.electric_type,
            R.color.normal_type,
            R.color.poison_type,
            R.color.psychic_type,
            R.color.rock_type,
            R.color.water_type
    };

    private Map<String, Integer> typeMap;

    public TypeModel() {
        typeMap = new HashMap<>(15);
        for (int i = 0; i < 15; i++) {
            typeMap.put(typeNames[i], typeColorIds[i]);
        }
    }

    public Map<String, Integer> getTypeMap() {
        return typeMap;
    }

    public int getColorForType(String type) {
        return typeMap.get(type.toLowerCase());
    }
}
