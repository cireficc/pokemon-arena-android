package com.pokemonbattlearena.android.fragment.battle;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;

/**
 * Created by Spencer Amann on 10/20/16.
 */

public class BattleViewItem {
    private final static String TAG = BattleViewItem.class.getSimpleName();
    ImageView pokemonImage;
    TextView pokemonName;
    TextView pokemonHPText;
    SeekBar pokemonHpProgress;
    BattlePokemon activePokemon;
    ImageView confusedStatusImage;
    ImageView extraStatusImage;

    public BattleViewItem(View playerView) {
        this.pokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        this.pokemonImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        this.pokemonHpProgress = (SeekBar) playerView.findViewById(R.id.hp_imageview);
        this.pokemonHPText = (TextView) playerView.findViewById(R.id.hp_textview);
    }

    public void setActivePokemon(BattlePokemon activePokemon) {
        this.activePokemon = activePokemon;
    }

    public void setVisibility(boolean visibility) {
        int visible = visibility ? View.VISIBLE : View.INVISIBLE;
        this.pokemonImage.setVisibility(visible);
        this.pokemonName.setVisibility(visible);
        this.pokemonHpProgress.setVisibility(visible);
        this.pokemonHPText.setVisibility(visible);
    }

    public void setStatusLayout(View status) {
        this.confusedStatusImage = (ImageView) status.findViewById(R.id.status_effect_confusion);
        this.extraStatusImage = (ImageView) status.findViewById(R.id.extra_status_effect);
        this.confusedStatusImage.setVisibility(View.INVISIBLE);
        this.extraStatusImage.setVisibility(View.INVISIBLE);
    }
}
