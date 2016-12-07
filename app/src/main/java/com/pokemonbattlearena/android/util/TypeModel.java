package com.pokemonbattlearena.android.util;

import com.pokemonbattlearena.android.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class TypeModel {
    public final static String[] typeNames = {
            "bug",
            "dragon",
            "electric",
            "fighting",
            "fire",
            "flying",
            "grass",
            "ghost",
            "ground",
            "ice",
            "normal",
            "poison",
            "psychic",
            "rock",
            "water"
    };

    private final static int[] typeColorIds = {
            R.color.bug_type,
            R.color.dragon_type,
            R.color.electric_type,
            R.color.fighting_type,
            R.color.fire_type,
            R.color.flying_type,
            R.color.grass_type,
            R.color.ghost_type,
            R.color.ground_type,
            R.color.ice_type,
            R.color.normal_type,
            R.color.poison_type,
            R.color.psychic_type,
            R.color.rock_type,
            R.color.water_type
    };

    public final static int[] typeImageIds = {
            R.drawable.ic_type_bug,
            R.drawable.ic_type_dragon,
            R.drawable.ic_type_electric,
            R.drawable.ic_type_fighting,
            R.drawable.ic_type_fire,
            R.drawable.ic_type_flying,
            R.drawable.ic_type_grass,
            R.drawable.ic_type_ghost,
            R.drawable.ic_type_ground,
            R.drawable.ic_type_ice,
            R.drawable.ic_type_normal,
            R.drawable.ic_type_poison,
            R.drawable.ic_type_psychic,
            R.drawable.ic_type_rock,
            R.drawable.ic_type_water
    };

    private Map<String, Integer> typeColorMap;

    private static Map<String, Integer> typeImageMap;

    public TypeModel() {
        typeColorMap = new HashMap<>(15);
        typeImageMap = new HashMap<>(15);
        for (int i = 0; i < 15; i++) {
            typeColorMap.put(typeNames[i], typeColorIds[i]);
            typeImageMap.put(typeNames[i], typeImageIds[i]);
        }
    }

    public static int getDrawableForType(String type) {
        return typeImageMap.get(type);
    }

    public int getColorForType(String type) {
        return typeColorMap.get(type.toLowerCase());
    }
}
