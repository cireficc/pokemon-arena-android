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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button mSaveButton;
    private PokemonGridAdapter mAdapter;
    private OnPokemonTeamSelectedListener mCallback;
    private int mTeamSize = 1;

    public TeamsHomeFragment() {
        super();
    }

    public interface OnPokemonTeamSelectedListener {
        void onTeamSelected(int[] pokemonIDs);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mGridView = (GridView)  view.findViewById(R.id.team_gridview);
        mSaveButton = (Button) view.findViewById(R.id.save_team_button);

        mApplication = PokemonBattleApplication.getInstance();
        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();
        mAdapter = new PokemonGridAdapter(getActivity(), mItemArray, mTeamSize);
        mGridView.setAdapter(mAdapter);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null && mAdapter != null) {
                    mCallback.onTeamSelected(mAdapter.getSelectedTeam());
                }
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

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mTeamSize = args.getInt("teamSize");
    }
}
