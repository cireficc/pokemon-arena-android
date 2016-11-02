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
import com.pokemonbattlearena.android.engine.match.AttackResult;
import com.pokemonbattlearena.android.engine.match.Battle;
import com.pokemonbattlearena.android.engine.match.BattlePhaseResult;
import com.pokemonbattlearena.android.engine.match.CommandResult;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.PokemonPlayer;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragments.battle.BattleHomeFragment;
import com.pokemonbattlearena.android.fragments.chat.ChatHomeFragment;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment.OnPokemonTeamSelectedListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
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
        ChatHomeFragment.OnChatLoadedListener {

    private static final int TEAM_SIZE_INT = 6;
    private static final int MIN_PLAYERS = 2;
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static String TAG = BottomBarActivity.class.getSimpleName();

    // GOOGLE PLAY GAMES FIELDS
    private static final int RC_SIGN_IN = 9001;
    private String mRoomCreatorId = null;
    private String mRoomId = null;
    private String mMyId = null;
    private ArrayList<Participant> mParticipants = null;

    // GOOGLE PLAY SIGN IN FIELDS
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    private FragmentManager mFragmentManager;
    private BattleHomeFragment mBattleHomeFragment;
    private TeamsHomeFragment mTeamsHomeFragment;
    private ChatHomeFragment mChatHomeFragment;

    private BottomBar mBottomBar;
    private SharedPreferences mPreferences;

    private PokemonPlayer mCurrentPokemonPlayer;

    private PokemonPlayer mOpponentPokemonPlayer;

    private Battle mActiveBattle = null;

    private int mBattleMatchFlag = 0;

    private final RuntimeTypeAdapterFactory<CommandResult> mRuntimeTypeAdapterFactory = RuntimeTypeAdapterFactory
            .of(CommandResult.class, "type")
            .registerSubtype(AttackResult.class);

    private final Gson mCommandGson = new GsonBuilder().registerTypeAdapterFactory(mRuntimeTypeAdapterFactory).create();
    private boolean isAiBattle = false;

    //region Fragment callbacks
    public void onTeamSelected(String pokemonJSON) {
        Log.d(TAG, "Selected: " + pokemonJSON);
        if (mFragmentManager != null) {
            mBottomBar.selectTabWithId(R.id.tab_battle);
            setSavedTeam(pokemonJSON);
            displaySavedTeam(true);
            setCurrentPokemonPlayer(new Gson().fromJson(pokemonJSON, PokemonTeam.class));
        }
    }

    @Override
    public void onBattleNowClicked(boolean isActiveBattle) {
        // showing the progress dialog also creates it if its null
        if (!isActiveBattle) {
            startMatchMaking();
            showProgressDialog();
            mBattleHomeFragment.hideBans(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mBattleHomeFragment.isVisible() ) {
                        Toast.makeText(mApplication, "Canceled search", Toast.LENGTH_SHORT).show();
                        leaveRoom();
                    }
                }
            });
        } else {
            leaveRoom();
        }
    }

    @Override
    public void onAiBattleClicked() {
        isAiBattle = true;
        startAiBattle();

    }

    @Override
    public void onMoveClicked(Move move) {

        if (!isAiBattle) {
            if (mBattleHomeFragment != null) {
                mBattleHomeFragment.appendMoveHistory(mCurrentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                if (mActiveBattle.getCurrentBattlePhase() == null) {
                    mActiveBattle.startNewBattlePhase();
                }
                boolean movesReady = mActiveBattle.getCurrentBattlePhase().queueAction(mActiveBattle.getSelf(), mActiveBattle.getOpponent(), move);
//            mBattleHomeFragment.showMoveUI(movesReady);
            }
            String gson = new Gson().toJson(move, Move.class);
            sendMessage(gson);
        } else {
            Toast.makeText(mApplication, move.getName(), Toast.LENGTH_SHORT).show();
            if (mActiveBattle instanceof AiBattle) {
                Toast.makeText(mApplication, mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getOriginalPokemon().getName() + " used: " + ((AiBattle) mActiveBattle).showIntelligence(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onTypeBanClicked(String type) {
        mBattleMatchFlag |= CustomTypeMatch.valueOf(type.toUpperCase()).getFlag();
    }

    @Override
    public void onTypeBanLongClicked(String type) {
        mBattleMatchFlag ^= CustomTypeMatch.valueOf(type.toUpperCase()).getFlag();
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

        mTeamsHomeFragment = createTeamsHomeFragment();
        mBattleHomeFragment = new BattleHomeFragment();
        mChatHomeFragment = new ChatHomeFragment();
        mFragmentManager.beginTransaction()
                .add(R.id.container, mBattleHomeFragment, "battle")
                .commit();

        // Listens for a tab touch (Only first touch of new tab)
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (displaySavedTeam(true)) {
                    setCurrentPokemonPlayer(getSavedTeam());
                }
                switch (tabId) {
                    case R.id.tab_teams:
                        if (mTeamsHomeFragment != null && !mTeamsHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction()
                                    .replace(R.id.container, mTeamsHomeFragment, "team")
                                    .commit();
                        }
                        if (mChatHomeFragment != null && mChatHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mChatHomeFragment).commit();
                        }
                        if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
                        }
                        break;
                    case R.id.tab_battle:
                        if (mBattleHomeFragment != null && !mBattleHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction()
                                    .replace(R.id.container, mBattleHomeFragment, "battle")
                                    .commit();
                        }
                        if (mTeamsHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
                        }
                        if (mChatHomeFragment != null && mChatHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mChatHomeFragment).commit();
                        }
                        break;
                    case R.id.tab_chat:
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
                        if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
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
            Log.d(TAG,"Connecting client.");
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
        Log.e(TAG, "Result of activity");
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

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();
        mRoomCreatorId = room.getCreatorId();
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
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
        if (shouldStartGame(room)) {
            Log.d(TAG, "We are going to start!");
            PokemonTeam team = getSavedTeam();
            PokemonPlayer currentPlayer = new PokemonPlayer();
            currentPlayer.setPokemonTeam(team);
            String player = new Gson().toJson(currentPlayer);
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
        updateRoom(room);
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
        updateRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mApplication.getGoogleApiClient()));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mRoomId==null)
            mRoomId = room.getRoomId();
        if(mRoomCreatorId==null)
            mRoomCreatorId = room.getCreatorId();
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
        updateRoom(room);
    }
    //endregion

    //region RealTimeMessageListener Callbacks
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String bufferString = new String(buf);
        Log.d(TAG, "Incoming");
        // a host is the player who created the room. (They don't get special treatment)
        boolean isHost = mRoomCreatorId.equalsIgnoreCase(mMyId);
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
            // since we have a battle, we can assume a message will update the game
            if (!isHost) {
                // the host will receive moves from an opponent
                Move move = new Gson().fromJson(bufferString, Move.class);
                Log.d(TAG, "In Game Message Received: " + move.getName());
                if (mBattleHomeFragment != null) {
                    mBattleHomeFragment.appendMoveHistory(mOpponentPokemonPlayer.getPokemonTeam().getPokemons().get(0).getName(), move);
                    boolean movesReady = mActiveBattle.getCurrentBattlePhase().queueAction(mActiveBattle.getOpponent(), mActiveBattle.getSelf(), move);

//                    mBattleHomeFragment.showMoveUI(movesReady);
                    //TODO: This will break. Something is broken in DamageCalculator.
                    BattlePhaseResult result = mActiveBattle.executeCurrentBattlePhase();
                    for (CommandResult commandResult : result.getCommandResults()) {
                        mActiveBattle.applyCommandResult(commandResult);
                    }
//                    updateUI();
                    String json = mCommandGson.toJson(result);
                    sendMessage(json);
                    // Send JSON bytes to opponent
                }

            } else {
                // we need to receive a BattleResult since we are the client
                BattlePhaseResult resultFromJson = mCommandGson.fromJson(bufferString, BattlePhaseResult.class);
                for (CommandResult commandResult : resultFromJson.getCommandResults()) {
                    // Update the internal state of the battle (only host really needs to do this, but opponent can too)
                    // Have opponent update their own battle state if you want to use the Battle object directly to update the UI (which makes more sense, IMO)

                    // Update UI here using data in the CommandResult (AttackResult) object or using the Battle object
                    // E.g. damageDone, healingDone, etc.
                }
                    mActiveBattle.startNewBattlePhase();
            }
        }

        hideProgressDialog();
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
        setCurrentPokemonPlayer(getSavedTeam());
        AiPlayer ai = new AiPlayer(mApplication.getBattleDatabase(), mCurrentPokemonPlayer);
        mActiveBattle = new AiBattle(mCurrentPokemonPlayer, ai);
        setupBattleUI(mCurrentPokemonPlayer, ai);
    }
    //endregion

    //region Private Helper Methods
    private void setCurrentPokemonPlayer(PokemonTeam team) {
        mCurrentPokemonPlayer = new PokemonPlayer();
        mCurrentPokemonPlayer.setPokemonTeam(team);
    }
    
    // Show error message about game being cancelled and return to main screen.
    private void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
    }

    private void sendMessage(String message) {
        Log.d(TAG, "Sending Message" + message);

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
            mRoomCreatorId = room.getCreatorId();
        }
        if (mParticipants != null) {
            // update game states
        }
    }

    private void setupBattleUI(PokemonPlayer player, PokemonPlayer opponent) {
        if (mBattleHomeFragment != null && mBattleHomeFragment.isAdded()) {
            mBattleHomeFragment.setPlayer(player);
            mBattleHomeFragment.setOpponent(opponent);
            mBattleHomeFragment.setBattleVisible(true);
        }
    }

    private void updateUI() {
        if (mBattleHomeFragment != null) {
            int health1 = mActiveBattle.getSelf().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();
            int health2 = mActiveBattle.getOpponent().getBattlePokemonTeam().getCurrentPokemon().getCurrentHp();
            mBattleHomeFragment.updateHealthBars(health1, health2);
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
        Log.e(TAG, "Setting team: " + pokemonJSON);
        editor.putString("pokemonTeamJSON", pokemonJSON).apply();
        editor.commit();
    }

    // Leave the room.
    private void leaveRoom() {
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Log.d(TAG, "Leaving room.");
            Games.RealTimeMultiplayer.leave(mApplication.getGoogleApiClient(), this, mRoomId);
            mActiveBattle = null;
            mRoomId = null;
            mRoomCreatorId = null;
        }
        refreshBattleFragment();
    }

    private void refreshBattleFragment() {
        if (mFragmentManager != null && mBattleHomeFragment.isAdded()) {
            mFragmentManager.beginTransaction().remove(mBattleHomeFragment).commit();
            mBattleHomeFragment = new BattleHomeFragment();
            mFragmentManager.beginTransaction().add(R.id.container, mBattleHomeFragment, "battle").commit();
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
            Log.e(TAG, "Got saved team: " + teamJSON);
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
}
