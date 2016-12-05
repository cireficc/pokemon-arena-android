package com.pokemonbattlearena.android.fragment.team;

import android.support.v7.widget.CardView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.engine.database.Pokemon;

public class PokemonGridViewItem {

    public CardView card;
    public TextView mId;
    public TextView mName;
    public ImageView mImage;
    public ImageView mType1;
    public ImageView mType2;
    public CheckBox mCheckbox;
    public Pokemon pokemon;
}
