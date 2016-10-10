package com.pokemonbattlearena.android.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.GridLayoutManager;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.ItemAdapter;

import java.util.ArrayList;

import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

/**
 * @author Mitch Couturier
 * @version 10/07/2016
 */
public class TeamSetupFragment extends Fragment implements View.OnClickListener {

    private static final int TOTAL_POKEMON_COUNT = 151;

    private Button selectButton;
    private DragListView mDragListView;
    private ArrayList<String> mItemArray;
    private PokemonBattleApplication mApplication;

    public TeamSetupFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        //TODO: THIS vvv
//        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter());

//        mApplication = PokemonBattleApplication.getInstance();

        mItemArray = new ArrayList<>(TOTAL_POKEMON_COUNT);
        for(int i = 0; i < TOTAL_POKEMON_COUNT; i++) {
            mItemArray.add(i, "Test Name"+(i+1));
        }
        setupGridVerticalRecyclerView();

        return view;
    }

    private void setupGridVerticalRecyclerView() {
        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.pokemon_name_title_textview)).getText();
            ((TextView) dragView.findViewById(R.id.pokemon_name_title_textview)).setText(text);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
