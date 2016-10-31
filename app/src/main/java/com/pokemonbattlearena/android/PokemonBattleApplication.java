package com.pokemonbattlearena.android;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.pokemonbattlearena.android.engine.BattleEngine;
import com.pokemonbattlearena.android.engine.database.Database;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class PokemonBattleApplication extends Application {

    private BattleEngine mBattleEngine;
    private Database mBattleDatabase;
    private static GoogleApiClient mGoogleApiClient;
    private static PokemonBattleApplication singleton;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        mBattleEngine = BattleEngine.getInstance();
        mBattleDatabase = new Database(getApplicationContext());
    }

    public BattleEngine getBattleEngine() {
        return mBattleEngine;
    }

    public Database getBattleDatabase() {
        return mBattleDatabase;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient c) {
        mGoogleApiClient = c;
    }

    public static PokemonBattleApplication getInstance(){
        return singleton;
    }
}
