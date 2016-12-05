package com.pokemonbattlearena.android.fragment.battle;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.pokemonbattlearena.android.application.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.util.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.fragment.team.PokemonGridAdapter;
import com.pokemonbattlearena.android.fragment.team.PokemonGridViewItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = BattleFragment.class.getSimpleName();
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private static int[] moveButtonIds = {R.id.move_button_0, R.id.move_button_1, R.id.move_button_2, R.id.move_button_3};

    private Button[] mMoveButtons;

    private TypeModel mTypeModel;

    private BattleViewItem mPlayerBattleView;

    private BattleViewItem mOpponentBattleView;

    private TextView mMoveHistoryText;

    private OnBattleFragmentTouchListener mCallback;

    private Map<Button, Move> mMoveButtonMap;

    private Button mSwitchButton;

    private BattlePokemonPlayer mPlayerBattlePlayer;
    private BattlePokemonPlayer mOpponentBattlePlayer;

    private final static int typePokemon = 0;
    private final static int typeStatus = 1;

    public BattleFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    public void enableButtonActions(boolean enabled) {
        String locked = enabled ? "UNLOCKED":"LOCKED";
        Log.d(TAG, "UI is now " + locked);
        for (Button button : mMoveButtons) {
            button.setEnabled(enabled);
        }
        mSwitchButton.setEnabled(enabled);
    }

    private void configureButtonsWithMoves(List<Move> moves) {
        mMoveButtonMap = new HashMap<>(4);
        for (int i = 0; i < moveButtonIds.length; i++) {
            Button b = mMoveButtons[i];
            Move move = moves.get(i);
            b.setOnClickListener(this);
            if (move != null) {
                b.setText(move.getName());
                b.setBackgroundColor(mApplication.getColor(mTypeModel.getColorForType(move.getType1())));
                b.setVisibility(View.VISIBLE);
            }
            mMoveButtonMap.put(b, move);
        }
    }

    public void refreshBattleUI(Battle activeBattle) {
            BattlePokemon self = activeBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon();
            BattlePokemon opponent = activeBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon();
            mPlayerBattleView.setActivePokemon(self);
            mOpponentBattleView.setActivePokemon(opponent);

            updatePokemonUI(self, opponent);
    }

    private void updatePokemonUI(BattlePokemon a, BattlePokemon b) {
        if (getView() != null) {
            Pokemon self = a.getOriginalPokemon();
            Pokemon opponent = b.getOriginalPokemon();
            mPlayerBattleView.pokemonImage.setImageDrawable(getDrawableForPokemon(self.getName(), typePokemon));
            mOpponentBattleView.pokemonImage.setImageDrawable(getDrawableForPokemon(opponent.getName(), typePokemon));

            mPlayerBattleView.pokemonName.setText(self.getName());
            mOpponentBattleView.pokemonName.setText(opponent.getName());

            mPlayerBattleView.pokemonHpProgress.setMax(self.getHp());
            mPlayerBattleView.pokemonHpProgress.setProgress(a.getCurrentHp());

            mOpponentBattleView.pokemonHpProgress.setMax(opponent.getHp());
            mOpponentBattleView.pokemonHpProgress.setProgress(b.getCurrentHp());

            updateStatusForPlayer();
            updateStatusForOpponent();

            configureButtonsWithMoves(a.getMoveSet());
        }
    }


    public interface OnBattleFragmentTouchListener {
        void onCancelBattle();
        void onMoveClicked(Move move);
        void onSwitchPokemon(int position);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle, container, false);
        Button mCancelBattleButton = (Button) view.findViewById(R.id.cancel_battle_button);
        mCancelBattleButton.setOnClickListener(this);
        mMoveHistoryText = (TextView) view.findViewById(R.id.move_history_text);
        mMoveHistoryText.setMovementMethod( new ScrollingMovementMethod());

        View playerView = view.findViewById(R.id.player_1_ui);
        View opponentView = view.findViewById(R.id.player_2_ui);

        mPlayerBattleView = new BattleViewItem(playerView);
        mPlayerBattleView.setVisibility(false);

        mOpponentBattleView = new BattleViewItem(opponentView);
        mOpponentBattleView.setVisibility(false);

        setupMoveButtons(view);

        mSwitchButton = (Button) view.findViewById(R.id.switch_button);
        mSwitchButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (BattleFragment.OnBattleFragmentTouchListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_battle_button:
                mCallback.onCancelBattle();
                break;
            case R.id.move_button_0:
                Log.d(TAG, mMoveButtonMap.get(v).getName());
                mCallback.onMoveClicked(mMoveButtonMap.get(v));
                break;
            case R.id.move_button_1:
                mCallback.onMoveClicked(mMoveButtonMap.get(v));
                break;
            case R.id.move_button_2:
                mCallback.onMoveClicked(mMoveButtonMap.get(v));
                break;
            case R.id.move_button_3:
                mCallback.onMoveClicked(mMoveButtonMap.get(v));
                break;
            case R.id.switch_button:
                switchPokemon();
            default:
                break;
        }
    }

    private void switchPokemon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View switchView = inflater.inflate(R.layout.switch_layout, null, false);

        ArrayList<Pokemon> pokemon = new ArrayList<>();
        for (BattlePokemon battlePokemon : mPlayerBattlePlayer.getBattlePokemonTeam().getBattlePokemons()) {
            pokemon.add(battlePokemon.getOriginalPokemon());
        }

        PokemonGridAdapter adapter = new PokemonGridAdapter(getActivity(), pokemon, R.layout.switch_grid_item);
        // keep track of if we selected a pokemon and which one was selected
        //    `selected[0]` holds the position of the pokemon the user tapped to switch. holds -1 if no pokemon selected.
        final Integer[] selected = new Integer[2];
        selected[0] = -1;
        selected[1] = 0;
        GridView gridView = (GridView) switchView.findViewById(R.id.switch_grid_view);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PokemonGridViewItem item = (PokemonGridViewItem) view.getTag();
                if (selected[0] < 0) {
                    selected[0] = position;
                    item.mCheckbox.setChecked(true);
                } else if (selected[0] == position){
                    selected[0] = -1;
                    item.mCheckbox.setChecked(false);
                }
            }
        });
        builder.setTitle(getString(R.string.tap_a_pokemon_to_switch));
        builder.setView(switchView);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Switch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selected[0] > -1) {
                    mCallback.onSwitchPokemon(selected[0]);
                    Log.d(TAG, "Switching to position: " + selected[0]);
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void setupMoveButtons(View v) {
        mMoveButtons = new Button[moveButtonIds.length];
        for (int i = 0; i < moveButtonIds.length; i++) {
            if (i < 4) {
                int buttonId = moveButtonIds[i];
                Button b = (Button) v.findViewById(buttonId);
                b.setVisibility(View.INVISIBLE);
                b.setOnClickListener(this);
                mMoveButtons[i] = b;
            }
        }
    }

    private Drawable getDrawableForPokemon(String name, int type) {
        String middle = "";
        switch (type) {
            case typePokemon:
                middle = "_pokemon_";
                break;
            case typeStatus:
                middle = "_status_";
                break;
            default:
                break;
        }
        String key = "ic" + middle + name.toLowerCase();
        int id = mApplication.getResources().getIdentifier(key, "drawable", mApplication.getPackageName());
        return mApplication.getDrawable(id);
    }

    public void setPlayer(BattlePokemonPlayer player) {
        this.mPlayerBattlePlayer = player;
        if (getView() != null) {
            mPlayerBattleView.setActivePokemon(player.getBattlePokemonTeam().getCurrentPokemon());
        }
    }

    public void setOpponent(BattlePokemonPlayer opponent) {
        mOpponentBattlePlayer = opponent;
        if (getView() != null) {
            mOpponentBattleView.setActivePokemon(opponent.getBattlePokemonTeam().getCurrentPokemon());
        }
    }

    public void setBattleVisible(boolean visible) {
        if (getView() != null) {
            mPlayerBattleView.setVisibility(visible);
            mOpponentBattleView.setVisibility(visible);
            mMoveHistoryText.setText(String.format("Match Started: %s", DateFormat.getDateTimeInstance().format(new Date())));
            mMoveHistoryText.setVisibility(View.VISIBLE);
        }
    }

    public void appendMoveHistory(String player, Move move) {
        Calendar c = Calendar.getInstance();
        String text = "\n" + " - " + player + " - " + move.getName() + " - " + move.getPower() + " damage";
        if (mMoveHistoryText != null) {
            mMoveHistoryText.append(text);
            final Layout layout = mMoveHistoryText.getLayout();
            if(layout != null){
                int scrollDelta = layout.getLineBottom(mMoveHistoryText.getLineCount() - 1)
                        - mMoveHistoryText.getScrollY() - mMoveHistoryText.getHeight();
                if(scrollDelta > 0)
                    mMoveHistoryText.scrollBy(0, scrollDelta);
            }
        }
    }

    public void initPokemonViewsForBattle() {
        if (getView() != null) {
            Pokemon self = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
            Pokemon opponent = mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();

            mPlayerBattleView.pokemonHpProgress.setMax(self.getHp());
            mPlayerBattleView.pokemonName.setText(self.getName());
            mPlayerBattleView.pokemonImage.setImageDrawable(getDrawableForPokemon(self.getName(), typePokemon));

            mPlayerBattleView.pokemonHpProgress.setProgress(mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getCurrentHp());

            mOpponentBattleView.pokemonHpProgress.setMax(opponent.getHp());
            mOpponentBattleView.pokemonName.setText(opponent.getName());
            mOpponentBattleView.pokemonImage.setImageDrawable(getDrawableForPokemon(opponent.getName(), typePokemon));

            mOpponentBattleView.pokemonHpProgress.setProgress(mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getCurrentHp());

            updateStatusForPlayer();
            updateStatusForOpponent();

            List<Move> moves = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getMoveSet();
            configureButtonsWithMoves(moves);
            mSwitchButton.setVisibility(View.VISIBLE);

            mPlayerBattleView.setVisibility(true);
            mOpponentBattleView.setVisibility(true);
        }
    }

    private void updateStatusForOpponent() {
        if (mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().isConfused()) {
            mOpponentBattleView.confusedStatusImage.setVisibility(View.VISIBLE);
        } else {
            mOpponentBattleView.confusedStatusImage.setVisibility(View.INVISIBLE);
        }
        if (mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().hasStatusEffect()) {
            StatusEffect e = mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getStatusEffect();
            Drawable drawable = getDrawableForPokemon(e.name().toLowerCase(), typeStatus);
            mOpponentBattleView.extraStatusImage.setImageDrawable(drawable);
            mPlayerBattleView.extraStatusImage.setVisibility(View.VISIBLE);
        } else {
            mOpponentBattleView.extraStatusImage.setVisibility(View.INVISIBLE);
        }
    }

    private void updateStatusForPlayer() {
        if (mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().isConfused()) {
            mPlayerBattleView.confusedStatusImage.setVisibility(View.VISIBLE);
        } else {
            mPlayerBattleView.confusedStatusImage.setVisibility(View.INVISIBLE);
        }
        if (mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().hasStatusEffect()) {
            StatusEffect e = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getStatusEffect();
            Drawable drawable = getDrawableForPokemon(e.name().toLowerCase(), typeStatus);
            mPlayerBattleView.extraStatusImage.setImageDrawable(drawable);
            mPlayerBattleView.extraStatusImage.setVisibility(View.VISIBLE);
        } else {
            mPlayerBattleView.extraStatusImage.setVisibility(View.INVISIBLE);
        }
    }

    public void updateHealthBars(int health1, int health2) {
        if (getView() != null) {
            mPlayerBattleView.pokemonHpProgress.setProgress(health1);
            mOpponentBattleView.pokemonHpProgress.setProgress(health2);
        }
    }
}
