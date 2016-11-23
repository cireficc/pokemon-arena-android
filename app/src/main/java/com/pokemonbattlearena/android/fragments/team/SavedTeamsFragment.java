package com.pokemonbattlearena.android.fragments.team;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragments.battle.MainMenuFragment;
import com.woxthebox.draglistview.DragListView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class SavedTeamsFragment extends Fragment {

    private DragListView mDragListView;
    private ArrayList<Pair<Long, PokemonTeam>> mSavedTeams;
    private SharedPreferences mPreferences;
    private FloatingActionButton addTeamButton;

    private OnSavedTeamsFragmentTouchListener mCallback;

    public SavedTeamsFragment() {
        // Required empty public constructor
    }

    public interface OnSavedTeamsFragmentTouchListener {
        void toggleAddTeamFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_teams, container, false);
        mDragListView = (DragListView) view.findViewById(R.id.team_drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        addTeamButton = (FloatingActionButton) view.findViewById(R.id.team_add_new_button);
        addTeamButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.toggleAddTeamFragment();
            }
        });

        //TODO: populate saved teams arrayList
        String teamJSON = mPreferences.getString("pokemonTeamJSON", "mew");
        PokemonTeam pokemonTeam = new Gson().fromJson(teamJSON, PokemonTeam.class);
        mSavedTeams = new ArrayList<Pair<Long, PokemonTeam>>();
        pokemonTeam.setTeamName("My Pokemon Team");
//        TextView otherText = (TextView) inflater.inflate(R.layout.saved_team_other_textview, mDragListView, false);
        mSavedTeams.add(new Pair(new Long(0), pokemonTeam));
//        mSavedTeams.add(new Pair(new Long(1), otherText));
        mSavedTeams.add(new Pair(new Long(2), pokemonTeam));
        mSavedTeams.add(new Pair(new Long(3), pokemonTeam));

        setupListRecyclerView();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (SavedTeamsFragment.OnSavedTeamsFragmentTouchListener) context;
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter listAdapter = new ItemAdapter(mSavedTeams, R.layout.saved_team_full_item, R.id.saved_team_cardView, false);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(null);
    }
}
