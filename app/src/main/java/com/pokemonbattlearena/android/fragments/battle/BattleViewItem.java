package com.pokemonbattlearena.android.fragments.battle;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    SeekBar pokemonHpProgress;
    Pokemon activePokemon;
    PokemonPlayer activePlayer;

    private final static String TAG = BattleViewItem.class.getSimpleName();

    public BattleViewItem(View playerView) {
        this.pokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        this.pokemonImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        this.pokemonHpProgress = (SeekBar) playerView.findViewById(R.id.hp_imageview);
        this.pokemonHPText = (TextView) playerView.findViewById(R.id.hp_textview);
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

    public void setActivePokemon(Pokemon activePokemon, Drawable d) {
        this.activePokemon = activePokemon;
        this.pokemonHpProgress.setMax(activePokemon.getHp());
        this.pokemonName.setText(activePokemon.getName());
        this.pokemonImage.setImageDrawable(d);
        setHPBar(activePokemon.getHp());
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
