package com.pokemonbattlearena.android.fragment.team;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.pokemonbattlearena.android.adapter.ItemAdapter;
import com.pokemonbattlearena.android.application.PokemonBattleApplication;
import com.pokemonbattlearena.android.util.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;


public class SavedTeamsFragment extends Fragment {

    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    public static DatabaseReference root;
    private String username;
    private ChildEventListener mChildListener;
    int longValue;

    private boolean onStartUp;

    private DragListView mDragListView;
    private static int adapterListSizeCurrent;
    private static ArrayList<Pair<Long, PokemonTeam>> mSavedTeams;
    private SharedPreferences mPreferences;
    private FloatingActionButton addTeamButton;

    private View.OnClickListener deleteListener;
    private OnSavedTeamsFragmentTouchListener mCallback;

    public SavedTeamsFragment() {
        onStartUp = true;
        mSavedTeams = new ArrayList<Pair<Long, PokemonTeam>>();
        longValue = 0;
        adapterListSizeCurrent = 0;
    }

    public interface OnSavedTeamsFragmentTouchListener {
        void toggleAddTeamFragment();
        void updateTeamOrder();
        ArrayList<String> retrieveTeamOrder();
        String getNewestPokemonTeamName();
        void deleteSavedTeam(String teamName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //connect to this user's saved teams on Firebase
        mPreferences = getActivity().getSharedPreferences(PokemonUtils.PREFS_KEY, Context.MODE_PRIVATE);
        username = mPreferences.getString(PokemonUtils.PROFILE_NAME_KEY, PokemonUtils.DEFAULT_NAME);
        root = FirebaseDatabase.getInstance().getReference().child(PokemonUtils.FIREBASE_USER).child(username).child(PokemonUtils.FIREBASE_TEAMS);
        //listener for populating saved teams page
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendTeamsList(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendTeamsList(dataSnapshot);
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
        root.removeEventListener(mChildListener);
        onStartUp = false;
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
        deleteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = v;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View deleteView = inflater.inflate(R.layout.saved_team_delete_dialog,(ViewGroup) getActivity().findViewById(R.id.teams_home),false);
                builder.setTitle(R.string.delete_team_title);
                builder.setView(deleteView);
                builder.setCancelable(false);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String teamName = (String)((TextView)((View)view.getParent()).findViewById(R.id.team_name_textView)).getText();
                        for(int i = 0; i < mSavedTeams.size(); i++){
                            if(mSavedTeams.get(i).second.getTeamName().equals(teamName)){
                                mSavedTeams.remove(i);
                            }
                        }
                        setAdapter();
                        mCallback.deleteSavedTeam(teamName);

                        mCallback.updateTeamOrder();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing, just cancel
                    }
                });
                builder.show();
            }
        };

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

        root.addChildEventListener(mChildListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mCallback.updateTeamOrder();
        root.removeEventListener(mChildListener);
    }

    private ArrayList<Pair<Long, PokemonTeam>> getOrderedSavedTeams(){
        ArrayList<Pair<Long, PokemonTeam>> orderedPokemonTeam = new ArrayList<Pair<Long, PokemonTeam>>();
        ArrayList<String> teamOrder = mCallback.retrieveTeamOrder();

        if(teamOrder == null || teamOrder.size() != mSavedTeams.size()){
            return mSavedTeams;
        }
        if(!teamOrder.contains(mCallback.getNewestPokemonTeamName())){
            ArrayList<String> tempList = new ArrayList<String>();
            //add newest pokemonTeam to the front of the list
            tempList.add(mCallback.getNewestPokemonTeamName());
            for(String name : teamOrder){
                tempList.add(name);
            }
            teamOrder = tempList;
        }
        for (String team : teamOrder) {
            for (int i = 0; i < mSavedTeams.size(); i++) {
                if (mSavedTeams.get(i).second.getTeamName().equals(team)) {
                    orderedPokemonTeam.add(mSavedTeams.get(i));
                    break;
                }
            }
        }

        return orderedPokemonTeam;
    }

    private void appendTeamsList(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue() != null) {
            String teamJSON = (String) dataSnapshot.getValue();
            PokemonTeam pokemonTeam = new Gson().fromJson(teamJSON, PokemonTeam.class);
            mSavedTeams.add(new Pair(new Long(longValue), pokemonTeam));
            longValue++;

            if(!onStartUp) {
                mSavedTeams = getOrderedSavedTeams();
                setAdapter();

                if(mDragListView.getAdapter().getItemList().size() > adapterListSizeCurrent){
                    if(mCallback.retrieveTeamOrder().size() <= mSavedTeams.size()) {
                        mCallback.updateTeamOrder();
                        adapterListSizeCurrent++;
                    }
                }
            }
        }
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
                mCallback.updateTeamOrder();
            }
        });
    }

    private void setAdapter() {
        ItemAdapter listAdapter = new ItemAdapter(mSavedTeams, R.layout.saved_team_full_item, R.id.saved_team_cardView, false, deleteListener);
        mDragListView.setAdapter(listAdapter, true);
    }

    public static ArrayList<Pair<Long, PokemonTeam>> getSavedTeams(){
        return mSavedTeams;
    }

    /**
     * returns an ArrayList of the saved PokemonTeams in the order that they are displayed in the Drag ListView
     * @return ordered list of saved teams
     */
    public ArrayList<Pair<Long, PokemonTeam>> getTeamOrder() {
        ArrayList<Pair<Long, PokemonTeam>> teamOrder = new ArrayList<Pair<Long, PokemonTeam>>();

        if(mDragListView.getAdapter().getItemList() == null){
            return mSavedTeams;
        }
        for (int i = 0; i < mDragListView.getAdapter().getItemList().size(); i++) {
            Long index = mDragListView.getAdapter().getItemId(i);
            for (int j = 0; j < mSavedTeams.size(); j++) {
                if (mSavedTeams.get(j).first.intValue() == index.intValue()) {
                    Pair<Long, PokemonTeam> team = mSavedTeams.get(j);
                    teamOrder.add(i, team);
                    break;
                }
            }
        }
        return teamOrder;
    }
}
