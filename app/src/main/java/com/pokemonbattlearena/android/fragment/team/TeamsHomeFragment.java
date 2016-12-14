package com.pokemonbattlearena.android.fragment.team;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pokemonbattlearena.android.adapter.MoveAdapter;
import com.pokemonbattlearena.android.adapter.PokemonGridAdapter;
import com.pokemonbattlearena.android.application.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.util.PokemonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by droidowl on 9/25/16.
 */

public class TeamsHomeFragment extends Fragment implements GridView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = TeamsHomeFragment.class.getSimpleName();

    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication;
    private GridView mGridView;
    private Button mCancelButton;
    private Button mSaveButton;
    private PokemonGridAdapter mAdapter;
    private OnPokemonTeamSelectedListener mCallback;
    private ArrayList<Pokemon> selectedTeamArrayList;

    public TeamsHomeFragment() {
        super();
    }

    public interface OnPokemonTeamSelectedListener {
        void onTeamSelected(String pokemonJSON);
        void toggleAddTeamFragment();
        ArrayList<String> retrieveTeamOrder();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mGridView = (GridView)  view.findViewById(R.id.team_gridview);
        mCancelButton = (Button) view.findViewById(R.id.cancel_team_button);
        mSaveButton = (Button) view.findViewById(R.id.save_team_button);
        selectedTeamArrayList = new ArrayList<>(PokemonUtils.TEAM_SIZE_INT);
        mApplication = PokemonBattleApplication.getInstance();
        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();
        mAdapter = new PokemonGridAdapter(getActivity(), mItemArray, R.layout.builder_grid_item);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mCancelButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnPokemonTeamSelectedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_team_button:
                if (selectedTeamArrayList.size() >= PokemonUtils.TEAM_SIZE_INT) {
                    //prompts team name and completes the team setup / save
                    promptTeamName();
                } else {
                    Toast.makeText(mApplication, "You must have" +PokemonUtils.TEAM_SIZE_STRING +" Pokemon in your team", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.cancel_team_button:
                mCallback.toggleAddTeamFragment();
                break;

            default:
                break;
        }
    }

    private void promptTeamName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View saveTeamView = inflater.inflate(R.layout.saved_team_name_dialog,(ViewGroup) getActivity().findViewById(R.id.teams_home),false);
        final EditText mTeamName = (EditText) saveTeamView.findViewById(R.id.team_name_dialog_editText);
        builder.setTitle("Set Team Name");
        builder.setView(saveTeamView);
        builder.setCancelable(false);
        builder.setPositiveButton("Complete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PokemonTeam pokemonTeam = new PokemonTeam(PokemonUtils.TEAM_SIZE_INT);
                pokemonTeam.setTeamName(mTeamName.getText().toString());
                //does not allow a null team name
                if(pokemonTeam.getTeamName() == null || pokemonTeam.getTeamName().equals("")){
                    Toast.makeText(mApplication, "Please enter a team name", Toast.LENGTH_SHORT).show();
                }
                //checks if team name already exists
                else if(mCallback.retrieveTeamOrder().contains(pokemonTeam.getTeamName())) {
                    Toast.makeText(mApplication, "Team name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    for (Pokemon pokemon : selectedTeamArrayList) {
                        pokemonTeam.addPokemon(pokemon);
                    }
                    mCallback.onTeamSelected(new Gson().toJson(pokemonTeam));
                }
            }
        });
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Pokemon selectedPokemon = (Pokemon) mAdapter.getItem(position);
        final PokemonGridViewItem item = (PokemonGridViewItem) view.getTag();
        final List<Move> moveList = mApplication.getBattleDatabase().getMovesForPokemon(selectedPokemon);
        final List<Move> selectedMoves = new ArrayList<>();
        if (!selectedTeamArrayList.contains(selectedPokemon)) {
            if (selectedTeamArrayList.size() >= PokemonUtils.TEAM_SIZE_INT) {
                Toast.makeText(mApplication, "You can only select " + PokemonUtils.TEAM_SIZE_INT + " pokemon for your team", Toast.LENGTH_SHORT).show();
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
                builder.setTitle(R.string.pick_move_title);
                builder.setView(moveSelectionView);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.save_moves, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPokemon.setActiveMoveList(selectedMoves);
                        selectedTeamArrayList.add(selectedPokemon);
                        item.mCheckbox.setChecked(true);
                    }
                });
                builder.setNeutralButton(R.string.default_moves, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Move> moves = mApplication.getBattleDatabase().getMovesForPokemon(selectedPokemon);
                        Collections.shuffle(moves);
                        selectedPokemon.setActiveMoveList(moves.subList(0,4));
                        selectedTeamArrayList.add(selectedPokemon);
                        item.mCheckbox.setChecked(true);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
}
