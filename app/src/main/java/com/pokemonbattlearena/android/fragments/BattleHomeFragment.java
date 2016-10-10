package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pokemonbattlearena.android.R;

/**
 * Created by droidowl on 9/25/16.
 */

public class BattleHomeFragment extends Fragment implements View.OnClickListener {

    private Button mBattleButton;
    private boolean battleBegun = false;
    private BattleUIFragment battleUIFragment;

    public BattleHomeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battlehome, container, false);
        mBattleButton = (Button) view.findViewById(R.id.quick_battle_button);
        mBattleButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (!battleBegun) {
            battleBegun = true;
            battleUIFragment = new BattleUIFragment();
            getFragmentManager().beginTransaction().add(R.id.battle_ui_container, battleUIFragment).commit();
            mBattleButton.setText(R.string.cancel_battle);
        } else {
            getFragmentManager().beginTransaction().remove(battleUIFragment).commit();
            battleUIFragment = null;
            mBattleButton.setText(R.string.battle);
            battleBegun = false;
        }
    }
}
