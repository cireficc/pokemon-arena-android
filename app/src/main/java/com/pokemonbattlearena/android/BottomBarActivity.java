package com.pokemonbattlearena.android;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.pokemonbattlearena.android.activity.*;
import com.pokemonbattlearena.android.engine.ai.AiBattle;
import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.Command;
import com.pokemonbattlearena.android.engine.match.CommandResult;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.engine.match.Switch;
import com.pokemonbattlearena.android.engine.match.SwitchResult;
import com.pokemonbattlearena.android.fragments.battle.BattleHomeFragment;
import com.pokemonbattlearena.android.fragments.battle.MainMenuFragment;
import com.pokemonbattlearena.android.fragments.chat.ChatHomeFragment;
import com.pokemonbattlearena.android.fragments.chat.ChatInGameFragment;
import com.pokemonbattlearena.android.fragments.team.SavedTeamsFragment;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment.OnPokemonTeamSelectedListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.stephentuso.welcome.WelcomeHelper;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.games.GamesStatusCodes.STATUS_OK;
import static com.google.android.gms.games.GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED;
import static com.google.android.gms.games.GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED;

public class BottomBarActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        RealTimeMessageReceivedListener,
        RoomUpdateListener,
        RoomStatusUpdateListener,
        RealTimeMultiplayer.ReliableMessageSentCallback,
        OnPokemonTeamSelectedListener,
        BattleHomeFragment.OnBattleFragmentTouchListener,
        MainMenuFragment.OnMenuFragmentTouchListener,
        ChatHomeFragment.OnChatLoadedListener,
        ChatInGameFragment.OnGameChatLoadedListener,
        SavedTeamsFragment.OnSavedTeamsFragmentTouchListener,
        BattleEndListener {

    private static final String TAG = BottomBarActivity.class.getSimpleName();
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

    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();

    // GOOGLE PLAY SIGN IN FIELDS
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    // FRAGMENTS
    private FragmentManager mFragmentManager;
    private MainMenuFragment mMainMenuFragment;
    private BattleHomeFragment mBattleHomeFragment;
    private SavedTeamsFragment mSavedTeamsFragment;
    private TeamsHomeFragment mTeamsHomeFragment;
    private ChatHomeFragment mChatHomeFragment;
    private ChatInGameFragment mChatInGameFragment;

    // UI Elements
    private BottomBar mBottomBar;
    private SharedPreferences mPreferences;

    // Battle Fields
    private PokemonPlayer mCurrentPokemonPlayer;
    private PokemonPlayer mOpponentPokemonPlayer;
    private Battle mActiveBattle = null;
    private boolean isAiBattle = false;

    //TODO Temporary integer used for AI switching. Need 2 kill. Need 4 Speed.
    private int ind = 0;

    //SAVED TEAMS
    private String newestAddedPokemonTeamName;

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

    // BATTLE END
    private BattleEndListener battleEndListener = this;

    //Splash
    private WelcomeHelper mWelcomeHelper;

    /********************************************************************************************
     * ACTIVITY
     *******************************************************************************************/
    //region Activity hooks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottombar);

        mWelcomeHelper = new WelcomeHelper(this, com.pokemonbattlearena.android.activity.SplashActivity.class);
        mWelcomeHelper.show(savedInstanceState);

        mPreferences = getSharedPreferences("Pokemon Battle Prefs", Context.MODE_PRIVATE);
        newestAddedPokemonTeamName = "";

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setDefaultTab(R.id.tab_battle);

        mFragmentManager = getFragmentManager();

        // Button listeners

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mApplication.setGoogleApiClient(googleApiClient);

        mMainMenuFragment = new MainMenuFragment();
        mSavedTeamsFragment = new SavedTeamsFragment();
        mTeamsHomeFragment = createTeamsHomeFragment();
        mBattleHomeFragment = new BattleHomeFragment();
        mChatHomeFragment = new ChatHomeFragment();
        mChatInGameFragment = new ChatInGameFragment();
        mFragmentManager.beginTransaction()
                .add(R.id.container, mMainMenuFragment, "main")
                .commit();

        // Listens for a tab touch (Only first touch of new tab)
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (displaySavedTeam(true)) {
                    setCurrentPokemonPlayerTeam(getSavedTeam());
                }
                switch (tabId) {
                    case R.id.tab_teams:
                        hideKeyboard();
                        if(mApplication.getApplicationPhase() == ApplicationPhase.INACTIVE_BATTLE) {
                            if (mTeamsHomeFragment != null && !mSavedTeamsFragment.isAdded()) {
                                displaySavedTeam(false);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.container, mSavedTeamsFragment, "team")
                                        .commit();
                            }
                            if (mChatHomeFragment != null && mChatHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mChatHomeFragment).commit();
                            }
                            if (mMainMenuFragment != null && mMainMenuFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mMainMenuFragment).commit();
                            }
                        } else if(mApplication.getApplicationPhase() == ApplicationPhase.ACTIVE_BATTLE) {

                        }
                        break;
                    case R.id.tab_battle:
                        hideKeyboard();
                        if(mApplication.getApplicationPhase() == ApplicationPhase.INACTIVE_BATTLE) {
                            if (mMainMenuFragment != null && !mMainMenuFragment.isAdded()) {
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.container, mMainMenuFragment, "main")
                                        .commit();
                            }
                            if (mBattleHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
                            }
                            if (mTeamsHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
                            }
                            if (mChatHomeFragment != null && mChatHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mChatHomeFragment).commit();
                            }
                        } else if(mApplication.getApplicationPhase() == ApplicationPhase.ACTIVE_BATTLE) {
                            if (mBattleHomeFragment != null && !mBattleHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.container, mBattleHomeFragment, "battle")
                                        .commit();
                                updateUI();
                            }
                            if (mMainMenuFragment != null && mMainMenuFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mMainMenuFragment).commit();
                            }
                            if (mTeamsHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
                            }
                            if (mChatInGameFragment != null && mChatInGameFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mChatInGameFragment).commit();
                            }
                        }
                        break;
                    case R.id.tab_chat:
                        if(mApplication.getApplicationPhase() == ApplicationPhase.INACTIVE_BATTLE) {
                            if (mChatHomeFragment != null && !mChatHomeFragment.isAdded()) {
                                displaySavedTeam(false);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.container, mChatHomeFragment, "chat")
                                        .commit();
                                showProgressDialog();
                            }
                            if (mTeamsHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
                            }
                            if (mMainMenuFragment != null && mMainMenuFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mMainMenuFragment).commit();
                            }
                        } else if(mApplication.getApplicationPhase() == ApplicationPhase.ACTIVE_BATTLE) {
                            if (mChatInGameFragment != null && !mChatInGameFragment.isAdded()) {
                                displaySavedTeam(false);
                                if(mRoomId.equals("AI_BATTLE")){
                                    mFragmentManager.beginTransaction()
                                            .replace(R.id.container, mChatHomeFragment, "chat")
                                            .commit();
                                } else {
                                    mFragmentManager.beginTransaction()
                                            .replace(R.id.container, mChatInGameFragment, "game_chat")
                                            .commit();
                                }
                                showProgressDialog();
                            }
                            if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
                                mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
                            }
                        }
                        break;
                    default:
                        break;

                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWelcomeHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        if (mApplication.getGoogleApiClient() != null && mApplication.getGoogleApiClient().isConnected()) {
            Log.w(TAG, "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG, "Connecting client.");
            mApplication.getGoogleApiClient().connect();
        }
        super.onStart();
    }
    //endregion

    //region Activity Result Callback
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (resultCode == RESULT_OK) {
                    mApplication.getGoogleApiClient().connect();
                } else {
                    // Bring up an error dialog to alert the user that sign-in
                    // failed. The R.string.signin_failure should reference an error
                    // string in your strings.xml file that tells the user they
                    // could not be signed in, such as "Unable to sign in."
                    //BaseGameUtils.showActivityResultError(this,
                    //        requestCode, resultCode, R.string.signin_other_error);
                    Log.e(TAG, "Error signing in " + requestCode);
                }
            case WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST:
                // The key of the welcome screen is in the Intent

                if (resultCode == RESULT_OK) {
                    // Code here will run if the welcome screen was completed
                    String name = mPreferences.getString("default_name", "default");
                    Toast.makeText(mApplication, name, Toast.LENGTH_SHORT).show();
                    displaySavedTeam(true);
                } else {
                    finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    //endregion

    /********************************************************************************************
     * ROOM UPDATES
     ********************************************************************************************/
    //region RoomUpdateListener Callbacks
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
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
            showGameError();
            return;
        }
        updateRoom(room);
        if (shouldStartGame(room)) {
            setCurrentPokemonPlayerTeam(getSavedTeam());
            Log.d(TAG, "HOST? : " + mIsHost);
            Log.d(TAG, "We are going to start!");
            String player = new Gson().toJson(mCurrentPokemonPlayer);
            sendMessage(player);
        }
    }
    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(this);
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
        if (mApplication.getGoogleApiClient().isConnected()) {
            Games.RealTimeMultiplayer.create(mApplication.getGoogleApiClient(), roomConfig);
        } else {
            showGameError();
            Log.e(TAG, "Not connected to Google Play Games");
        }

        // prevent screen from sleeping during handshake
        keepScreenOn();
    }
    //endregion

    /********************************************************************************************
     * ROOM STATUS
     *******************************************************************************************/
    //region RoomStatusUpdateListener Callbacks
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        leaveRoom();
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        Log.d(TAG, "Peer joined room");
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        leaveRoom();
    }

    @Override
    public void onConnectedToRoom(Room room) {
        updateRoom(room);
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        leaveRoom();
        showGameError();
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        Log.d(TAG, "Peer(S) connected");
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        leaveRoom();
    }

    @Override
    public String getHostId() {
        return mHostId;
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
            mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mApplication.getGoogleApiClient()));
            ArrayList<String> sortedIds = new ArrayList<>();
            setHost(sortedIds);
        }
    }

    // Leave the room.
    private void leaveRoom() {
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mApplication.getGoogleApiClient(), this, mRoomId);
            mRoomId = null;
            mActiveBattle = null;
            mMyId = null;
            mParticipants = null;
            mIsHost = false;
            mHostId = null;
            Log.d(TAG, "Left room everything is null.");
            mApplication.setApplicationPhase(ApplicationPhase.INACTIVE_BATTLE);
            refreshBattleFragment();
        }
    }
    //endregion

    /*******************************************************************************************
     * MESSAGING
     * *******************************************************************************************/
    //region RealTimeMessageListener Callbacks
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String bufferString = new String(buf);
        Log.d(TAG, "In Game Message Received: " + bufferString);
        // a host is the player who created the room. (They don't get special treatment)
        if (mActiveBattle == null) {
            // we don't have a battle, so we can assume a message is going to have a player
            try {
                mOpponentPokemonPlayer = new Gson().fromJson(bufferString, PokemonPlayer.class);
                Log.d(TAG, "Incoming Battle Message Received: " + mOpponentPokemonPlayer.getPokemonTeam().toString());
                mActiveBattle = Battle.createBattle(mCurrentPokemonPlayer, mOpponentPokemonPlayer);
                setupBattleUI(mCurrentPokemonPlayer, mOpponentPokemonPlayer);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            if (mIsHost) {
                Command command = mCommandGson.fromJson(bufferString, Command.class);
                Log.d(TAG, "We got a command from client of type: " + command.getClass());
                boolean phaseReady = mActiveBattle.getCurrentBattlePhase().queueCommand(command);
                if (phaseReady) {
                    handleBattleResult();
                }
            } else {
                BattlePhaseResult resultFromJson = mCommandResultGson.fromJson(bufferString, BattlePhaseResult.class);
                Log.d(TAG, "We got a battle phase result: " + resultFromJson.toString());
                for (CommandResult commandResult : resultFromJson.getCommandResults()) {
                    // Update the internal state of the battle (only host really needs to do this, but opponent can too)
                    // Have opponent update their own battle state if you want to use the Battle object directly to update the UI (which makes more sense, IMO)
                    mActiveBattle.applyCommandResult(commandResult);
                    Log.d(TAG, commandResult.getTargetInfo().toString());
                    //TODO: check if battle ended
                    updateUI();
                }

                mBattleHomeFragment.enableButtonActions(true);
                if (mActiveBattle.selfPokemonFainted()) {
                    Button force;
                    force = (Button)findViewById(R.id.switch_button);
                    force.performClick();
                }
                updateUI();
            }
        }
        hideProgressDialog();
    }

    //region Networking Helper methods
    private void queueHostMessage(Command c) {
        boolean movesReady = mActiveBattle.getCurrentBattlePhase().queueCommand(c);
        mBattleHomeFragment.enableButtonActions(movesReady);
        if (movesReady) {
            handleBattleResult();
        }
    }

    private void sendClientMessage(Command c) {
        String gson = mCommandGson.toJson(c, Command.class);
        sendMessage(gson);
        mBattleHomeFragment.enableButtonActions(false);
    }

    private void sendMessage(String message) {
        byte[] byteMessage = message.getBytes();
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
                Games.RealTimeMultiplayer.sendReliableMessage(mApplication.getGoogleApiClient(), null, byteMessage,
                        mRoomId, p.getParticipantId());
                Log.d(TAG, "Reliable message sent (sendMessage()) + " + message);
            }
        }
    }
    //endregion

    @Override
    public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientId) {

        Log.d(TAG, "Realtime message sent to " + recipientId + " (onRealTimeMessageSent callback)");

        switch (statusCode) {
            case STATUS_OK:
                Log.d(TAG, "Message sent successfully");
                break;
            case STATUS_REAL_TIME_MESSAGE_SEND_FAILED:
                Log.d(TAG, "Message failed to send");
                break;
            case STATUS_REAL_TIME_ROOM_NOT_JOINED:
                Log.d(TAG, "Message failed to send because recipient is not in the room");
                break;
            default:
                break;
        }
    }
    //endregion

    /***********************************************************************************************
     * BATTLE
     **********************************************************************************************/
    //region Battle Helper Methods
    //region AI Helper Methods
    public void AIBattleTurn(Command cmd) {
        if (mActiveBattle instanceof AiBattle) {
            mBattleHomeFragment.enableButtonActions(false);
            updateUI();
            mActiveBattle.getCurrentBattlePhase().queueCommand(cmd);
            ((AiBattle) mActiveBattle).buildIntelligence();
            Command aiCommand = ((AiBattle) mActiveBattle).showIntelligence();

            if (mActiveBattle.oppPokemonFainted() && ind < 5) {
                ind++;
                BattlePokemon cur = mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon();
                Switch aiSw = new Switch(mActiveBattle.getOpponent(), ind);
                mActiveBattle.getCurrentBattlePhase().queueCommand(aiSw);
            } else {
                mActiveBattle.getCurrentBattlePhase().queueCommand(aiCommand);
            }

            handleBattleResult();
        }
    }

    private void handleBattleResult() {
        BattlePhaseResult result = mActiveBattle.executeCurrentBattlePhase();
        PokemonTeam pokes = new PokemonTeam(6);
        for (BattlePokemon bp : mActiveBattle.getSelf().getBattlePokemonTeam().getBattlePokemons()) {
            pokes.addPokemon(bp.getOriginalPokemon());
        }
        setCurrentPokemonPlayerTeam(pokes);
        displaySavedTeam(true);

        for (CommandResult commandResult : result.getCommandResults()) {
            updateUI();
            Log.d(TAG, commandResult.getTargetInfo().toString());

            if (mActiveBattle.isFinished()) {
                battleEndListener.onBattleEnd();
                if (!isAiBattle) {
                    //TODO: don't leave the room since
                    leaveRoom();
                }
                return;
            }
        }
        mBattleHomeFragment.enableButtonActions(true);
        mBattleHomeFragment.refreshActivePokemon(mActiveBattle);
        updateUI();

        if(!isAiBattle) {
            String json = mCommandResultGson.toJson(result);
            sendMessage(json);
        }

        mActiveBattle.startNewBattlePhase();

        if (mActiveBattle.selfPokemonFainted()) {
            Button force;
            force = (Button)findViewById(R.id.switch_button);
            force.performClick();
        }
    }
    //endregion
    private void startAiBattle() {
        mRoomId="AI_BATTLE"; //mRoomId needs to be set or you can't exit the battle in leaveRoom() method
        mApplication.setApplicationPhase(ApplicationPhase.ACTIVE_BATTLE);
        refreshBattleFragment();
        mMyId = "Player";
        setCurrentPokemonPlayerTeam(getSavedTeam());
        mOpponentPokemonPlayer = new AiPlayer(mApplication.getBattleDatabase(), mCurrentPokemonPlayer);
        mActiveBattle = new AiBattle(mCurrentPokemonPlayer, (AiPlayer)mOpponentPokemonPlayer);
        setupBattleUI(mCurrentPokemonPlayer, mOpponentPokemonPlayer);
    }

    private boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    private void setCurrentPokemonPlayerTeam(PokemonTeam team) {
        mCurrentPokemonPlayer = new PokemonPlayer(mMyId);
        mCurrentPokemonPlayer.setPokemonTeam(team);
        String json = new Gson().toJson(team, PokemonTeam.class);
        setCurrentTeam(json);
        if (mBattleHomeFragment != null) {
            mBattleHomeFragment.setPlayerTeam(team);
        }
    }

    private PokemonTeam getSavedTeam() {
        String teamJSON = mPreferences.getString("pokemon_team", "mew");
        if (!teamJSON.equals("mew")) {
            Log.d(TAG, "Got saved team: " + teamJSON);
            return new Gson().fromJson(teamJSON, PokemonTeam.class);
        }
        return null;
    }
    //endregion

    /*********************************************************************************************
     * USER INTERFACE
     **********************************************************************************************/
    //region user interface
    private void setupBattleUI(PokemonPlayer player, PokemonPlayer opponent) {
        if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
            //TODO: use updateUI to do this
            mBattleHomeFragment.setPlayer(mCurrentPokemonPlayer);
            mBattleHomeFragment.setOpponent(mOpponentPokemonPlayer);
            mBattleHomeFragment.setBattleVisible(true);
        }
    }

    private void updateUI() {
        if (mBattleHomeFragment != null) {
            int health1 = mActiveBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();
            int health2 = mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();

            String name = mActiveBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon().getName();
            int currentHp = mActiveBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();
            Log.d(TAG, name + ": " + currentHp);

            name = mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon().getName();
            currentHp = mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();

            Log.d(TAG, name + ": " + currentHp);

            mBattleHomeFragment.updateHealthBars(health1, health2);
            mBattleHomeFragment.refreshActivePokemon(mActiveBattle);
        }
    }

    private boolean displaySavedTeam(boolean show) {
        String teamJSON = mPreferences.getString("pokemon_team", "mew");
        View savedView = findViewById(R.id.saved_team_layout);
        TextView savedText = (TextView) findViewById(R.id.saved_team_textview);
        String builder = mPreferences.getString(NameFragment.profile_name_key, "User") +
                getString(R.string.append_profile_text);
        savedText.setText(builder);
        if (!teamJSON.equals("mew") && show) {
            savedView.setVisibility(View.VISIBLE);
            PokemonTeam pokemonTeam = new Gson().fromJson(teamJSON, PokemonTeam.class);
            setDrawablesForSavedTeam(savedView, pokemonTeam);
            return true;
        } else {
            savedView.setVisibility(View.GONE);
        }
        return false;
    }

    private void setDrawablesForSavedTeam(View savedView, PokemonTeam pokemonTeam) {
        int index = 0;
        for (Pokemon pokemon : pokemonTeam.getPokemons()) {
            String key = "saved_team_" + index++;
            int id = getResources().getIdentifier(key, "id", getPackageName());
            ImageView savedImage = (ImageView) savedView.findViewById(id);
            savedImage.setImageDrawable(getDrawableForPokemon(this, pokemon.getName()));
        }
    }

    private void addSavedTeam(String teamName, String pokemonJSON) {
        DatabaseReference root = SavedTeamsFragment.root;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(teamName, pokemonJSON);
        root.updateChildren(map);
    }

    public void deleteSavedTeam(String teamName) {
        DatabaseReference root = SavedTeamsFragment.root;
        root.child(teamName).removeValue();
    }

    public void updateTeamOrder(){
        ArrayList<Pair<Long, PokemonTeam>> savedTeams;
        ArrayList<String> teamOrder = new ArrayList<String>();
        //put team order from drag listview into new Arraylist
        savedTeams = mSavedTeamsFragment.getTeamOrder();
        //populate temp ArrayList with all team names
        for(Pair<Long, PokemonTeam> team : savedTeams){
            teamOrder.add(team.second.getTeamName());
        }

        //add temp Arraylist to sharedpreferences for team order
        SharedPreferences.Editor editor = mPreferences.edit();
        String orderedTeamJSON = new Gson().toJson(teamOrder);
        Log.d(TAG, "Setting saved team order: " + orderedTeamJSON);
        editor.putString("orderedTeamJSON", orderedTeamJSON).apply();
        editor.commit();

        //add first team as your active team
        if(savedTeams.size() != 0) {
            String pokemonJSON = new Gson().toJson(savedTeams.get(0).second);
            setCurrentTeam(pokemonJSON);
        }
    }

    public ArrayList<String> retrieveTeamOrder(){
        String orderedTeamJSON = mPreferences.getString("orderedTeamJSON", "mew");
        if (!orderedTeamJSON.equals("mew")) {
            Log.d(TAG, "Got team order: " + orderedTeamJSON);
            return new Gson().fromJson(orderedTeamJSON, ArrayList.class);
        }
        return new ArrayList<String>();
    }

    private void setCurrentTeam(String pokemonJSON){
        SharedPreferences.Editor editor = mPreferences.edit();
        Log.d(TAG, "Setting current team: " + pokemonJSON);
        editor.putString("pokemon_team", pokemonJSON).apply();
        editor.commit();
    }

    private void refreshBattleFragment() {
        if(mApplication.getApplicationPhase() == ApplicationPhase.ACTIVE_BATTLE) {
            if (mFragmentManager != null && mMainMenuFragment.isAdded()) {
                mFragmentManager.beginTransaction().remove(mMainMenuFragment).commit();
                mBattleHomeFragment = new BattleHomeFragment();
                mFragmentManager.beginTransaction().add(R.id.container, mBattleHomeFragment, "battle").commit();
                mFragmentManager.executePendingTransactions();
            }
        } else if(mApplication.getApplicationPhase() == ApplicationPhase.INACTIVE_BATTLE) {
            //deletes in-game chat from Firebase
            mChatInGameFragment.deleteChatRoom();
            if (mFragmentManager != null && mBattleHomeFragment.isAdded()) {
                mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
                mFragmentManager.beginTransaction().add(R.id.container, mMainMenuFragment, "main").commit();
                mFragmentManager.executePendingTransactions();
            }
            if (mFragmentManager != null && mChatInGameFragment.isAdded()) {
                mFragmentManager.beginTransaction().remove(mChatInGameFragment).commit();
                mBottomBar.getTabWithId(R.id.tab_battle).performClick();
            }
            //TODO: Add In-Game Team UI Handling here

        }
    }

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = c.getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Show error message about game being cancelled and return to main screen.
    private void showGameError() {
        //BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
    }
    //endregion

    //region Battle End Listener
    //TODO pass in some useful parameters for different tasks
    public void onBattleEnd() {
        //TODO push move history to Firebase, update records, give client BattlePhase list, etc
        mApplication.setApplicationPhase(ApplicationPhase.INACTIVE_BATTLE);

        if (!mMainMenuFragment.isAdded()) {
            refreshBattleFragment();
        }

        if (isAiBattle) {
            ind = 0;
            if (mActiveBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication," AI has won the battle", Toast.LENGTH_LONG).show();
            } else if (mActiveBattle.oppPokemonFainted()) {
                Toast.makeText(mApplication," You have won the battle", Toast.LENGTH_LONG).show();

            }
            return;
        }

        if (mActiveBattle.isFinished()) {
       //     for (int i = 0;i < 6; i ++) {
       //         BattlePokemon mBP = mActiveBattle.getSelf().getBattlePokemonTeam().getBattlePokemons().get(i);
       //         BattlePokemon oBP = mActiveBattle.getOpponent().getBattlePokemonTeam().getBattlePokemons().get(i);
                if (mActiveBattle.selfPokemonFainted()) {
                    Toast.makeText(mApplication, "A player" + " has won the battle", Toast.LENGTH_LONG).show();
                    return;
                } else if (mActiveBattle.oppPokemonFainted()){
                    Toast.makeText(mApplication, "A player" + " has won the battle", Toast.LENGTH_LONG).show();
                    return;
                }
        //    }
        }
    }
    //endregion

    /********************************************************************************************
     * FRAGMENTS
     *******************************************************************************************/

    private TeamsHomeFragment createTeamsHomeFragment() {
        TeamsHomeFragment teamsHomeFragment = new TeamsHomeFragment();
        // Set the team size
        Bundle teamArgs = new Bundle();
        teamArgs.putInt("teamSize", TEAM_SIZE_INT);
        teamsHomeFragment.setArguments(teamArgs);
        return teamsHomeFragment;
    }

    //region Battle Fragment callbacks
    @Override
    public void onCancelBattle() {
        leaveRoom();
    }

    @Override
    public void onSwitchPokemon(int position) {
        Switch s = new Switch(mActiveBattle.getSelf(), position);
        if (!isAiBattle) {
            if (mIsHost) {
                queueHostMessage(s);
            } else {
                sendClientMessage(s);
            }
        } else {
            AIBattleTurn(s);
        }
    }

    @Override
    public void onMoveClicked(Move move) {
        if (!isAiBattle) {
            if (mBattleHomeFragment != null) {
                mBattleHomeFragment.appendMoveHistory(mCurrentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                Attack attack = new Attack(mActiveBattle.getSelf(), mActiveBattle.getOpponent(), move);
                if(mIsHost) {
                    Log.d(TAG, "Host: queuing move: " + move.getName());
                    queueHostMessage(attack);
                } else {
                    Log.d(TAG, "Client: sending move: " + move.getName());
                    sendClientMessage(attack);
                }
            }
        } else {
            if (mBattleHomeFragment != null) {
                mBattleHomeFragment.appendMoveHistory(mCurrentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                Attack attack = new Attack(mActiveBattle.getSelf(), mActiveBattle.getOpponent(), move);
                AIBattleTurn(attack);
            }
        }
    }
    //endregion Battle Fragment callbacks

    //region Main Menu Fragment callbacks
    @Override
    public void onBattleNowClicked() {
        // showing the progress dialog also creates it if its null
        mApplication.setApplicationPhase(ApplicationPhase.ACTIVE_BATTLE);
        refreshBattleFragment();
        startMatchMaking();
        showProgressDialog();
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mBattleHomeFragment.isVisible() ) {
                    Toast.makeText(mApplication, "Canceled search", Toast.LENGTH_SHORT).show();
                    leaveRoom();
                }
            }
        });
    }

    @Override
    public void onAiBattleClicked() {
        isAiBattle = true;
        startAiBattle();
    }
    //endregion

    //region Team Fragment callbacks
    public void onTeamSelected(String pokemonJSON) {
        Log.d(TAG, "Selected: " + pokemonJSON);
        if (mFragmentManager != null) {
            PokemonTeam team = new Gson().fromJson(pokemonJSON, PokemonTeam.class);
            //add saved Team to Firebase
            newestAddedPokemonTeamName = team.getTeamName();
            addSavedTeam(newestAddedPokemonTeamName, pokemonJSON);

            setCurrentPokemonPlayerTeam(team);
            toggleAddTeamFragment();
        }

    }

    public String getNewestPokemonTeamName() {
        return newestAddedPokemonTeamName;
    }

    @Override
    public void toggleAddTeamFragment() {
        if (mFragmentManager != null && mSavedTeamsFragment.isAdded()) {
            mFragmentManager.beginTransaction().remove(mSavedTeamsFragment).commit();
            mFragmentManager.beginTransaction().add(R.id.container, mTeamsHomeFragment, "team").commit();
            mFragmentManager.executePendingTransactions();
        } else if (mFragmentManager != null && mTeamsHomeFragment.isAdded()) {
            mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
            mFragmentManager.beginTransaction().add(R.id.container, mSavedTeamsFragment, "team").commit();
            mFragmentManager.executePendingTransactions();
        }
    }
    //endregion

    //region Chat Fragment callbacks
    @Override
    public void onChatLoaded() {
        hideProgressDialog();
    }
    //endregion

    /********************************************************************************************
     * GOOGLE API CLIENT
     *******************************************************************************************/
    //region Google API Client
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (bundle != null) {

            Invitation inv = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            // check to see if we have an invite
            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(mApplication.getGoogleApiClient(), roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                keepScreenOn();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mApplication.getGoogleApiClient().connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }
        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.

             if (!BaseGameUtils.resolveConnectionFailure(this,
                    mApplication.getGoogleApiClient(), connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
    }
    //endregion
}
