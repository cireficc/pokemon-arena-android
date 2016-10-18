package com.pokemonbattlearena.android.fragments.team;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;


/**
 * Created by droidowl on 9/25/16.
 */

public class TeamsHomeFragment extends Fragment implements GridView.OnItemClickListener {

    private static final String TAG = "Teams Fragment";

    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication;
    private GridView mGridView;
    private PokemonGridAdapter mAdapter;

    public TeamsHomeFragment() {
        super();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public interface OnPokemonTeamSelectedListener {
        void onTeamSelected(int pokemonId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mGridView = (GridView)  view.findViewById(R.id.team_gridview);

        mApplication = PokemonBattleApplication.getInstance();
        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();
        mAdapter = new PokemonGridAdapter(getActivity(), mItemArray);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

        return view;
    }
}
