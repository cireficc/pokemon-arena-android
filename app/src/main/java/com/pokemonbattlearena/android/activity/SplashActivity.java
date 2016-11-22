package com.pokemonbattlearena.android.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pokemonbattlearena.android.R;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.FragmentWelcomePage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

/**
 * Created by Spencer Amann on 11/22/16.
 */

public class SplashActivity extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.color_blastoise)
                .page(new TitlePage(R.drawable.ic_title_logo,
                        "Welcome to Pokemon Battle!")
                ).page(new FragmentWelcomePage() {
                    @Override
                    protected Fragment fragment() {
                        return new NameFragment();
                    }
                }.background(R.color.color_charizard))
                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.fade_out)
                .canSkip(false)
                .build();
    }
}