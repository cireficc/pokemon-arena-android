package awesome.com.pokemonbattle.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import awesome.com.pokemonbattle.R;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleHomeFragment extends Fragment {

    public BattleHomeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battlehome, container, false);
    }
}
