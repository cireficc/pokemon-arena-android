package com.pokemonbattlearena.android.activity;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pokemonbattlearena.android.fragment.chat.ChatFragment;
import com.pokemonbattlearena.android.fragment.chat.ChatType;
import com.pokemonbattlearena.android.util.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragment.home.HomeFragment;
import com.pokemonbattlearena.android.fragment.team.SavedTeamsFragment;
import com.pokemonbattlearena.android.fragment.team.TeamsHomeFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends BaseActivity implements OnTabSelectListener,
        HomeFragment.OnHomeFragmentTouchListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ChatFragment.OnChatLoadedListener,
        SavedTeamsFragment.OnSavedTeamsFragmentTouchListener,
        TeamsHomeFragment.OnPokemonTeamSelectedListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final int TEAM_SIZE_INT = 6;
    private WelcomeHelper mWelcomeHelper;
    private GoogleApiClient mGoogleApiClient;
    private BottomBar mBottomBar;
    private FragmentManager mFragmentManager;
    private HomeFragment mHomeFragment;
    private SavedTeamsFragment mSavedTeamsFragment;
    private TeamsHomeFragment mTeamBuilderFragment;
    private ChatFragment mChatFragment;
    private SharedPreferences mPreferences;
    // GOOGLE PLAY SIGN IN FIELDS
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private String newestAddedPokemonTeamName;
    private String mUsername;
    //MUSIC
    private MediaPlayer mMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPreferences = getSharedPreferences(PokemonUtils.PREFS_KEY, Context.MODE_PRIVATE);
        mWelcomeHelper = new WelcomeHelper(this, com.pokemonbattlearena.android.activity.SplashActivity.class);
        mUsername = mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, PokemonUtils.DEFAULT_NAME);
        mBottomBar = (BottomBar) findViewById(R.id.home_bottom_bar);
        mBottomBar.setDefaultTab(R.id.tab_battle);
        mBottomBar.setOnTabSelectListener(this);

        mFragmentManager = getFragmentManager();

        mHomeFragment = new HomeFragment();
        mSavedTeamsFragment = new SavedTeamsFragment();
        mChatFragment = new ChatFragment();
        Bundle chatBundle = new Bundle();
        chatBundle.putSerializable(PokemonUtils.CHAT_TYPE_KEY, ChatType.GLOBAL);
        chatBundle.putSerializable(PokemonUtils.CHAT_ALLOW_IN_GAME, false);
        mChatFragment.setArguments(chatBundle);

        mFragmentManager.beginTransaction()
                .add(R.id.home_container, mHomeFragment, "home")
                .add(R.id.home_container, mSavedTeamsFragment, "team_save")
                .add(R.id.home_container, mChatFragment, "chat")
                .hide(mChatFragment)
                .hide(mSavedTeamsFragment)
                .commit();

        // Button listeners
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mWelcomeHelper.show(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG, "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG, "Connecting client.");
            mGoogleApiClient.connect();
        }
        if(mMusic == null) {
            //Sets up and plays theme music
            createMusic();
            setUpMusic(R.raw.music_menu, R.raw.music_menu_cont);
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
        Uri ins = Uri.parse(PokemonUtils.ROOT_URL+ startId);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9001 && resultCode == RESULT_OK) {

        } else if (requestCode == WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST) {
            mGoogleApiClient.connect();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWelcomeHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
            case R.id.tab_teams:
                if (mSavedTeamsFragment != null && mSavedTeamsFragment.isAdded() && mSavedTeamsFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .hide(mHomeFragment)
                            .hide(mChatFragment)
                            .show(mSavedTeamsFragment)
                            .commit();
                }
                break;
            case R.id.tab_battle:
                if (mHomeFragment != null && mHomeFragment.isAdded() && mHomeFragment.isHidden()) {
                    mFragmentManager.beginTransaction()
                            .hide(mSavedTeamsFragment)
                            .hide(mChatFragment)
                            .show(mHomeFragment)
                            .commit();
                }
                break;
            case R.id.tab_chat:
                if (mChatFragment != null && !mChatFragment.isAdded()) {
                    mFragmentManager.beginTransaction()
                            .add(R.id.home_container, mChatFragment, getString(R.string.fragment_chat))
                            .hide(mSavedTeamsFragment)
                            .hide(mHomeFragment)
                            .commit();
                } else if (mChatFragment != null && mChatFragment.isAdded() && mChatFragment.isHidden()){
                    mFragmentManager.beginTransaction()
                            .hide(mHomeFragment)
                            .hide(mSavedTeamsFragment)
                            .show(mChatFragment)
                            .commit();
                }
                break;
        }
    }

    @Override
    public void onBattleNowClicked() {
        Intent battleIntent = new Intent();
        battleIntent.setAction(PokemonUtils.ACTION_START);
        battleIntent.putExtra(PokemonUtils.AI_BATTLE_KEY, false);
        startActivityForResult(battleIntent, PokemonUtils.BATTLE_REQUEST);
    }

    @Override
    public void onAiBattleClicked() {
        Intent battleIntent = new Intent();
        battleIntent.setAction(PokemonUtils.ACTION_START);
        battleIntent.putExtra(PokemonUtils.AI_BATTLE_KEY, true);
        startActivityForResult(battleIntent, PokemonUtils.BATTLE_REQUEST);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

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
    public void onChatLoaded() {
        hideProgressDialog();
    }

    @Override
    public String getHostId() {
        return mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, PokemonUtils.DEFAULT_NAME);
    }

    @Override
    public void onTeamSelected(String pokemonJSON) {
        Log.d(TAG, "Selected: " + pokemonJSON);
        if (mFragmentManager != null) {
            PokemonTeam team = new Gson().fromJson(pokemonJSON, PokemonTeam.class);
            //add saved Team to Firebase
            newestAddedPokemonTeamName = team.getTeamName();
            addSavedTeam(newestAddedPokemonTeamName, pokemonJSON);

            toggleAddTeamFragment();
        }

    }
    private void addSavedTeam(String teamName, String pokemonJSON) {
        DatabaseReference root = SavedTeamsFragment.root;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(teamName, pokemonJSON);
        root.updateChildren(map);
    }
    @Override
    public void toggleAddTeamFragment() {
        if (mFragmentManager != null && mSavedTeamsFragment != null && mSavedTeamsFragment.isAdded()) {
            mTeamBuilderFragment = createTeamsHomeFragment();
            mFragmentManager.beginTransaction().remove(mSavedTeamsFragment).commit();
            mFragmentManager.beginTransaction().add(R.id.home_container, mTeamBuilderFragment, getString(R.string.fragment_team_builder)).commit();
            mFragmentManager.executePendingTransactions();
        } else if (mFragmentManager != null && mTeamBuilderFragment != null && mTeamBuilderFragment.isAdded()) {
            mFragmentManager.beginTransaction().remove(mTeamBuilderFragment).commit();
            mFragmentManager.beginTransaction().add(R.id.home_container, mSavedTeamsFragment, getString(R.string.fragment_team_save)).commit();
            mFragmentManager.executePendingTransactions();
        }
    }

    private TeamsHomeFragment createTeamsHomeFragment() {
        TeamsHomeFragment teamsHomeFragment = new TeamsHomeFragment();
        // Set the team size
        Bundle teamArgs = new Bundle();
        teamArgs.putInt(PokemonUtils.TEAM_SIZE_STRING, TEAM_SIZE_INT);
        teamsHomeFragment.setArguments(teamArgs);
        return teamsHomeFragment;
    }

    @Override
    public void updateTeamOrder() {
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
        editor.putString(PokemonUtils.ORDER_TEAM_JSON_KEY, orderedTeamJSON).apply();
        editor.commit();

        //add first team as your active team
        if(savedTeams.size() != 0) {
            String pokemonJSON = new Gson().toJson(savedTeams.get(0).second);
            setCurrentTeam(pokemonJSON);
        }
    }

    private void setCurrentTeam(String pokemonJSON){
        SharedPreferences.Editor editor = mPreferences.edit();
        Log.d(TAG, "Setting current team: " + pokemonJSON);
        editor.putString(PokemonUtils.CURRENT_TEAM_KEY, pokemonJSON).apply();
        editor.commit();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child(PokemonUtils.FIREBASE_USER).child(mUsername).child(PokemonUtils.FIREBASE_ACTIVE_TEAM);
        root.setValue(pokemonJSON);
    }

    @Override
    public ArrayList<String> retrieveTeamOrder() {
        String orderedTeamJSON = mPreferences.getString(PokemonUtils.ORDER_TEAM_JSON_KEY, PokemonUtils.DEFAULT_TEAM);
        if (!orderedTeamJSON.equals(PokemonUtils.DEFAULT_TEAM)) {
            Log.d(TAG, "Got team order: " + orderedTeamJSON);
            return new Gson().fromJson(orderedTeamJSON, ArrayList.class);
        }
        return new ArrayList<String>();
    }

    @Override
    public String getNewestPokemonTeamName() {
        return newestAddedPokemonTeamName;
    }

    @Override
    public void deleteSavedTeam(String teamName) {
        DatabaseReference root = SavedTeamsFragment.root;
        root.child(teamName).removeValue();
    }
}
