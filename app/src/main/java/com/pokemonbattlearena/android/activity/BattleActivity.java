package com.pokemonbattlearena.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.pokemonbattlearena.android.fragment.battle.BattleEndListener;
import com.pokemonbattlearena.android.application.PokemonBattleApplication;
import com.pokemonbattlearena.android.engine.ai.AiBattle;
import com.pokemonbattlearena.android.engine.ai.AiPlayer;
import com.pokemonbattlearena.android.engine.match.NoP;
import com.pokemonbattlearena.android.engine.match.NoPResult;
import com.pokemonbattlearena.android.fragment.chat.ChatType;
import com.pokemonbattlearena.android.fragment.team.TeamStatFragment;
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
import com.pokemonbattlearena.android.fragment.chat.ChatFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.gms.games.GamesStatusCodes.STATUS_OK;

public class BattleActivity extends BaseActivity implements OnTabSelectListener, RoomUpdateListener, RealTimeMessageReceivedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoomStatusUpdateListener, BattleFragment.OnBattleFragmentTouchListener, ChatFragment.OnChatLoadedListener, BattleEndListener {
    private static final String TAG = BattleActivity.class.getSimpleName();
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private static DatabaseReference mRootFirebase;
    private GoogleApiClient mGoogleApiClient;
    private FragmentManager mFragmentManager;
    private BattleFragment mBattleFragment;
    private ChatFragment mChatFragment;
    private TeamStatFragment mTeamStatFragment;
    private BottomBar mBottomBar;
    private Button mCancelBattleButton;
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
    private PokemonTeam mOpponentTeam = null;
    private String mUsername = null;
    //MUSIC
    private MediaPlayer mMusic;

    private final RuntimeTypeAdapterFactory<Command> mCommandRuntimeTypeAdapter = RuntimeTypeAdapterFactory
            .of(Command.class, "type")
            .registerSubtype(Attack.class)
            .registerSubtype(NoP.class)
            .registerSubtype(Switch.class);

    private final RuntimeTypeAdapterFactory<CommandResult> mCommandResultRuntimeTypeAdapter = RuntimeTypeAdapterFactory
            .of(CommandResult.class, "type")
            .registerSubtype(AttackResult.class)
            .registerSubtype(NoPResult.class)
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
        mPreferences = getSharedPreferences(getString(R.string.battle_prefs), Context.MODE_PRIVATE);
        mBottomBar = (BottomBar) findViewById(R.id.battle_bottom_bar);
        mBottomBar.setDefaultTab(R.id.tab_battle);

