package com.pokemonbattlearena.android.fragments.battle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.engine.database.Pokemon;

import org.w3c.dom.Text;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    ImageView pokemonHPImage;
    Pokemon pokemon;

    public BattleViewItem(ImageView pokemonImage, TextView pokemonName, Pokemon pokemon) {
        this.pokemonImage = pokemonImage;
        this.pokemonName = pokemonName;
        this.pokemon = pokemon;
    }

    public BattleViewItem(ImageView pokemonImage, TextView pokemonName, TextView pokemonHPText, ImageView pokemonHPImage, Pokemon pokemon) {
        this.pokemonImage = pokemonImage;
        this.pokemonName = pokemonName;
        this.pokemonHPText = pokemonHPText;
        this.pokemonHPImage = pokemonHPImage;
        this.pokemon = pokemon;
    }

    public ImageView getPokemonImage() {
        return pokemonImage;
    }

    public TextView getPokemonName() {
        return pokemonName;
    }

    public TextView getPokemonHPText() {
        return pokemonHPText;
    }

    public ImageView getPokemonHPImage() {
        return pokemonHPImage;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setVisibility(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.INVISIBLE;
        pokemonImage.setVisibility(visible);
        pokemonName.setVisibility(visible);
        pokemonHPImage.setVisibility(visible);
        pokemonHPText.setVisibility(visible);
    }
}
