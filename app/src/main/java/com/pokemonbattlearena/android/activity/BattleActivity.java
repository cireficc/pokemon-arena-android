package com.pokemonbattlearena.android.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.pokemonbattlearena.android.BattleEndListener;
import com.pokemonbattlearena.android.application.ApplicationPhase;
import com.pokemonbattlearena.android.application.PokemonBattleApplication;
import com.pokemonbattlearena.android.engine.BattleEngine;
import com.pokemonbattlearena.android.engine.ai.AiBattle;
import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.match.NoP;
import com.pokemonbattlearena.android.util.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonPlayer;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.CommandResult;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.engine.match.Switch;
import com.pokemonbattlearena.android.engine.match.SwitchResult;
import com.pokemonbattlearena.android.fragment.battle.BattleFragment;
import com.pokemonbattlearena.android.fragment.chat.ChatBattleFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.gms.games.GamesStatusCodes.STATUS_OK;

public class BattleActivity extends BaseActivity implements OnTabSelectListener, RoomUpdateListener, RealTimeMessageReceivedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoomStatusUpdateListener, BattleFragment.OnBattleFragmentTouchListener, ChatBattleFragment.OnGameChatLoadedListener, BattleEndListener {
    private static final String TAG = BattleActivity.class.getSimpleName();
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private static DatabaseReference mRootFirebase;
    private GoogleApiClient mGoogleApiClient;
    private FragmentManager mFragmentManager;
    private BattleFragment mBattleFragment;
    private ChatBattleFragment mChatFragment;
    private BottomBar mBottomBar;
    private Battle mBattle;

