package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonbattlearena.android.ItemAdapter;
import com.pokemonbattlearena.android.MySwipeRefreshLayout;
import com.pokemonbattlearena.android.R;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by droidowl on 9/25/16.
 */

public class TeamsHomeFragment extends Fragment {

    private ArrayList<String> mItemArray;
    private DragListView mDragListView;
    private static final String TAG = "Teams Fragment";

    public TeamsHomeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teamshome, container, false);
        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
            }
        });

        mItemArray = new ArrayList<>(15);
        for (int i = 0; i < 15; i++) {
            mItemArray.add(i, "Electabuzz" + (i+1));
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
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.color_electabuzz));
        }
    }
}
