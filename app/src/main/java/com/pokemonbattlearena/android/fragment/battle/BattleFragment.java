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
import com.pokemonbattlearena.android.util.PokemonUtils;
import com.pokemonbattlearena.android.util.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.StatusEffect;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.adapter.PokemonGridAdapter;
import com.pokemonbattlearena.android.fragment.team.PokemonGridViewItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pokemonbattlearena.android.util.PokemonUtils.getDrawableForPokemon;
import static com.pokemonbattlearena.android.util.PokemonUtils.typePokemon;
import static com.pokemonbattlearena.android.util.PokemonUtils.typeStatus;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = BattleFragment.class.getSimpleName();
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private static int[] moveButtonIds = {R.id.move_button_0, R.id.move_button_1, R.id.move_button_2, R.id.move_button_3};

    private Button[] mMoveButtons;

    private TypeModel mTypeModel;

    private BattleViewHolder mPlayerBattleView;

    private BattleViewHolder mOpponentBattleView;

    private TextView mMoveHistoryText;

    private OnBattleFragmentTouchListener mCallback;

    private Map<Button, Move> mMoveButtonMap;

    private Button mSwitchButton;

    private Button mCancelBattleButton;

    private BattlePokemonPlayer mPlayerBattlePlayer;
    private BattlePokemonPlayer mOpponentBattlePlayer;

    public BattleFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    public void enableButtonActions(boolean enabled) {
        String locked = enabled ? PokemonUtils.UNLOCKED:PokemonUtils.LOCKED;
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
            mPlayerBattlePlayer = activeBattle.getSelf();
            mOpponentBattlePlayer = activeBattle.getOpponent();

            updatePokemonUI();
    }

    private void updatePokemonUI() {
        if (getView() != null) {
            BattlePokemon self = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon();
            BattlePokemon opponent = mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon();
            Pokemon originalSelf = self.getOriginalPokemon();
            Pokemon originalOpponent = opponent.getOriginalPokemon();

            mPlayerBattleView.updateViews(mApplication, originalSelf);
            mOpponentBattleView.updateViews(mApplication, originalOpponent);
            mPlayerBattleView.updateHealthProgress(self.getCurrentHp());
            mOpponentBattleView.updateHealthProgress(opponent.getCurrentHp());

            updateStatusForPlayer();
            updateStatusForOpponent();

            configureButtonsWithMoves(self.getMoveSet());
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
        mCancelBattleButton = (Button) view.findViewById(R.id.cancel_battle_button);
        mCancelBattleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCancelBattle();
            }
        });
        mMoveHistoryText = (TextView) view.findViewById(R.id.move_history_text);
        mMoveHistoryText.setMovementMethod( new ScrollingMovementMethod());

        View playerView = view.findViewById(R.id.player_1_ui);
        View opponentView = view.findViewById(R.id.player_2_ui);

        View player1status = view.findViewById(R.id.player_1_status_layout);
        View player2status = view.findViewById(R.id.player_2_status_layout);

        mPlayerBattleView = new BattleViewHolder(playerView);
        mPlayerBattleView.setStatusLayout(player1status);
        mPlayerBattleView.setVisibility(false);
        mPlayerBattleView.setStatusConfusedVisible(View.INVISIBLE);
        mPlayerBattleView.setStatusExtraVisible(View.INVISIBLE);

        mOpponentBattleView = new BattleViewHolder(opponentView);
        mOpponentBattleView.setStatusLayout(player2status);
        mOpponentBattleView.setVisibility(false);
        mOpponentBattleView.setStatusConfusedVisible(View.INVISIBLE);
        mOpponentBattleView.setStatusExtraVisible(View.INVISIBLE);

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
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.switch_pokemon, new DialogInterface.OnClickListener() {
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



    public void setPlayer(BattlePokemonPlayer player) {
        this.mPlayerBattlePlayer = player;
        if (getView() != null) {

        }
    }

    public void setOpponent(BattlePokemonPlayer opponent) {
        mOpponentBattlePlayer = opponent;
        if (getView() != null) {
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
            if (mPlayerBattlePlayer != null && mOpponentBattlePlayer != null) {
                Pokemon self = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();
                Pokemon opponent = mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon();

                mPlayerBattleView.updateViews(mApplication, self);
                mOpponentBattleView.updateViews(mApplication, opponent);
                mPlayerBattleView.updateHealthProgress(mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getCurrentHp());
                mOpponentBattleView.updateHealthProgress(mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getCurrentHp());

                updateStatusForPlayer();
                updateStatusForOpponent();

                List<Move> moves = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon().getMoveSet();

                configureButtonsWithMoves(moves);
                mSwitchButton.setVisibility(View.VISIBLE);

                mPlayerBattleView.setVisibility(true);
                mOpponentBattleView.setVisibility(true);
            }
        }
    }

    private void updateStatusForOpponent() {
        BattlePokemon currentPokemon = mOpponentBattlePlayer.getBattlePokemonTeam().getCurrentPokemon();
        if (currentPokemon.hasStatusEffect()) {
            StatusEffect e = currentPokemon.getStatusEffect();
            Drawable drawable = getDrawableForPokemon(mApplication, e.name().toLowerCase(), typeStatus);
            mOpponentBattleView.extraStatusImage.setImageDrawable(drawable);
            mOpponentBattleView.extraStatusImage.setVisibility(View.VISIBLE);
        } else if (currentPokemon.isConfused()) {
            mOpponentBattleView.confusedStatusImage.setVisibility(View.VISIBLE);
        } else {
            mOpponentBattleView.setStatusConfusedVisible(View.INVISIBLE);
            mOpponentBattleView.setStatusExtraVisible(View.INVISIBLE);
        }
    }

    private void updateStatusForPlayer() {
        BattlePokemon currentPokemon = mPlayerBattlePlayer.getBattlePokemonTeam().getCurrentPokemon();
        if (currentPokemon.isConfused()) {
            mPlayerBattleView.confusedStatusImage.setVisibility(View.VISIBLE);
        } else {
            mPlayerBattleView.confusedStatusImage.setVisibility(View.INVISIBLE);
        }

        if (currentPokemon.hasStatusEffect()) {
            StatusEffect e = currentPokemon.getStatusEffect();
            Drawable drawable = getDrawableForPokemon(mApplication, e.toString(), typeStatus);
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