        mFragmentManager = getFragmentManager();
        mUsername = mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, PokemonUtils.DEFAULT_NAME);
        // Button listeners
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleApiClient.connect();

        mBattleFragment = new BattleFragment();

        mChatFragment = new ChatFragment();
        Bundle chatBundle = new Bundle();
        ChatType type = isAiBattle ? ChatType.GLOBAL : ChatType.IN_GAME;
        chatBundle.putSerializable(PokemonUtils.CHAT_TYPE_KEY, type);
        // we want to hide the ability to switch chats when AI battle mode
        chatBundle.putBoolean(PokemonUtils.CHAT_ALLOW_IN_GAME, !isAiBattle);
        mChatFragment.setArguments(chatBundle);

        mTeamStatFragment = new TeamStatFragment();
        Bundle statBundle = new Bundle();
        statBundle.putString(PokemonUtils.POKEMON_TEAM_KEY, new Gson().toJson(getSavedTeam(), PokemonTeam.class));
        mTeamStatFragment.setArguments(statBundle);

        mFragmentManager.beginTransaction()
                .add(R.id.battle_container, mChatFragment, getString(R.string.fragment_chat))
                .add(R.id.battle_container, mTeamStatFragment, getString(R.string.fragment_team_stat))
                .add(R.id.battle_container, mBattleFragment, getString(R.string.fragment_battle))
                .hide(mChatFragment)
                .hide(mTeamStatFragment)
                .commit();

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
        if(mMusic == null) {
            //Sets up and plays theme music
            createMusic();
            setUpMusic(R.raw.music_battle, R.raw.music_battle_cont);
        } else {
            mMusic.start();
        }

        super.onStart();
    }

    @Override
    public void onStop() {
        mMusic.pause();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mMusic.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMusic.start();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMusic.stop();
        mMusic.release();
        mMusic = null;
        super.onDestroy();
    }

    public void createMusic() {
        if (mMusic != null && mMusic.isPlaying()) {
            mMusic.stop();
            mMusic.release();
        }
        mMusic = new MediaPlayer();
    }

    public void startMusic(int id) {
        Uri ins = Uri.parse(PokemonUtils.ROOT_URL + id);
        try{
            mMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMusic.start();
                }
            });
            mMusic.setDataSource(this, ins);
        } catch (IOException e) {
            Log.e(TAG, "Could not set DataSource for MediaPlayer");
        }
        mMusic.prepareAsync();
    }

    public void setUpMusic(int startId, int endId){
        final int ENDID = endId;
        Uri ins = Uri.parse(PokemonUtils.ROOT_URL + startId);
        try {
            mMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMusic.start();
                    mMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            createMusic();
                            startMusic(ENDID);
                            mMusic.setLooping(true);
                            mMusic.setOnCompletionListener(null);
                        }
                    });
                }
            });
            mMusic.setDataSource(this, ins);
        } catch (IOException e) {
            Log.e(TAG, "Could not set DataSource for MediaPlayer");
        }
        mMusic.prepareAsync();
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_teams:
                if (mTeamStatFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .hide(mBattleFragment)
                            .hide(mChatFragment)
                            .show(mTeamStatFragment)
                            .commit();
                }
                break;
            case R.id.tab_battle:
                if (mBattleFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .show(mBattleFragment)
                            .hide(mChatFragment)
                            .hide(mTeamStatFragment)
                            .commit();
                }
                break;
            case R.id.tab_chat:
                if (mChatFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .show(mChatFragment)
                            .hide(mTeamStatFragment)
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
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        finish();
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

    private void sendPokemonTeam(PokemonTeam savedTeam) {
        String json = new Gson().toJson(savedTeam);
        byte[] message = json.getBytes();
        String id = findOpponentId();
        if (id != null) {
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message, mRoomId, id);
            Log.d(TAG, "Sent team over Google API");
        }
    }

    private void sendUsername(String selfUsername) {
        byte[] byteMessage = selfUsername.getBytes();
        String id = findOpponentId();
        if (id != null) {
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, byteMessage,
                    mRoomId, id);
            Log.d(TAG, "Reliable message (username) sent to " + id + " + " + selfUsername);
        }
    }

    private String findOpponentId() {
        for (Participant p : mParticipants) {
            if (!p.getParticipantId().equals(mMyId)) {
               return p.getParticipantId();
            }
        }
        return null;
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(this);
    }
    //endregion

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
        }
        finish();
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        byte[] buf = realTimeMessage.getMessageData();
        String bufferString = new String(buf);
        Log.d(TAG, "In Game Message Received: " + bufferString);
        if (mOpponentUsername == null) {
            mOpponentUsername = bufferString.trim();
            sendPokemonTeam(getSavedTeam());
            return;
        }
        if (mOpponentTeam == null && mOpponentUsername != null) {
            try {
                mOpponentTeam = new Gson().fromJson(bufferString, PokemonTeam.class);
                setupBattleWithOpponent();
                return;
            } catch (Exception e) {
                mOpponentTeam = null;
                return;
            }
        }
        if (mOpponentTeam != null && mOpponentUsername != null) {
            if (mIsHost) {
                Command command = mCommandGson.fromJson(bufferString, Command.class);
                Log.d(TAG, "We got a command from client of type: " + command.getClass());
                boolean phaseReady = mBattle.getCurrentBattlePhase().queueCommand(command);
                if (phaseReady) {
                    handleBattleResult();
                }
            } else {
                boolean nop = false;
                BattlePhaseResult resultFromJson = mCommandResultGson.fromJson(bufferString, BattlePhaseResult.class);
                Log.d(TAG, "We got a battle phase result: " + resultFromJson.toString());
                for (CommandResult commandResult : resultFromJson.getCommandResults()) {
                    // Update the internal state of the battle (only host really needs to do this, but opponent can too)
                    // Have opponent update their own battle state if you want to use the Battle object directly to update the UI (which makes more sense, IMO)
                    mBattle.applyCommandResult(commandResult);
                    if (mBattle.oppPokemonFainted()) {
                        nop = true;
                    }
                    Log.d(TAG, commandResult.getTargetInfo().toString());
                }

                mBattleFragment.enableButtonActions(true);
                if (mBattle.selfPokemonFainted()) {
                    Button force;
                    force = (Button)findViewById(R.id.switch_button);
                    force.performClick();
                } else if (nop) {
                    sendClientMessage(new NoP(mBattle.getSelf()));
                    mBattleFragment.enableButtonActions(false);
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
            BattlePokemonPlayer opponent = getPlayerForTeam(mOpponentUsername, mOpponentTeam);
            BattlePokemonPlayer self = getPlayerForTeam(mUsername, getSavedTeam());
            mBattle = new Battle(self, opponent);
            mBattleFragment.setPlayer(self);
            mBattleFragment.setOpponent(opponent);
        }

//        getFragmentManager().executePendingTransactions();

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

    private PokemonTeam getSavedTeam() {
        String teamJSON = mPreferences.getString(PokemonUtils.POKEMON_TEAM_KEY, PokemonUtils.DEFAULT_TEAM);
        if (!teamJSON.equals(PokemonUtils.DEFAULT_TEAM)) {
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
        showProgressDialog();
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!isAiBattle) {
                    Toast.makeText(mApplication, R.string.cancelled_battle, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
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
        leaveRoom();
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
        leaveRoom();
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
                Log.d(TAG, getString(R.string.message_sent) + message);
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
        return mHostId;
    }

    @Override
    public void onBattleEnd() {
        mChatFragment.deleteChatRoom();
        if (mBattle instanceof AiBattle) {
            if (mBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication, R.string.ai_win, Toast.LENGTH_LONG).show();
            } else if (mBattle.oppPokemonFainted()) {
                Toast.makeText(mApplication, R.string.player_win, Toast.LENGTH_LONG).show();
            }
        }

        if (mBattle.isFinished()) {
            if (mBattle.selfPokemonFainted()) {
                Toast.makeText(mApplication, mUsername + getString(R.string.winner), Toast.LENGTH_LONG).show();
            } else if (mBattle.oppPokemonFainted()){
                Toast.makeText(mApplication, mOpponentUsername + getString(R.string.winner), Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
