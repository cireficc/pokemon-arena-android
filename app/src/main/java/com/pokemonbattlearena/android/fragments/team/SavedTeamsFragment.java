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

import com.google.android.gms.games.Games;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.pokemonbattlearena.android.fragments.battle.MainMenuFragment;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class SavedTeamsFragment extends Fragment {

    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    public static DatabaseReference root;
    private final String headRootName = "Users";
    private final String teamsRootName = "Teams";
    private String username;
    private ChildEventListener mChildListener;

    private boolean reorderingData;

    private DragListView mDragListView;
    private ArrayList<Pair<Long, PokemonTeam>> mSavedTeams;
    private SharedPreferences mPreferences;
    private FloatingActionButton addTeamButton;

    private OnSavedTeamsFragmentTouchListener mCallback;

    public SavedTeamsFragment() {
        //TODO: later set up so that root is defined here
    }

    public interface OnSavedTeamsFragmentTouchListener {
        void toggleAddTeamFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        //connect to this user's saved teams on Firebase
        username = Games.Players.getCurrentPlayer(mApplication.getGoogleApiClient()).getDisplayName();
        root = FirebaseDatabase.getInstance().getReference().child(headRootName).child(username).child(teamsRootName);
        //make sure the UI can update with any new or updated teams
        reorderingData = false;
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

        setupListRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (SavedTeamsFragment.OnSavedTeamsFragmentTouchListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSavedTeams = new ArrayList<Pair<Long, PokemonTeam>>();

        //listener for populating saved teams page
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!reorderingData) {
                    appendTeamsList(dataSnapshot);
                    System.out.println("LIST SIZE "+mSavedTeams.size());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(!reorderingData) {
                    appendTeamsList(dataSnapshot);
                    System.out.println("LIST SIZE "+mSavedTeams.size());
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        root.addChildEventListener(mChildListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        root.removeEventListener(mChildListener);
        reorderingData = false;
    }

    /**
     * Update the team order on Firebase by first clearing it then re-adding all nodes
     */
    private void updateTeamOrder() {
        //temporarily disable the root child listener so that no new teams appear
        reorderingData = true;
        root.removeValue();
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        //reorder items in new order
        for(int i = 0; i<mDragListView.getAdapter().getItemList().size();i++){
            Long index = mDragListView.getAdapter().getItemId(i);
            for(int j = 0; j<mSavedTeams.size();j++){
                if(mSavedTeams.get(j).first.intValue() == index.intValue()){
                    PokemonTeam team = mSavedTeams.get(j).second;
                    map.put(team.getTeamName(), new Gson().toJson(team));
                    break;
                }
            }
        }
        root.updateChildren(map);
    }

    private void appendTeamsList(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue() != null) {
            String teamJSON = (String) dataSnapshot.getValue();
            PokemonTeam pokemonTeam = new Gson().fromJson(teamJSON, PokemonTeam.class);
            mSavedTeams.add(new Pair(new Long(mSavedTeams.size()), pokemonTeam));
        }
        setAdapter();
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter();
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(null);
        mDragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
            }
            @Override
            public void onItemDragging(int itemPosition, float x, float y) {
            }
            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                updateTeamOrder();
            }
        });
    }

    private void setAdapter() {
        ItemAdapter listAdapter = new ItemAdapter(mSavedTeams, R.layout.saved_team_full_item, R.id.saved_team_cardView, false);
        mDragListView.setAdapter(listAdapter, true);
    }
}
