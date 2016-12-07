package com.pokemonbattlearena.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.fragment.team.PokemonGridViewItem;

import java.util.ArrayList;

/**
 * Created by Spencer Amann on 10/17/16.
 */

public class PokemonGridAdapter extends BaseAdapter {
    private ArrayList<Pokemon> mItemList;
    private Context mContext;
    private int layoutId;

    public PokemonGridAdapter(Context c, ArrayList<Pokemon> pokemon, int layoutId) {
        this.mContext = c;
        this.mItemList = pokemon;
        this.layoutId = layoutId;
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

        final Pokemon pokemon = mItemList.get(position);
        PokemonGridViewItem holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, parent, false);
            holder = new PokemonGridViewItem();

            holder.card = (CardView) convertView.findViewById(R.id.item_layout);
            holder.mName = (TextView) convertView.findViewById(R.id.pokemon_name_title_textview);
            holder.mImage = (ImageView) convertView.findViewById(R.id.pokemon_grid_imageview);
            holder.mCheckbox = (CheckBox) convertView.findViewById(R.id.pokemon_grid_item_checkbox);
            holder.pokemon = pokemon;

            if (layoutId == R.layout.builder_grid_item) {
                holder.mId = (TextView) convertView.findViewById(R.id.pokemon_number_textview);
                holder.mType1 = (ImageView) convertView.findViewById(R.id.pokemon_type1_imageview);
                holder.mType2 = (ImageView) convertView.findViewById(R.id.pokemon_type2_imageview);
            }

            convertView.setTag(holder);
        } else {
            holder = (PokemonGridViewItem) convertView.getTag();
        }

        // Set TextView components
        if (layoutId == R.layout.builder_grid_item) {
            holder.mId.setText("#" + pokemon.getId());
            int color = position % 3;
            switch (color) {
                case 0:
                    holder.card.setCardBackgroundColor(mContext.getColor(R.color.color_charizard));
                    break;
                case 1:
                    holder.card.setCardBackgroundColor(mContext.getColor(R.color.color_blastoise));
                    break;
                case 2:
                    holder.card.setCardBackgroundColor(mContext.getColor(R.color.color_venusaur));
                    break;
                default:
                    break;
            }
            int Type1Id = holder.mType1.getContext().getResources().getIdentifier("ic_type_" + pokemon.getType1().toLowerCase(), "drawable", holder.mType1.getContext().getPackageName());
            holder.mType1.setImageResource(Type1Id);
            int Type2Id = holder.mType2.getContext().getResources().getIdentifier("ic_type_" + pokemon.getType2().toLowerCase(), "drawable", holder.mType2.getContext().getPackageName());
            holder.mType2.setImageResource(Type2Id);

        } else if (layoutId == R.layout.switch_grid_item) {
            holder.card.setCardBackgroundColor(Color.LTGRAY);
        }
        holder.mName.setText(pokemon.getName());
        holder.mCheckbox.setChecked(false);

        // Set ImageView components
        int imageId = holder.mImage.getContext().getResources().getIdentifier("ic_pokemon_" + pokemon.getName().toLowerCase(), "drawable", holder.mImage.getContext().getPackageName());
        holder.mImage.setImageResource(imageId);

        return convertView;
    }
}
