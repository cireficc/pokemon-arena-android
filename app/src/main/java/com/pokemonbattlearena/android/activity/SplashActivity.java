package com.pokemonbattlearena.android.activity;

import android.support.v4.app.Fragment;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.fragment.splash.NameFragment;
import com.pokemonbattlearena.android.fragment.splash.FirstTeamFragment;
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
                        getString(R.string.welcome_splash))
                ).page(new FragmentWelcomePage() {
                    @Override
                    protected Fragment fragment() {
                        return new NameFragment();
                    }
                }.background(R.color.color_charizard))
                .page(new FragmentWelcomePage() {
                    @Override
                    protected Fragment fragment() {
                        return new FirstTeamFragment();
                    }
                }.background(R.color.color_pikachu))
                .useCustomDoneButton(true)
                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.fade_out)
                .canSkip(false)
                .build();
    }
}