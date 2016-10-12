package com.pokemonbattlearena.android.fragments.team;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.CardView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.ItemAdapter;

import java.util.ArrayList;

import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

/**
 * @author Mitch Couturier
 * @version 10/07/2016
 */
@TargetApi(24)
public class TeamSetupFragment extends Fragment implements View.OnClickListener {

    private DragListView mDragListView;
    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication;

    public TeamSetupFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

        mApplication = PokemonBattleApplication.getInstance();
        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();

        //setupGridVerticalRecyclerView();
        for(Pokemon pokemon : mItemArray) {
            createGridItem(inflater, (ViewGroup) view.findViewById(R.id.team_grid_layout), pokemon);
        }

        return view;
    }

//    private void setupGridVerticalRecyclerView() {
//        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
//        mDragListView.setAdapter(listAdapter, true);
//        mDragListView.setCanDragHorizontally(true);
//        mDragListView.setCustomDragItem(null);
//    }
//
//    private static class MyDragItem extends DragItem {
//
//        public MyDragItem(Context context, int layoutId) {
//            super(context, layoutId);
//        }
//
//        @Override
//        public void onBindDragView(View clickedView, View dragView) {
//            CharSequence text = ((TextView) clickedView.findViewById(R.id.pokemon_name_title_textview)).getText();
//            ((TextView) dragView.findViewById(R.id.pokemon_name_title_textview)).setText(text);
//        }
//    }

    @Override
    public void onClick(View v) {

    }

    /**
     * Creates a clickable item for the team selector grid
     * @param inflater
     * @param container
     * @param pokemon
     */
    private void createGridItem(LayoutInflater inflater, ViewGroup container, Pokemon pokemon) {
        View gridItem = inflater.inflate(R.layout.grid_item, container, false);

        CardView card = (CardView) gridItem.findViewById(R.id.item_layout);
        TextView mId = (TextView) gridItem.findViewById(R.id.pokemon_number_textview);
        TextView mName = (TextView) gridItem.findViewById(R.id.pokemon_name_title_textview);
        ImageView mImage = (ImageView) gridItem.findViewById(R.id.pokemon_grid_imageview);
        ImageView mType1 = (ImageView) gridItem.findViewById(R.id.pokemon_type1_imageview);
        ImageView mType2 = (ImageView) gridItem.findViewById(R.id.pokemon_type2_imageview);

        //set TextView components
        mId.setText("#"+pokemon.getId());
        mName.setText(pokemon.getName());

        //set ImageView components
        int imageId = mImage.getContext().getResources().getIdentifier("ic_pokemon_"+pokemon.getName().toLowerCase(), "drawable", mImage.getContext().getPackageName());
        mImage.setImageResource(imageId);
        int Type1Id = mType1.getContext().getResources().getIdentifier("ic_type_"+pokemon.getType1().toLowerCase(), "drawable", mType1.getContext().getPackageName());
        mType1.setImageResource(Type1Id);
        int Type2Id = mType2.getContext().getResources().getIdentifier("ic_type_"+pokemon.getType2().toLowerCase(), "drawable", mType2.getContext().getPackageName());
        mType2.setImageResource(Type2Id);

        gridItem.setTag(pokemon.getName());

        gridItem.setClickable(true);

        //add gridItem to gridLayout
        container.addView(gridItem);
    }
}
