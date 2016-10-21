package com.pokemonbattlearena.android;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
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
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.fragments.battle.BattleHomeFragment;
import com.pokemonbattlearena.android.fragments.chat.ChatHomeFragment;
import com.pokemonbattlearena.android.fragments.team.TeamsHomeFragment;
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
        TeamsHomeFragment.OnPokemonTeamSelectedListener,
        BattleHomeFragment.OnBattleReadyToStartListener {

    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static String TAG = BottomBarActivity.class.getSimpleName();

    // GOOGLE PLAY GAMES FIELDS
    private static final int RC_SIGN_IN = 9001;
    private final static int RC_SELECT_PLAYERS = 7789;
    private final static int RC_INVITATION_INBOX = 10001;
    private String mRoomCreatorId = null;
    private String mRoomId = null;
    private String mMyId = null;
    private ArrayList<Participant> mParticipants = null;
    private String mIncomingInvitationId = null;

    // GOOGLE PLAY SIGN IN FIELDS
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    private boolean battleBegun = false;
    private boolean teamReady = false;
    private int[] teamIDs;

    private FragmentManager mFragmentManager;
    private BattleHomeFragment mBattleHomeFragment;
    private TeamsHomeFragment mTeamsHomeFragment;
    private ChatHomeFragment mChatHomeFragment;

    private BottomBar mBottomBar;

    public void onTeamSelected(int[] pokemonIDs) {
        Log.d(TAG, "Selected: " + pokemonIDs.toString());
        teamIDs = pokemonIDs;
        teamReady = true;
        if (mFragmentManager != null) {
            mBattleHomeFragment = new BattleHomeFragment();
            Bundle battleArgs = new Bundle();
            battleArgs.putIntArray("pokemonIDs", pokemonIDs);
            mBattleHomeFragment.setArguments(battleArgs);
            mFragmentManager.beginTransaction().replace(R.id.container, mBattleHomeFragment, "battle").commit();
            mBottomBar.selectTabWithId(R.id.tab_battle);
            mBattleHomeFragment.setBattleVisible(true);
        }
    }

    @Override
    public void onBattleReady(int[] pokemonIDs) {

    }

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

        // Listens for a tab touch (Only first touch of new tab)
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_teams:
                        if (mTeamsHomeFragment == null) {
                            mTeamsHomeFragment = createTeamsHomeFragment();
                            mFragmentManager.beginTransaction()
                                    .add(R.id.container, mTeamsHomeFragment, "team")
                                    .commit();
                        } else {
                            mFragmentManager.beginTransaction()
                                    .replace(R.id.container, mTeamsHomeFragment, "team")
                                    .commit();
                        }
                        break;
                    case R.id.tab_battle:
                        if (mTeamsHomeFragment != null && mTeamsHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mTeamsHomeFragment).commit();
                        }
                        if (mChatHomeFragment != null && mChatHomeFragment.isAdded()) {
                            mFragmentManager.beginTransaction().remove(mChatHomeFragment).commit();
                        }
                        break;
                    case R.id.tab_chat:
                        if (mChatHomeFragment == null) {
                            mChatHomeFragment = new ChatHomeFragment();
                            mFragmentManager.beginTransaction()
                                    .add(R.id.container, mChatHomeFragment, "battle")
                                    .commit();
                        } else {
                            mFragmentManager.beginTransaction()
                                    .replace(R.id.container, mChatHomeFragment, "chat")
                                    .commit();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        // Listens for a tab touch (Only when 'reselected')
        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_teams:
                        Toast.makeText(BottomBarActivity.this, "Teams Again", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_battle:
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
    }

    private TeamsHomeFragment createTeamsHomeFragment() {
        TeamsHomeFragment teamsHomeFragment = new TeamsHomeFragment();
        // Set the team size
        Bundle teamArgs = new Bundle();
        teamArgs.putInt("teamSize", 6);
        teamsHomeFragment.setArguments(teamArgs);
        return teamsHomeFragment;
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

    /*

        Start GoogleApiClient Callbacks

     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (bundle != null) {

            Invitation inv = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                mBattleHomeFragment.startBattle();
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

    /*

        Activity Callbacks

     */
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
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(resultCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(resultCode, intent);
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            //TODO: exit the battle
            return;
        }
        if (data == null || data.getExtras() == null || data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS) == null) {
            return;
        }

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
        assert inv != null;
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
        mBattleHomeFragment.startBattle();
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
        Log.d(TAG, "Peer(S) connected");
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
        byte[] message = ("I am: " + mMyId + " -created the room: " + mRoomCreatorId).getBytes();
        for (Participant p : mParticipants) {
            Log.w(TAG, "Participant: "  + p.getDisplayName() + ", id " + p.getParticipantId());

            if (p.getParticipantId().equals(mMyId)) {
                Log.w(TAG, "Ignoring sending message to self (" + p.getParticipantId() + ")");
                continue;
            }

            if (p.getStatus() !=  Participant.STATUS_JOINED) {
                Log.d(TAG, "Participant is apparently no longer joined?");
            } else {
                Games.RealTimeMultiplayer.sendReliableMessage(mApplication.getGoogleApiClient(), null, message,
                        mRoomId, p.getParticipantId());
                Log.d(TAG, "Reliable message sent (sendMessage()) + " + new String(message));
            }
        }
    }

    /*

        Private Methods

     */

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setRoomStatusUpdateListener(this)
                .setMessageReceivedListener(mBattleHomeFragment);
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

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mApplication.getGoogleApiClient(), this, mRoomId);
            mRoomId = null;
            mRoomCreatorId = null;
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
