package com.pokemonbattlearena.android;

import java.util.ArrayList;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

/**
 * Created by mitchcout on 10/12/2016.
 */

public class GridItem {

    private int pId;
    private String pName;
    private String pType1;
    private String pType2;

    public GridItem(Pokemon pokemon) {
        pId = pokemon.getId();
        pName = pokemon.getName();
        pType1 = pokemon.getType1();
        pType2 = pokemon.getType2();

        addItemToContainer();
    }

    public static void createGridItem(Pokemon pokemon, LinearLayout container) {


    }

    private static void addItemToContainer() {

    }
}
