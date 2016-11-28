package com.pokemonbattlearena.android.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragments.team.MoveAdapter;
import com.pokemonbattlearena.android.fragments.team.PokemonGridAdapter;
import com.pokemonbattlearena.android.fragments.team.PokemonGridViewItem;
import com.stephentuso.welcome.WelcomeFinisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Spencer Amann on 11/22/16.
 */
public class FirstTeamFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication;
    private GridView mGridView;
    private Button mSaveButton;
    private PokemonGridAdapter mAdapter;
    private int teamSize = 6;
    private ArrayList<Pokemon> selectedTeamArrayList;
    private TextView mSaveTeamText;

    private WelcomeFinisher welcomeFinisher;

    private SharedPreferences mPreferences;

    public FirstTeamFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mSaveTeamText = (TextView) view.findViewById(R.id.save_first_team_textview);
        mSaveTeamText.setVisibility(View.VISIBLE);
        mGridView = (GridView)  view.findViewById(R.id.team_gridview);
        mSaveButton = (Button) view.findViewById(R.id.save_team_button);
        selectedTeamArrayList = new ArrayList<>(teamSize);
        mApplication = PokemonBattleApplication.getInstance();
        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();
        mAdapter = new PokemonGridAdapter(getActivity(), mItemArray, R.layout.builder_grid_item);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mSaveButton.setOnClickListener(this);
        welcomeFinisher = new WelcomeFinisher(this);

        mPreferences = getActivity().getSharedPreferences("Pokemon Battle Prefs", Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (selectedTeamArrayList.size() >= teamSize) {
            PokemonTeam pokemonTeam = new PokemonTeam(teamSize);
            for (Pokemon pokemon : selectedTeamArrayList) {
                pokemonTeam.addPokemon(pokemon);
            }
            saveTeam(pokemonTeam);
            welcomeFinisher.finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Pokemon selectedPokemon = (Pokemon) mAdapter.getItem(position);
        final PokemonGridViewItem item = (PokemonGridViewItem) view.getTag();
        final List<Move> moveList = mApplication.getBattleDatabase().getMovesForPokemon(selectedPokemon);
        final List<Move> selectedMoves = new ArrayList<>();
        if (!selectedTeamArrayList.contains(selectedPokemon)) {
            if (selectedTeamArrayList.size() >= teamSize) {
                Toast.makeText(mApplication, "You can only select " + teamSize + " pokemon for your team", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View moveSelectionView = inflater.inflate(R.layout.move_selection_dialog,parent,false);
                final ListView moveListView = (ListView) moveSelectionView.findViewById(R.id.move_list_view);
                final MoveAdapter adapter = new MoveAdapter(getActivity(), moveList);
                moveListView.setAdapter(adapter);
                moveListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Move selectedMove = (Move) adapter.getItem(position);
                        MoveAdapter.MoveViewHolder holder = (MoveAdapter.MoveViewHolder) view.getTag();
                        if (!selectedMoves.contains(selectedMove) && selectedMoves.size() < 4) {
                            selectedMoves.add(selectedMove);
                            holder.moveCheckbox.setChecked(true);
                        } else {
                            selectedMoves.remove(selectedMove);
                            holder.moveCheckbox.setChecked(false);
                        }
                    }
                });
                builder.setTitle("Pick 4 Moves");
                builder.setView(moveSelectionView);
                builder.setCancelable(false);
                builder.setPositiveButton("Save Moves", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPokemon.setActiveMoveList(selectedMoves);
                        selectedTeamArrayList.add(selectedPokemon);
                        item.mCheckbox.setChecked(true);
                    }
                });
                builder.setNeutralButton("Default Moves", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Move> moves = mApplication.getBattleDatabase().getMovesForPokemon(selectedPokemon);
                        Collections.shuffle(moves);
                        selectedPokemon.setActiveMoveList(moves.subList(0,4));
                        selectedTeamArrayList.add(selectedPokemon);
                        item.mCheckbox.setChecked(true);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTeamArrayList.remove(selectedPokemon);
                    }
                });
                builder.show();
            }
        } else {
            item.mCheckbox.setChecked(false);
            selectedTeamArrayList.remove(selectedPokemon);
        }
    }

    private void saveTeam(PokemonTeam team) {
        SharedPreferences.Editor edit = mPreferences.edit();
        String json = new Gson().toJson(team, PokemonTeam.class);
        edit.putString("pokemon_team", json);
        edit.apply();
        edit.commit();
    }
}
