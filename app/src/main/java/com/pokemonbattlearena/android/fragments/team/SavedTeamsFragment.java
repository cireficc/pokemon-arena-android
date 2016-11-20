package com.pokemonbattlearena.android.fragments.team;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;


public class SavedTeamsFragment extends Fragment {

    private DragListView mDragListView;
    private ArrayList<Pair<Long, PokemonTeam>> mSavedTeams;
    private SharedPreferences mPreferences;

    public SavedTeamsFragment() {
        // Required empty public constructor
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
//        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
//            @Override
//            public void onItemDragStarted(int position) {
//                mRefreshLayout.setEnabled(false);
//            }
//
//            @Override
//            public void onItemDragEnded(int fromPosition, int toPosition) {
//                mRefreshLayout.setEnabled(true);
//            }
//        });

        //TODO: populate saved teams arrayList
        String teamJSON = mPreferences.getString("pokemonTeamJSON", "mew");
        PokemonTeam pokemonTeam = new Gson().fromJson(teamJSON, PokemonTeam.class);
        mSavedTeams = new ArrayList<Pair<Long, PokemonTeam>>();
        mSavedTeams.add(new Pair(new Long(0), pokemonTeam));
        mSavedTeams.add(new Pair(new Long(1), pokemonTeam));
        mSavedTeams.add(new Pair(new Long(2), pokemonTeam));

        setupListRecyclerView();
        return view;
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter listAdapter = new ItemAdapter(mSavedTeams, R.layout.saved_team_full_item, R.id.saved_team_cardView, false);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MySavedTeamItem(getContext(), R.layout.saved_team_full_item));
    }

    private static class MySavedTeamItem extends DragItem {
        public MySavedTeamItem(Context context, int layoutId) {
            super(context, layoutId);
        }
        @Override
        public void onBindDragView(View clickedView, View dragView){
            //??????
        }
    }
}
