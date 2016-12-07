package com.pokemonbattlearena.android.fragment.team;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.adapter.StatAdapter;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.util.PokemonUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeamStatFragment extends Fragment {

    private ListView mListView;
    private StatAdapter mAdapter;

    public TeamStatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_team_stat, container, false);
        mListView = (ListView) v.findViewById(R.id.stat_list_view);
        if (getArguments() != null) {
            PokemonTeam team = new Gson().fromJson(getArguments().getString(PokemonUtils.POKEMON_TEAM_KEY), PokemonTeam.class);
            BattlePokemonTeam bTeam = new BattlePokemonTeam(team);
            mAdapter = new StatAdapter(getActivity(), bTeam);
            mListView.setAdapter(mAdapter);
        }
        return v;
    }

}
