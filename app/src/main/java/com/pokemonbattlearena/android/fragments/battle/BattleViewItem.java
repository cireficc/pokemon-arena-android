package com.pokemonbattlearena.android.fragments.battle;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    SeekBar pokemonHpProgress;
    //TODO: Change how active pokemon is displayed once switching can happen!
    Pokemon activePokemon;
    PokemonPlayer activePlayer;

    public BattleViewItem(ImageView pokemonImage, TextView pokemonName, TextView pokemonHPText, SeekBar pokemonHpProgress) {
        this.pokemonImage = pokemonImage;
        this.pokemonName = pokemonName;
        this.pokemonHPText = pokemonHPText;
        this.pokemonHpProgress = pokemonHpProgress;
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

    public SeekBar getPokemonHpProgress() {
        return pokemonHpProgress;
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
        pokemonHpProgress.setVisibility(visible);
        pokemonHPText.setVisibility(visible);
    }

    public void updateHealthBar(int amount) {
        Log.e("UPDATING HEALTH AT", "HEALTH: " + amount);
       pokemonHpProgress.setProgress(amount);
    }

    public void setHPBar(int hp) {
        pokemonHpProgress.setMax(hp);
        pokemonHpProgress.setProgress(hp);
    }
}
