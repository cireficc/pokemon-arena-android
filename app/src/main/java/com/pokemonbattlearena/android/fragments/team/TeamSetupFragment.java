package com.pokemonbattlearena.android.fragments.team;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;

/**
 * @author Mitch Couturier
 * @version 10/07/2016
 */
@TargetApi(24)
public class TeamSetupFragment extends Fragment implements View.OnClickListener {

    private ArrayList<Pokemon> mItemArray;
    private PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();

    public TeamSetupFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

        mItemArray = (ArrayList<Pokemon>) mApplication.getBattleDatabase().getPokemons();

        for(Pokemon pokemon : mItemArray) {
            createGridItem(inflater, (ViewGroup) view.findViewById(R.id.team_grid_layout), pokemon);
        }

        return view;
    }

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
        CheckBox mCheckbox = (CheckBox) gridItem.findViewById(R.id.select_checkbox);

        //set TextView components
        mId.setText("#"+pokemon.getId());
        mName.setText(pokemon.getName());
        mCheckbox.setClickable(true);
        mCheckbox.setOnClickListener(this);

        switch (pokemon.getId() % 3) {
            case 0:
                card.setBackgroundColor(getActivity().getColor(R.color.color_blastoise));
                break;
            case 1:
                card.setBackgroundColor(getActivity().getColor(R.color.color_charizard));
                break;
            case 2:
                card.setBackgroundColor(getActivity().getColor(R.color.color_venusaur));
                break;
            default:
                card.setBackgroundColor(Color.BLACK);
                break;
        }

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
