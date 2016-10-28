package com.pokemonbattlearena.android.fragments.battle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    ImageView pokemonHPImage;
    //TODO: Change how active pokemon is displayed once switching can happen!
    Pokemon activePokemon;
    PokemonPlayer activePlayer;

    public BattleViewItem(ImageView pokemonImage, TextView pokemonName, TextView pokemonHPText, ImageView pokemonHPImage) {
        this.pokemonImage = pokemonImage;
        this.pokemonName = pokemonName;
        this.pokemonHPText = pokemonHPText;
        this.pokemonHPImage = pokemonHPImage;
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

    public PokemonPlayer getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(PokemonPlayer player) {
        this.activePlayer = player;
    }

    public Pokemon getActivePokemon() {
        return activePokemon;
    }

    public void setActivePokemon(Pokemon activePokemon) {
        this.activePokemon = activePokemon;
    }

    public void setVisibility(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.INVISIBLE;
        pokemonImage.setVisibility(visible);
        pokemonName.setVisibility(visible);
        pokemonHPImage.setVisibility(visible);
        pokemonHPText.setVisibility(visible);
    }
}