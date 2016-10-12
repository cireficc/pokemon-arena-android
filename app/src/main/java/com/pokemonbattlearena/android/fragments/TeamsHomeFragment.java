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

public class TeamsHomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Teams Fragment";

    private Button mTeamButton;
    private boolean creatingTeam = false;
    private TeamSetupFragment teamSetupFragment;

    public TeamsHomeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mTeamButton = (Button) view.findViewById(R.id.select_team_button);
        mTeamButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        if(!creatingTeam) {
            creatingTeam = true;
            teamSetupFragment = new TeamSetupFragment();
            getFragmentManager().beginTransaction().add(R.id.teams_ui_container, teamSetupFragment).commit();
            mTeamButton.setText(R.string.cancel);
        } else {
            creatingTeam = false;
            getFragmentManager().beginTransaction().remove(teamSetupFragment).commit();
            teamSetupFragment = null;
            mTeamButton.setText(R.string.teams);
        }
    }
}
