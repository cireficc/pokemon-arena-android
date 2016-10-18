package com.pokemonbattlearena.android.fragments.team;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;

/**
 * Created by Spencer Amann on 10/17/16.
 */

public class PokemonGridAdapter extends BaseAdapter {
    private ArrayList<Pokemon> mItemList;
    private Context mContext;

    public PokemonGridAdapter(Context c, ArrayList<Pokemon> pokemon) {
        mContext = c;
        mItemList = pokemon;
    }
    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = createGridItem(inflater, parent, mItemList.get(position));
        }
        convertView.setTag(mItemList.get(position));
        return convertView;
    }

    private View createGridItem(LayoutInflater inflater, ViewGroup container, Pokemon pokemon) {
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

        return gridItem;
    }
}