    private static final int TEAM_SIZE_INT = 6;
    private static final int MIN_PLAYERS = 2;
    // GOOGLE PLAY GAMES FIELDS
    private static final int RC_SIGN_IN = 9001;
    private String mRoomId = null;
    private String mMyId = null;
    private ArrayList<Participant> mParticipants = null;
    private String mHostId = null;
    private boolean mIsHost = false;
    private int mBattleMatchFlag = 0;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    private String mOpponentUsername = null;
    private String mUsername = null;

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
    private boolean isAiBattle;
    private BattleEndListener battleEndListener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isAiBattle = getIntent().getBooleanExtra(PokemonUtils.AI_BATTLE_KEY, false);
        mPreferences = getSharedPreferences("Pokemon Battle Prefs", Context.MODE_PRIVATE);
        mBottomBar = (BottomBar) findViewById(R.id.battle_bottom_bar);
        mBottomBar.setDefaultTab(R.id.tab_battle);
        mFragmentManager = getFragmentManager();
        mUsername = mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, "example");
        // Button listeners
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleApiClient.connect();

        mBattleFragment = new BattleFragment();

        mFragmentManager.beginTransaction()
                .add(R.id.battle_container, mBattleFragment, "battle")
                .hide(mBattleFragment)
                .commit();


        showProgressDialog();

        mRootFirebase = FirebaseDatabase.getInstance().getReference().child(PokemonUtils.PROFILE_NAME_KEY);
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
    public void onStop() {
        leaveRoom();
        super.onStop();
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_teams:
                break;
            case R.id.tab_battle:
                if (mBattleFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .show(mBattleFragment)
                            .hide(mChatFragment)
                            .commit();
                }
                break;
            case R.id.tab_chat:
                if (mChatFragment == null) {
                    mChatFragment = new ChatBattleFragment();
                    mFragmentManager.beginTransaction()
                            .add(R.id.battle_container, mChatFragment, "chat")
                            .hide(mBattleFragment)
                            .commit();
                } else if (mChatFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .show(mChatFragment)
                            .hide(mBattleFragment)
                            .commit();
                }
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
        if (mOpponentUsername == null) {
            mOpponentUsername = bufferString.trim();
            setupBattleWithOpponent();
            return;
        } else {
            if (mIsHost) {
                Command command = mCommandGson.fromJson(bufferString, Command.class);
                Log.d(TAG, "We got a command from client of type: " + command.getClass());
                boolean phaseReady = mBattle.getCurrentBattlePhase().queueCommand(command);
                if (phaseReady) {
                    handleBattleResult();
                }
            } else {
                BattlePhaseResult resultFromJson = mCommandResultGson.fromJson(bufferString, BattlePhaseResult.class);
                Log.d(TAG, "We got a battle phase result: " + resultFromJson.toString());
                for (CommandResult commandResult : resultFromJson.getCommandResults()) {
                    // Update the internal state of the battle (only host really needs to do this, but opponent can too)
                    // Have opponent update their own battle state if you want to use the Battle object directly to update the UI (which makes more sense, IMO)
                    mBattle.applyCommandResult(commandResult);
                    Log.d(TAG, commandResult.getTargetInfo().toString());
                }

                mBattleFragment.enableButtonActions(true);
                if (mBattle.selfPokemonFainted()) {
                    Button force;
                    force = (Button)findViewById(R.id.switch_button);
                    force.performClick();
                }
            }
            mBattleFragment.refreshBattleUI(mBattle);
        }

    }

    private void setupBattleWithOpponent() {
        //TODO: Don't use the players saved team for the opponent.
        // get this from firebase using the ID sent from the message
        if (isAiBattle) {
            PokemonPlayer player = new PokemonPlayer(mUsername);
            player.setPokemonTeam(getSavedTeam());
            PokemonPlayer ai = new AiPlayer(mApplication.getBattleDatabase(), player);
            mBattle = new AiBattle(player, (AiPlayer) ai);
            mBattleFragment.setPlayer(new BattlePokemonPlayer(player));
            mBattleFragment.setOpponent(new BattlePokemonPlayer(ai));
        } else {
            BattlePokemonPlayer opponent = getPlayerForTeam(mOpponentUsername, getSavedTeam());
            BattlePokemonPlayer self = getPlayerForTeam(mUsername, getSavedTeam());
            mBattle = new Battle(self, opponent);
            mBattleFragment.setPlayer(self);
            mBattleFragment.setOpponent(opponent);
        }

        mFragmentManager.beginTransaction()
                .show(mBattleFragment)
                .commit();

        mBattleFragment.initPokemonViewsForBattle();

        hideProgressDialog();
    }


    private BattlePokemonPlayer getPlayerForTeam(String name, PokemonTeam team) {
        PokemonPlayer opponentPlayer = new PokemonPlayer(name);
        opponentPlayer.setPokemonTeam(team);
        BattlePokemonPlayer opponent = new BattlePokemonPlayer(opponentPlayer);
        return opponent;
    }

    private PokemonTeam getOpponentTeamFromFirebase() {
        //TODO: Use this method to get the opponents team from firebase using 'mOpponentUsername' for the player_name field

        return null;
    }

    private PokemonTeam getSavedTeam() {
        String teamJSON = mPreferences.getString("pokemon_team", "mew");
        if (!teamJSON.equals("mew")) {
            Log.d(TAG, "Got saved team: " + teamJSON);
            return new Gson().fromJson(teamJSON, PokemonTeam.class);
        }
        return null;
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
        if (!isAiBattle) {
            startMatchMaking();
        } else {
            setupBattleWithOpponent();
        }
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

    @Override
    public void onCancelBattle() {
        leaveRoom();
        finish();
    }

    @Override
    public void onMoveClicked(Move move) {
        if (mBattle instanceof AiBattle) {
            if (mBattleFragment != null) {
                Attack attack = new Attack(mBattle.getSelf(), mBattle.getOpponent(), move);
                AIBattleTurn(attack);
            }
        } else {
            Attack attack = new Attack(mBattle.getSelf(), mBattle.getOpponent(), move);
            if (mIsHost) {
                Log.d(TAG, "Host: queuing move: " + move.getName());
                queueHostMessage(attack);
            } else {
                Log.d(TAG, "Client: sending move: " + move.getName());
                sendClientMessage(attack);
            }
        }
    }

    //region Networking Helper methods
    private void queueHostMessage(Command c) {
        boolean movesReady = mBattle.getCurrentBattlePhase().queueCommand(c);
        mBattleFragment.enableButtonActions(movesReady);
        if (movesReady) {
            handleBattleResult();
        }
    }

    private void sendClientMessage(Command c) {
        String gson = mCommandGson.toJson(c, Command.class);
        sendMessage(gson);
        mBattleFragment.enableButtonActions(false);
    }

    public void AIBattleTurn(Command cmd) {
        if (mBattle instanceof AiBattle) {
            mBattleFragment.enableButtonActions(false);
            mBattle.getCurrentBattlePhase().queueCommand(cmd);

            if (mBattle.oppPokemonFainted()) {

                ((AiBattle) mBattle).buildIntelligence(mBattle, true);
                Command aiCommand = ((AiBattle) mBattle).showIntelligence();
                mBattle.getCurrentBattlePhase().queueCommand(aiCommand);

            } else if (mBattle.selfPokemonFainted()) {
                Command aiCommand = new NoP(mBattle.getOpponent());
                mBattle.getCurrentBattlePhase().queueCommand(aiCommand);
            } else{
                ((AiBattle) mBattle).buildIntelligence(mBattle, false);
                Command aiCommand = ((AiBattle) mBattle).showIntelligence();
                mBattle.getCurrentBattlePhase().queueCommand(aiCommand);
            }
            mBattleFragment.refreshBattleUI(mBattle);
            handleBattleResult();
        }
    }

    private void handleBattleResult() {
        BattlePhaseResult result = mBattle.executeCurrentBattlePhase();
        PokemonTeam pokes = new PokemonTeam(6);
        boolean nop = false;
        for (BattlePokemon bp : mBattle.getSelf().getBattlePokemonTeam().getBattlePokemons()) {
            pokes.addPokemon(bp.getOriginalPokemon());
        }

        for (CommandResult commandResult : result.getCommandResults()) {

            mBattle.applyCommandResult(commandResult);

            if (mBattle.isFinished()) {
                battleEndListener.onBattleEnd();
                leaveRoom();
                return;
            }

            if (mBattle.oppPokemonFainted()) {
                nop = true;
            }
        }
        mBattleFragment.enableButtonActions(true);
        mBattleFragment.refreshBattleUI(mBattle);

        if(!isAiBattle) {
            String json = mCommandResultGson.toJson(result);
            sendMessage(json);
        }

        mBattle.startNewBattlePhase();

        if (mBattle.selfPokemonFainted()) {
            Button force;
            force = (Button)findViewById(R.id.switch_button);
            force.performClick();
            return;
        } else if (nop) {
            if (isAiBattle) {
                AIBattleTurn(new NoP(mBattle.getSelf()));
            } else {
                mBattle.getCurrentBattlePhase().queueCommand(new NoP(mBattle.getSelf()));
                mBattleFragment.enableButtonActions(false);
            }
        }
    }

    private void sendMessage(String message) {
        byte[] byteMessage = message.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, byteMessage,
                        mRoomId, p.getParticipantId());
                Log.d(TAG, "Reliable message sent (sendMessage()) + " + message);
            }
        }
    }

    @Override
    public void onSwitchPokemon(int position) {
        Switch s = new Switch(mBattle.getSelf(), position);
        if (mBattle instanceof AiBattle) {
            AIBattleTurn(s);
        } else {
            if (mIsHost) {
                queueHostMessage(s);
            } else {
                sendClientMessage(s);
            }
        }
    }

    @Override
    public void onChatLoaded() {
        hideProgressDialog();
    }

    @Override
    public String getHostId() {
        return mUsername;
    }

    @Override
    public void onBattleEnd() {

        if (mBattle instanceof AiBattle) {
            if (mBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication," AI has won the battle", Toast.LENGTH_LONG).show();
            } else if (mBattle.oppPokemonFainted()) {
                Toast.makeText(mApplication," You have won the battle", Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (mBattle.isFinished()) {
            if (mBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication, "A player" + " has won the battle", Toast.LENGTH_LONG).show();
                return;
            } else if (mBattle.oppPokemonFainted()){
                Toast.makeText(mApplication, "A player" + " has won the battle", Toast.LENGTH_LONG).show();
                return;
            }
        }
        finish();
    }
}
