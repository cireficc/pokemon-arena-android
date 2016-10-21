package com.pokemonbattlearena.android.fragments.battle;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.gson.Gson;
import com.pokemonbattlearena.android.BottomBarActivity;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleHomeFragment extends Fragment implements View.OnClickListener, RealTimeMessageReceivedListener {
    PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static String TAG = BattleHomeFragment.class.getSimpleName();
    private Button mBattleButton;
    private boolean battleBegun = false;
    private static int[] buttonIds = {R.id.move_button_0, R.id.move_button_1, R.id.move_button_2, R.id.move_button_3};

    private TypeModel mTypeModel;

    private PokemonTeam mPlayerTeam;

    private List<Move> mPlayerMoves;

    private Button[] mMoveButtons;

    private BattleViewItem mPlayerBattleView;

    private BattleViewItem mOpponentBattleView;

    public BattleHomeFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        String json = args.getString("pokemonTeamJSON");
        if (json != null) {
            mPlayerTeam = new Gson().fromJson(json, PokemonTeam.class);
        }
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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Pokemon pokemon = mPlayerTeam.getPokemons().get(0);
        mPlayerMoves = mApplication.getBattleDatabase().getMovesForPokemon(pokemon);
        configureMoveButtons();
        if (mPlayerBattleView != null) {
            mPlayerBattleView.setPokemon(pokemon);
            mPlayerBattleView.getPokemonImage().setImageDrawable(getDrawableForPokemon(getActivity(), pokemon.getName()));
            mPlayerBattleView.getPokemonName().setText(pokemon.getName());
            mPlayerBattleView.setVisibility(true);
        }
        if (mOpponentBattleView != null) {
            mOpponentBattleView.setPokemon(pokemon);
            mOpponentBattleView.getPokemonImage().setImageDrawable(getDrawableForPokemon(getActivity(), "mew"));
            mOpponentBattleView.getPokemonName().setText("Mew");
            mOpponentBattleView.setVisibility(true);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.battle_now_button:
                if (!battleBegun) {
                    battleBegun = true;
                    startBattle();
                    mBattleButton.setText(R.string.battle);
                } else {
                    mBattleButton.setText(R.string.cancel_battle);
                }
                break;
            case R.id.move_button_0:
                break;
            case R.id.move_button_1:
                break;
            case R.id.move_button_2:
                break;
            case R.id.move_button_3:
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(resultCode, intent);
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void startBattle() {
        startMatchMaking();
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        // Message format: pokemonID1:pokemonID2
        byte[] buf = realTimeMessage.getMessageData();
        String bufferString = new String(buf);
        Log.d(TAG, "Message Received: " + bufferString + " from: " + realTimeMessage.getSenderParticipantId());

    }

    private void setupMoveButtons(View v) {
        mMoveButtons = new Button[buttonIds.length];
        for (int i = 0; i < buttonIds.length; i++) {
            if (i < 4) {
                int buttonId = buttonIds[i];
                Button b = (Button) v.findViewById(buttonId);
                b.setVisibility(View.VISIBLE);
                b.setOnClickListener(this);
                mMoveButtons[i] = b;
            }
        }
    }

    // set the buttons to the current pokemon
    private void configureMoveButtons() {
        if (mPlayerMoves == null) {
            Log.e(TAG, "Player doesn't have moves");
        } else {
            for (int i = 0; i < buttonIds.length; i++) {
                mMoveButtons[i].setText(mPlayerMoves.get(i).getName());
                mMoveButtons[i].setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(mPlayerMoves.get(i).getType1())));
            }
        }
    }

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = c.getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    private void startMatchMaking() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mApplication.getGoogleApiClient(), roomConfig);

        // prevent screen from sleeping during handshake
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mApplication.getGoogleApiClient(), 1, 3);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            //TODO: exit the battle
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = makeBasicRoomConfigBuilder();
        rtmConfigBuilder.addPlayersToInvite(invitees);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Games.RealTimeMultiplayer.create(mApplication.getGoogleApiClient(), rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder((BottomBarActivity) getActivity())
                .setRoomStatusUpdateListener((BottomBarActivity) getActivity())
                .setMessageReceivedListener(this);
    }

    public void setBattleVisible(boolean visible) {
        if (mPlayerBattleView != null && mOpponentBattleView != null) {
            mPlayerBattleView.setVisibility(visible);
            mOpponentBattleView.setVisibility(visible);
        }
    }
}
