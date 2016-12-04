package com.pokemonbattlearena.android.fragments.battle;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    SeekBar pokemonHpProgress;
    BattlePokemon activePokemon;
    //TODO: use battle pokemon player
    //TODO: create a method to update the player when the battle changes
    BattlePokemonPlayer battlePokemonPlayer;
    boolean hasConfusion = false;
    ImageView confusedStatusImage;
    ImageView extraStatusImage;

    private final static String TAG = BattleViewItem.class.getSimpleName();

    public BattleViewItem() {

    }

    public void setPlayerView(View playerView) {
        this.pokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        this.pokemonImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        this.pokemonHpProgress = (SeekBar) playerView.findViewById(R.id.hp_imageview);
        this.pokemonHPText = (TextView) playerView.findViewById(R.id.hp_textview);
        this.confusedStatusImage = (ImageView) playerView.findViewById(R.id.status_effect_confusion);
        this.extraStatusImage = (ImageView) playerView.findViewById(R.id.extra_status_effect);
        this.confusedStatusImage.setVisibility(View.GONE);
        this.extraStatusImage.setVisibility(View.GONE);
    }

    public BattlePokemonPlayer getActivePlayer() {
        return battlePokemonPlayer;
    }

    public void setActivePlayer(BattlePokemonPlayer player) {
        this.battlePokemonPlayer = player;
    }

    public void setActivePokemon(BattlePokemon activePokemon, Drawable d) {
        this.activePokemon = activePokemon;
        this.pokemonHpProgress.setMax(activePokemon.getOriginalPokemon().getHp());
        this.pokemonName.setText(activePokemon.getOriginalPokemon().getName());
        this.pokemonImage.setImageDrawable(d);

        setHPBar(activePokemon.getOriginalPokemon().getHp());
    }

    public void setVisibility(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.INVISIBLE;
        this.pokemonImage.setVisibility(visible);
        this.pokemonName.setVisibility(visible);
        this.pokemonHpProgress.setVisibility(visible);
        this.pokemonHPText.setVisibility(visible);
    }

    public void updateHealthBar(int amount) {
        Log.e("UPDATING HEALTH AT", "HEALTH: " + amount);
       pokemonHpProgress.setProgress(amount);
    }

    public void setHPBar(int hp) {
        pokemonHpProgress.setMax(hp);
        pokemonHpProgress.setProgress(hp);
    }

    public boolean getConfusion() {
        return hasConfusion;
    }

    public void updateStatusEffect(boolean isConfusion, Drawable d) {
        if (isConfusion) {
            hasConfusion = isConfusion;
            confusedStatusImage.setImageDrawable(d);
            confusedStatusImage.setVisibility(View.VISIBLE);
        } else {
            extraStatusImage.setImageDrawable(d);
            extraStatusImage.setVisibility(View.VISIBLE);
        }
    }

    public void removeStatusEffects() {
        hasConfusion = false;
        this.confusedStatusImage.setImageResource(0);
        this.extraStatusImage.setImageResource(0);
    }
}
