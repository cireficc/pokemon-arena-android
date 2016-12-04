package com.pokemonbattlearena.android.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.fragments.battle.MainMenuFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.stephentuso.welcome.WelcomeHelper;

public class HomeActivity extends BaseActivity implements OnTabSelectListener, MainMenuFragment.OnHomeFragmentTouchListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private WelcomeHelper mWelcomeHelper;
    private GoogleApiClient mGoogleApiClient;
    private BottomBar mBottomBar;
    private FragmentManager mFragmentManager;
    // GOOGLE PLAY SIGN IN FIELDS
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWelcomeHelper = new WelcomeHelper(this, com.pokemonbattlearena.android.activity.SplashActivity.class);

        mBottomBar = (BottomBar) findViewById(R.id.home_bottom_bar);
        mBottomBar.setDefaultTab(R.id.tab_battle);
        mBottomBar.setOnTabSelectListener(this);

        mFragmentManager = getFragmentManager();

        mFragmentManager.beginTransaction().add(R.id.home_container, new MainMenuFragment(), "home").commit();

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
        super.onStart();
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
                break;
            case R.id.tab_battle:
                break;
            case R.id.tab_chat:
                break;
        }
    }

    @Override
    public void onBattleNowClicked() {
        Intent battleIntent = new Intent();
        battleIntent.setAction("om.pokemonbattlearena.android.battle.START");

        startActivityForResult(battleIntent, PokemonUtils.BATTLE_REQUEST);
    }

    @Override
    public void onAiBattleClicked() {

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
}
