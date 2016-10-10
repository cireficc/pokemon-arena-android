package com.pokemonbattlearena.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
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
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import com.pokemonbattlearena.android.fragments.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.android.gms.games.GamesStatusCodes.STATUS_OK;
import static com.google.android.gms.games.GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED;
import static com.google.android.gms.games.GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED;

public class BottomBarActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        RealTimeMessageReceivedListener,
        RoomUpdateListener,
        RoomStatusUpdateListener,
        RealTimeMultiplayer.ReliableMessageSentCallback {
    private static final int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private boolean battleBegun = false;
    // Request codes for the UIs that we show with startActivityForResult:
    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_INVITATION_INBOX = 10001;
    private final static int RC_WAITING_ROOM = 10002;
    private String mRoomId = null;

    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    private String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    private String mIncomingInvitationId = null;

    // Message buffer for sending messages
    private byte[] mMsgBuf = new byte[2];
    private BattleUIFragment battleUIFragment;
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();

    private final static String TAG = BottomBarActivity.class.getSimpleName();

    /*
        Fragment Methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottombar);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        final BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        bottomBar.setDefaultTab(R.id.tab_battle);

        final BattleHomeFragment battleFragment = new BattleHomeFragment();
        final TeamsHomeFragment teamFragment = new TeamsHomeFragment();
        final ChatHomeFragment chatFragment = new ChatHomeFragment();
        final android.app.FragmentManager manger = getFragmentManager();

        // Button listeners

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mApplication.setGoogleApiClient(googleApiClient);
        manger.beginTransaction()
                .add(R.id.container, battleFragment, "battle")
                .commit();


        // Listens for a tab touch (Only first touch of new tab)
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_teams:
                        manger.beginTransaction()
                                .replace(R.id.container, teamFragment, "team")
                                .commit();
                        break;
                    case R.id.tab_battle:
                        manger.beginTransaction()
                                .replace(R.id.container, battleFragment, "battle")
                                .commit();
                        break;
                    case R.id.tab_chat:
                        manger.beginTransaction()
                                .replace(R.id.container, chatFragment, "chat")
                                .commit();
                        break;
                    default:
                        break;
                }
            }
        });

        // Listens for a tab touch (Only when 'reselected')
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_teams:
                        Toast.makeText(BottomBarActivity.this, "Teams Again", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_battle:
                        startQuickGame();
                        Toast.makeText(BottomBarActivity.this, "Battle Again", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_chat:
                        Toast.makeText(BottomBarActivity.this, "Chat Again", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        battleUIFragment = new BattleUIFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApplication.getGoogleApiClient().connect();
    }

    /*

        Start GoogleApiClient Callbacks

     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (bundle != null) {

            Invitation inv = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(mApplication.getGoogleApiClient(), roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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

    /*

        Activity Callbacks

     */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        Log.e(TAG, "Result of activity");
        if (requestCode == RC_SIGN_IN) {

        }

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
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(resultCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(resultCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (resultCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
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
        keepScreenOn();
        Games.RealTimeMultiplayer.create(mApplication.getGoogleApiClient(), rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            //TODO: Exit the battle
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setInvitationIdToAccept(invId);
        keepScreenOn();
        Games.RealTimeMultiplayer.join(mApplication.getGoogleApiClient(), roomConfigBuilder.build());
    }

    /*

        Start RoomUpdateListener Callbacks

     */
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

    private void launchBattleFrags() {
        if (battleUIFragment == null) {
            battleUIFragment = new BattleUIFragment();
        }
        if (!battleBegun) {
            battleBegun = true;
            getFragmentManager().beginTransaction().add(R.id.battle_ui_container, battleUIFragment).commitAllowingStateLoss();
        } else {
            getFragmentManager().beginTransaction().remove(battleUIFragment).commitAllowingStateLoss();
            battleUIFragment = null;
            battleBegun = false;
        }
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        // we have left the room; return to main screen.
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
        launchBattleFrags();
        sendMessage();
        updateRoom(room);
    }

    /*

        Start RoomStatusUpdateListener Callbacks

     */

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
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
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
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    /*

        MessageListener Callbacks

     */
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        // Message format: pokemonName1:pokemonName2
        Log.d(TAG, rtm.toString());
        byte[] buf = rtm.getMessageData();
        String bufferString = new String(buf);
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received:" + bufferString);
        Toast.makeText(this, "Message received: " + bufferString, Toast.LENGTH_LONG).show();
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

    private void sendMessage() {
        Log.d(TAG, "Sending Message");
        Random random = new Random();
        int r1 = random.nextInt(150);
        int r2 = random.nextInt(150);
        byte[] message = (r1 + ":" + r2).getBytes();
        for (Participant p : mParticipants) {
            Log.w(TAG, "Participant: "  + p.getDisplayName() + ", id " + p.getParticipantId());

            if (p.getParticipantId().equals(mMyId)) {
                Log.w(TAG, "Ignoring sending message to self (" + p.getParticipantId() + ")");
                continue;
            }

            if (p.getStatus() !=  Participant.STATUS_JOINED) {
                Log.d(TAG, "Participant is apparently no longer joined?");
                continue;
            } else {
                Games.RealTimeMultiplayer.sendReliableMessage(mApplication.getGoogleApiClient(), null, message,
                        mRoomId, p.getParticipantId());
                Log.d(TAG, "Reliable message sent (sendMessage()) + " + message);
            }
        }
    }

    /*

        Private Methods

     */
    private void startQuickGame() {
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mApplication.getGoogleApiClient(), 1, 3);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(battleUIFragment);
    }

    private void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
            mRoomId = room.getRoomId();
            mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mApplication.getGoogleApiClient()));
        }
        if (mParticipants != null) {
            // update game states
        }
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mApplication.getGoogleApiClient(), this, mRoomId);
            mRoomId = null;
        }
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    private void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
