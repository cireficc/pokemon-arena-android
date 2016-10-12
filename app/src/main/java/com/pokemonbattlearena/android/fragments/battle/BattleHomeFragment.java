package com.pokemonbattlearena.android.fragments.battle;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.pokemonbattlearena.android.BottomBarActivity;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;

import java.util.ArrayList;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleHomeFragment extends Fragment implements View.OnClickListener {
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static String TAG = BattleHomeFragment.class.getSimpleName();
    private Button mBattleButton;
    private boolean battleBegun = false;
    private BattleUIFragment mBattleUIFragment;
    private Bundle mBattleArgs;

    public BattleHomeFragment() {
        super();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mBattleArgs = args;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battlehome, container, false);
        mBattleButton = (Button) view.findViewById(R.id.battle_now_button);
        mBattleButton.setOnClickListener(this);
        mBattleUIFragment = new BattleUIFragment();
        return view;
    }

    @Override
    public void onClick(View v) {
        if (!battleBegun) {
            startBattle();
            mBattleButton.setText(R.string.battle);
        } else {
            mBattleButton.setText(R.string.cancel_battle);
        }
    }

    private void setupBattleUI() {
        if (!mBattleUIFragment.isAdded()) {
            getFragmentManager().beginTransaction().add(R.id.battle_ui_container, mBattleUIFragment).commit();
            mBattleButton.setText(R.string.cancel_battle);
        }
    }

    public BattleUIFragment getBattleUIFragment() {
        return mBattleUIFragment;
    }

    public void startBattle() {
        setupBattleUI();
        if (!battleBegun) {
            battleBegun = true;
            startMatchMaking();
        }
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
                .setMessageReceivedListener(mBattleUIFragment);
    }
}
