package com.pokemonbattlearena.android.fragment.battle;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.util.PokemonUtils;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewHolder {
    private final static String TAG = BattleViewHolder.class.getSimpleName();
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    SeekBar pokemonHpProgress;
    ImageView confusedStatusImage;
    ImageView extraStatusImage;

    public BattleViewHolder(View playerView) {
        this.pokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        this.pokemonImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        this.pokemonHpProgress = (SeekBar) playerView.findViewById(R.id.hp_imageview);
        this.pokemonHPText = (TextView) playerView.findViewById(R.id.hp_textview);
    }

    public void setVisibility(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.INVISIBLE;
        this.pokemonImage.setVisibility(visible);
        this.pokemonName.setVisibility(visible);
        this.pokemonHpProgress.setVisibility(visible);
        this.pokemonHPText.setVisibility(visible);
    }

    public void setStatusLayout(View statusView) {
        this.confusedStatusImage = (ImageView) statusView.findViewById(R.id.status_effect_confusion);
        this.extraStatusImage = (ImageView) statusView.findViewById(R.id.extra_status_effect);
    }

    public void setStatusConfusedVisible(int visible) {
        if (this.confusedStatusImage != null) {
            this.confusedStatusImage.setVisibility(visible);
        }
    }

    public void setStatusExtraVisible(int visible) {
        if (this.extraStatusImage != null) {
            this.extraStatusImage.setVisibility(visible);
        }
    }

    public void updateViews(Context c, Pokemon original) {
        this.pokemonImage.setImageDrawable(PokemonUtils.getDrawableForPokemon(c, original.getName(), PokemonUtils.typePokemon));
        this.pokemonName.setText(original.getName());
        this.pokemonHpProgress.setMax(original.getHp());
        int backgroundId = c.getResources().getIdentifier("background_" + original.getType1().toLowerCase(), "drawable", c.getPackageName());
        this.pokemonImage.setBackgroundResource(backgroundId);
    }

    public void updateHealthProgress(int currentHp) {
        this.pokemonHpProgress.setProgress(currentHp);
    }
}
