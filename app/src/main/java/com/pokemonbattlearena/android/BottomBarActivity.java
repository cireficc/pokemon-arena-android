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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.pokemonbattlearena.android.engine.ai.AiBattle;
import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.Attack;
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final int TEAM_SIZE_INT = 6;
    private static final int MIN_PLAYERS = 2;
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static String TAG = BottomBarActivity.class.getSimpleName();

    // GOOGLE PLAY GAMES FIELDS
    private static final int RC_SIGN_IN = 9001;
    private String mRoomId = null;
    private String mMyId = null;
    private ArrayList<Participant> mParticipants = null;
    private String mHostId = null;
    private boolean mIsHost = false;

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

    private BottomBar mBottomBar;
    private SharedPreferences mPreferences;

    private PokemonPlayer mCurrentPokemonPlayer;
    private PokemonPlayer mOpponentPokemonPlayer;

    private Battle mActiveBattle = null;

    private int mBattleMatchFlag = 0;

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
    private boolean isAiBattle = false;

    // BATTLE END
    private BattleEndListener battleEndListener = this;

    //region Fragment callbacks
    public void onTeamSelected(String pokemonJSON) {
        Log.d(TAG, "Selected: " + pokemonJSON);
        if (mFragmentManager != null) {
            setSavedTeam(pokemonJSON);
            setCurrentPokemonPlayerTeam(new Gson().fromJson(pokemonJSON, PokemonTeam.class));
            toggleAddTeamFragment();
        }

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

    @Override
    public void onCancelBattle() {
        leaveRoom();
    }

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
    public void onSwitchPokemon(int position) {
        Switch s = new Switch(mActiveBattle.getSelf(), position);
        if (mIsHost) {
            sendHostMessage(s);
        } else {
            sendClientMessage(s);
        }
    }

    private void sendHostMessage(Command c) {
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


    @Override
    public void onAiBattleClicked() {
        isAiBattle = true;
        startAiBattle();
    }

    @Override
    //TODO Break the AI stuff out into methods i.e. use handleBattleResult
    public void onMoveClicked(Move move) {
        if (!isAiBattle) {
            if (mBattleHomeFragment != null) {
                mBattleHomeFragment.appendMoveHistory(mCurrentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                Attack attack = new Attack(mActiveBattle.getSelf(), mActiveBattle.getOpponent(), move);
                if(mIsHost) {
                    Log.d(TAG, "Host: queuing move: " + move.getName());
                    sendHostMessage(attack);
                } else {
                    Log.d(TAG, "Client: sending move: " + move.getName());
                    sendClientMessage(attack);
                }
            }
        } else {
            if (mBattleHomeFragment != null) {
                mBattleHomeFragment.appendMoveHistory(mCurrentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                if (mActiveBattle.getCurrentBattlePhase() == null) {
                    mActiveBattle.startNewBattlePhase();
                }
                if (mActiveBattle instanceof AiBattle) {
                    Move tmp = ((AiBattle) mActiveBattle).showIntelligence();
                    mBattleHomeFragment.appendMoveHistory("AI", tmp);
                    Attack attack = new Attack(mActiveBattle.getSelf(), mActiveBattle.getOpponent(), move);
                    boolean movesReady = mActiveBattle.getCurrentBattlePhase().queueCommand(attack);
                    //mBattleHomeFragment.showMoveUI(movesReady);
                    Attack aiAttack = new Attack(mActiveBattle.getOpponent(), mActiveBattle.getSelf(), tmp);
                    mActiveBattle.getCurrentBattlePhase().queueCommand(aiAttack);
                    mActiveBattle.executeCurrentBattlePhase();

                    for (CommandResult cmdR: mActiveBattle.getCurrentBattlePhase().getBattlePhaseResult().getCommandResults())
                    {
                        if (mActiveBattle.selfPokemonFainted() || mActiveBattle.oppPokemonFainted()) {
                            if(mActiveBattle.isFinished()) {
                                battleEndListener.onBattleEnd();
                                break;
                            }
                            //TODO somehow handle the knocking out of a Pokemon mid-Phase
                            break;
                        }
                        mActiveBattle.applyCommandResult(cmdR);
                        updateUI();
                    }
                    updateUI();
                    if(!mActiveBattle.isFinished()) {
                        mActiveBattle.startNewBattlePhase();
                    }
                    else {
                        battleEndListener.onBattleEnd();
                    }
                }
            }
        }
    }

    @Override
    public void onChatLoaded() {
        hideProgressDialog();
    }

    //endregion

    //region Activity hooks
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottombar);

        mPreferences = getPreferences(Context.MODE_PRIVATE);

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
                    BaseGameUtils.showActivityResultError(this,
                            requestCode, resultCode, R.string.signin_other_error);
                    Log.e(TAG, "Error signing in " + requestCode);
                }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    //endregion

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
    //endregion

    //region RoomStatusUpdateListener Callbacks
    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
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
    //endregion

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
                mActiveBattle = new Battle(mCurrentPokemonPlayer, mOpponentPokemonPlayer);
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
                    updateUI();
                }

                mBattleHomeFragment.enableButtonActions(true);
                mBattleHomeFragment.refreshActivePokemon(mActiveBattle);
            }
        }
        hideProgressDialog();
    }

    private void handleBattleResult() {
        BattlePhaseResult result = mActiveBattle.executeCurrentBattlePhase();

        for (CommandResult commandResult : result.getCommandResults()) {
            mActiveBattle.applyCommandResult(commandResult);
            updateUI();
            Log.d(TAG, commandResult.getTargetInfo().toString());

            if (mActiveBattle.isFinished()) {
                battleEndListener.onBattleEnd();
                leaveRoom();
                return;
            }
        }
        mBattleHomeFragment.enableButtonActions(true);
        mBattleHomeFragment.refreshActivePokemon(mActiveBattle);
        String json = mCommandResultGson.toJson(result);
        sendMessage(json);

        mActiveBattle.startNewBattlePhase();
    }

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

    //region AI Battle
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
    //endregion

    //region Private Helper Methods
    private void setCurrentPokemonPlayerTeam(PokemonTeam team) {
        mCurrentPokemonPlayer = new PokemonPlayer(mMyId);
        mCurrentPokemonPlayer.setPokemonTeam(team);
    }

    // Show error message about game being cancelled and return to main screen.
    private void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
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

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(this);
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

    private void setHost(ArrayList<String> sortedIds) {
        for (Participant mParticipant : mParticipants) {
            sortedIds.add(mParticipant.getParticipantId());
        }
        Collections.sort(sortedIds);
        mHostId = sortedIds.get(0);
        mIsHost = mHostId.equalsIgnoreCase(mMyId);
    }

    private void setupBattleUI(PokemonPlayer player, PokemonPlayer opponent) {
        if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
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
//            mBattleHomeFragment.refreshActivePokemon(mActiveBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon(), mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon());
        }
    }

    private boolean displaySavedTeam(boolean show) {
        String teamJSON = mPreferences.getString("pokemonTeamJSON", "mew");
        View savedView = (View) findViewById(R.id.saved_team_layout);

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

    private void setSavedTeam(String pokemonJSON) {
        SharedPreferences.Editor editor = mPreferences.edit();
        Log.d(TAG, "Setting team: " + pokemonJSON);
        editor.putString("pokemonTeamJSON", pokemonJSON).apply();
        editor.commit();
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

    private PokemonTeam getSavedTeam() {
        String teamJSON = mPreferences.getString("pokemonTeamJSON", "mew");
        if (!teamJSON.equals("mew")) {
            Log.d(TAG, "Got saved team: " + teamJSON);
            return new Gson().fromJson(teamJSON, PokemonTeam.class);
        }
        return null;
    }

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = c.getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    private TeamsHomeFragment createTeamsHomeFragment() {
        TeamsHomeFragment teamsHomeFragment = new TeamsHomeFragment();
        // Set the team size
        Bundle teamArgs = new Bundle();
        teamArgs.putInt("teamSize", TEAM_SIZE_INT);
        teamsHomeFragment.setArguments(teamArgs);
        return teamsHomeFragment;
    }

    private boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    //endregion

    @Override
    public String getHostId() {
        return mHostId;
    }

    //region Battle End Listener
    //TODO pass in some useful parameters for different tasks
    public void onBattleEnd() {
        //TODO push move history to Firebase, update records, give client BattlePhase list, etc
        mApplication.setApplicationPhase(ApplicationPhase.INACTIVE_BATTLE);

        if (!mMainMenuFragment.isAdded()) {
            refreshBattleFragment();
        }

        if (isAiBattle) {
            if (mActiveBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication," AI has won the battle", Toast.LENGTH_LONG).show();
            } else if (mActiveBattle.oppPokemonFainted()) {
                Toast.makeText(mApplication," You have won the battle", Toast.LENGTH_LONG).show();

            }
            return;
        }
        //TODO Change this to reflect entire Pokemon team instead of just the first Pokemon
        if (mActiveBattle.selfPokemonFainted()) {
            for (Participant p: mParticipants) {
                if (p.getParticipantId().equals(mOpponentPokemonPlayer.getPlayerId())) {
                    Toast.makeText(mApplication, p.getDisplayName() + " has won the battle", Toast.LENGTH_LONG).show();
                }
            }

        } else if (mActiveBattle.oppPokemonFainted()){
            for (Participant p: mParticipants) {
                if (p.getParticipantId().equals(mCurrentPokemonPlayer.getPlayerId())) {
                    Toast.makeText(mApplication, p.getDisplayName() + " has won the battle", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    //endregion
}
