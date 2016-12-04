package com.pokemonbattlearena.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.pokemonbattlearena.android.ApplicationPhase;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.CommandResult;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.engine.match.Switch;
import com.pokemonbattlearena.android.engine.match.SwitchResult;
import com.pokemonbattlearena.android.fragments.battle.BattleFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.gms.games.GamesStatusCodes.STATUS_OK;

public class BattleActivity extends BaseActivity implements OnTabSelectListener, RoomUpdateListener, RealTimeMessageReceivedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoomStatusUpdateListener {
    private static final String TAG = BattleActivity.class.getSimpleName();
    private static DatabaseReference mRootFirebase;
    private GoogleApiClient mGoogleApiClient;
    private FragmentManager mFragmentManager;
    private BattleFragment mBattleFragment;
    private BottomBar mBottomBar;
    private Battle mBattle;

    private static final int TEAM_SIZE_INT = 6;
    private static final int MIN_PLAYERS = 2;
    // GOOGLE PLAY GAMES FIELDS
    private static final int RC_SIGN_IN = 9001;
    private String mRoomId = null;
    private String mMyId = null;
    private String mOpponentId = null;
    private ArrayList<Participant> mParticipants = null;
    private String mHostId = null;
    private boolean mIsHost = false;
    private int mBattleMatchFlag = 0;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    private final RuntimeTypeAdapterFactory<Command> mCommandRuntimeTypeAdapter = RuntimeTypeAdapterFactory
            .of(Command.class, "type")
            .registerSubtype(Attack.class)
            .registerSubtype(Switch.class);

    private final RuntimeTypeAdapterFactory<CommandResult> mCommandResultRuntimeTypeAdapter = RuntimeTypeAdapterFactory
            .of(CommandResult.class, "type")
            .registerSubtype(AttackResult.class)
            .registerSubtype(SwitchResult.class);

    private final Gson mCommandGson = new GsonBuilder().registerTypeAdapterFactory(mCommandRuntimeTypeAdapter).create();
    private final Gson mCommandResultGson = new GsonBuilder().registerTypeAdapterFactory(mCommandResultRuntimeTypeAdapter).create();

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPreferences = getSharedPreferences("Pokemon Battle Prefs", Context.MODE_PRIVATE);
        mBottomBar = (BottomBar) findViewById(R.id.battle_bottom_bar);
        mBottomBar.setDefaultTab(R.id.tab_battle);
        mFragmentManager = getFragmentManager();

        // Button listeners
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleApiClient.connect();

        mBattleFragment = new BattleFragment();

        showProgressDialog();

        mRootFirebase = FirebaseDatabase.getInstance().getReference().child("playerid");
        mBottomBar.setOnTabSelectListener(this);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Already connected to Google");
        } else {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_teams:
                break;
            case R.id.tab_battle:
                break;
            case R.id.tab_chat:
                break;
        }
    }

    private void startMatchMaking() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, mBattleMatchFlag);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        if (mGoogleApiClient.isConnected()) {
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);
        } else {
            Log.e(TAG, "Not connected to Google Play Games");
        }

        // prevent screen from sleeping during handshake
        keepScreenOn();
    }

    //region RoomUpdateListener Callbacks
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        // we have left the room; return to main screen.
        leaveRoom();
        Log.d(TAG, "onLeftRoom, code " + statusCode);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            return;
        }
        updateRoom(room);
        /*
        user is connected to the room with at least one other person
        grab the opponents id and check firebase for their team
         */
        if (shouldStartGame(room)) {
            Log.d(TAG, "We are going to start!");
            String selfUsername = mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, "example");
            sendUsername(selfUsername);
        }
    }

    private void sendUsername(String selfUsername) {
        byte[] byteMessage = selfUsername.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, byteMessage,
                        mRoomId, p.getParticipantId());
                Log.d(TAG, "Reliable message sent to " + p.getParticipantId() + " + " + selfUsername);
            }
        }
    }

    private String getOpponentFromRoom(Room room) {
        room.getParticipantIds().remove(mMyId);
        return room.getParticipantIds().get(0);
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(this);
    }
    //endregion

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void setHost(ArrayList<String> sortedIds) {
        for (Participant mParticipant : mParticipants) {
            sortedIds.add(mParticipant.getParticipantId());
        }
        Collections.sort(sortedIds);
        mHostId = sortedIds.get(0);
        mIsHost = mHostId.equalsIgnoreCase(mMyId);
    }

    private void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
            mRoomId = room.getRoomId();
            mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
            ArrayList<String> sortedIds = new ArrayList<>();
            setHost(sortedIds);
        }
    }

    // Leave the room.
    private void leaveRoom() {
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            mBattle = null;
            mMyId = null;
            mParticipants = null;
            mIsHost = false;
            mHostId = null;
            Log.d(TAG, "Left room everything is null.");
        }
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        byte[] buf = realTimeMessage.getMessageData();
        String bufferString = new String(buf);
        Log.d(TAG, "In Game Message Received: " + bufferString);
    }

    private boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startMatchMaking();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        updateRoom(room);
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }
    //endregion
}
