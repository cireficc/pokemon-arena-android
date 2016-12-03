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
import com.pokemonbattlearena.android.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.fragments.battle.MainMenuFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.stephentuso.welcome.WelcomeHelper;

public class HomeActivity extends BaseActivity implements OnTabSelectListener, MainMenuFragment.OnHomeFragmentTouchListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private WelcomeHelper mWelcomeHelper;
    private GoogleApiClient mGoogleApiClient;
    private BottomBar mBottomBar;
    private FragmentManager mFragmentManager;

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
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mWelcomeHelper.show(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            Log.d(TAG, "Already connected to Google");
        }
        super.onStart();
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
}
