package com.pokemonbattlearena.android.fragments.team;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.PokemonMove;

import java.util.ArrayList;


/**
 * Created by droidowl on 9/25/16.
 */

public class TeamsHomeFragment extends Fragment {

    private static final String TAG = "Teams Fragment";

    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication;
    private GridView mGridView;
    private PokemonGridAdapter mAdapter;
    private int savedPokemonId = -1;
    private OnPokemonTeamSelectedListener mCallback;

    public TeamsHomeFragment() {
        super();
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
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Pokemon p = (Pokemon) parent.getItemAtPosition(position);
                Log.d(TAG, "Clicked Pokemon: " + p.getName());
                savedPokemonId = p.getId();
                mCallback.onTeamSelected(savedPokemonId);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnPokemonTeamSelectedListener) context;
            Log.d(TAG, "Worked");
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() +"must implement listener");
        }
    }
}
