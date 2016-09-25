package awesome.com.pokemonbattle;

import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        bottomBar.setDefaultTab(R.id.tab_battle);
        // Listens for a tab touch (Can be first or Nth after)
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_teams:
                        Toast.makeText(MainActivity.this, "Teams", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_battle:
                        Toast.makeText(MainActivity.this, "Battle", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_chat:
                        Toast.makeText(MainActivity.this, "Chat", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Teams Again", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_battle:
                        Toast.makeText(MainActivity.this, "Battle Again", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tab_chat:
                        Toast.makeText(MainActivity.this, "Chat Again", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
