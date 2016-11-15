package com.pokemonbattlearena.android.fragments.battle;

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

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.fragments.team.PokemonGridAdapter;
import com.pokemonbattlearena.android.fragments.team.PokemonGridViewItem;

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

public class BattleHomeFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = BattleHomeFragment.class.getSimpleName();
    private static int[] moveButtonIds = {R.id.move_button_0, R.id.move_button_1, R.id.move_button_2, R.id.move_button_3};

    private Button[] mMoveButtons;

    private TypeModel mTypeModel;

    private BattleViewItem mPlayerBattleView;

    private BattleViewItem mOpponentBattleView;

    private TextView mMoveHistoryText;

    private OnBattleFragmentTouchListener mCallback;

    private Map<Button, Move> mMoveButtonMap;

    private Button mSwitchButton;

    public BattleHomeFragment() {
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
                b.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(move.getType1())));
                b.setVisibility(View.VISIBLE);
            }
            mMoveButtonMap.put(b, move);
        }
    }

    public void refreshActivePokemon(Battle activeBattle) {
        BattlePokemon self = activeBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon();
        BattlePokemon opponent = activeBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon();
        mPlayerBattleView.setActivePokemon(self.getOriginalPokemon(), getDrawableForPokemon(getActivity(), self.getOriginalPokemon().getName()));
        configureButtonsWithMoves(self.getMoveSet());
        mOpponentBattleView.setActivePokemon(opponent.getOriginalPokemon(), getDrawableForPokemon(getActivity(), opponent.getOriginalPokemon().getName()));
        updateHealthBars(self.getCurrentHp(), opponent.getCurrentHp());
    }

    public interface OnBattleFragmentTouchListener {
        void onCancelBattle();
        void onMoveClicked(Move move);
        void onSwitchPokemon(int position);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battlehome, container, false);
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
        ArrayList<Pokemon> pokemon = (ArrayList<Pokemon>) mPlayerBattleView.getActivePlayer().getPokemonTeam().getPokemons();
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

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = c.getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    public void setPlayer(PokemonPlayer player) {
        mPlayerBattleView.setActivePlayer(player);
        Pokemon activePokemon = player.getPokemonTeam().getPokemons().get(0);
        mPlayerBattleView.setActivePokemon(activePokemon, getDrawableForPokemon(getActivity(), activePokemon.getName()));
        List<Move> moves = activePokemon.getActiveMoveList();
        configureButtonsWithMoves(moves);
        mSwitchButton.setVisibility(View.VISIBLE);
    }

    public void setOpponent(PokemonPlayer opponent) {
        mOpponentBattleView.setActivePlayer(opponent);
        Pokemon activePokemon = opponent.getPokemonTeam().getPokemons().get(0);
        mOpponentBattleView.setActivePokemon(activePokemon, getDrawableForPokemon(getActivity(), activePokemon.getName()));
    }

    public void setBattleVisible(boolean visible) {
        if (mPlayerBattleView != null && mOpponentBattleView != null) {
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

    public void updateHealthBars(int health1, int health2) {
        mPlayerBattleView.updateHealthBar(health1);
        mOpponentBattleView.updateHealthBar(health2);
    }
}
