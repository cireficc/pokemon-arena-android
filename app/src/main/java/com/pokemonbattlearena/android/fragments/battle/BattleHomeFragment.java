package com.pokemonbattlearena.android.fragments.battle;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;

import java.util.List;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleHomeFragment extends Fragment implements View.OnClickListener {
    PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static String TAG = BattleHomeFragment.class.getSimpleName();
    private Button mBattleButton;
    private static int[] buttonIds = {R.id.move_button_0, R.id.move_button_1, R.id.move_button_2, R.id.move_button_3};

    private TypeModel mTypeModel;

    private List<Move> mPlayerMoves;

    private Button[] mMoveButtons;

    private BattleViewItem mPlayerBattleView;

    private BattleViewItem mOpponentBattleView;

    private OnBattleFragmentTouchListener mCallback;

    public BattleHomeFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    public interface OnBattleFragmentTouchListener {
        void onBattleNowClicked();
        void onMoveClicked(Move move);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battlehome, container, false);
        mBattleButton = (Button) view.findViewById(R.id.battle_now_button);
        mBattleButton.setOnClickListener(this);
        View playerView = view.findViewById(R.id.player_1_ui);
        View opponentView = view.findViewById(R.id.player_2_ui);

        TextView pokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        ImageView pokemonImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        ImageView pokemonHPImage = (ImageView) playerView.findViewById(R.id.hp_imageview);
        TextView pokemonHPText = (TextView) playerView.findViewById(R.id.hp_textview);

        mPlayerBattleView = new BattleViewItem(pokemonImage, pokemonName, pokemonHPText, pokemonHPImage);
        mPlayerBattleView.setVisibility(false);

        pokemonName = (TextView) opponentView.findViewById(R.id.active_name_textview);
        pokemonImage = (ImageView) opponentView.findViewById(R.id.active_imageview);
        pokemonHPImage = (ImageView)  opponentView.findViewById(R.id.hp_imageview);
        pokemonHPText = (TextView)  opponentView.findViewById(R.id.hp_textview);

        //TODO: don't create opponent until match starts

        mOpponentBattleView = new BattleViewItem(pokemonImage, pokemonName, pokemonHPText, pokemonHPImage);
        mOpponentBattleView.setVisibility(false);

        setupMoveButtons(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (BattleHomeFragment.OnBattleFragmentTouchListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //TODO: Leave the Room
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.battle_now_button:
                mCallback.onBattleNowClicked();
                break;
            case R.id.move_button_0:
                mCallback.onMoveClicked(mPlayerMoves.get(0));
                break;
            case R.id.move_button_1:
                mCallback.onMoveClicked(mPlayerMoves.get(1));
                break;
            case R.id.move_button_2:
                mCallback.onMoveClicked(mPlayerMoves.get(2));
                break;
            case R.id.move_button_3:
                mCallback.onMoveClicked(mPlayerMoves.get(3));
                break;
            default:
                break;
        }
    }

    private void setupMoveButtons(View v) {
        mMoveButtons = new Button[buttonIds.length];
        for (int i = 0; i < buttonIds.length; i++) {
            if (i < 4) {
                int buttonId = buttonIds[i];
                Button b = (Button) v.findViewById(buttonId);
                b.setVisibility(View.INVISIBLE);
                b.setOnClickListener(this);
                mMoveButtons[i] = b;
            }   
        }
    }

    // set the buttons to the current activePokemon
    private void configureMoveButtons() {
        if (mPlayerMoves != null) {
            for (int i = 0; i < buttonIds.length; i++) {
                Move m = mPlayerMoves.get(i);
                if (m != null) {
                    mMoveButtons[i].setText(m.getName());
                    mMoveButtons[i].setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(m.getType1())));
                    mMoveButtons[i].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = c.getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    public void setPlayer(PokemonPlayer player) {
        mPlayerBattleView.setActivePlayer(player);
        Pokemon activePokemon = player.getPokemonTeam().getPokemons().get(0);
        mPlayerBattleView.setActivePokemon(activePokemon);
        mPlayerBattleView.getPokemonImage().setImageDrawable(getDrawableForPokemon(getActivity(), activePokemon.getName()));
        mPlayerBattleView.getPokemonName().setText(activePokemon.getName());
        mPlayerMoves = mApplication.getBattleDatabase().getMovesForPokemon(activePokemon);
        configureMoveButtons();
    }

    public void setOpponent(PokemonPlayer opponent) {
        mOpponentBattleView.setActivePlayer(opponent);
        Pokemon activePokemon = opponent.getPokemonTeam().getPokemons().get(0);
        mOpponentBattleView.setActivePokemon(activePokemon);
        mOpponentBattleView.getPokemonImage().setImageDrawable(getDrawableForPokemon(getActivity(), activePokemon.getName()));
        mOpponentBattleView.getPokemonName().setText(activePokemon.getName());
    }

    public void setBattleVisible(boolean visible) {
        if (mPlayerBattleView != null && mOpponentBattleView != null) {
            mPlayerBattleView.setVisibility(visible);
            mOpponentBattleView.setVisibility(visible);
        }
    }
}
