package com.pokemonbattlearena.android.adapter;

/**
 * Created by mitchcout on 11/20/2016.
 */

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<Pair<Long, PokemonTeam>, ItemAdapter.ViewHolder>
{
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private View.OnClickListener deleteListener;

    private LayoutInflater inflater;

    public ItemAdapter(ArrayList<Pair<Long, PokemonTeam>> list, int layoutId, int grabHandleId, boolean dragOnLongPress, View.OnClickListener deleteListener) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
        this.deleteListener = deleteListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(mLayoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //set team name
        String text = (mItemList.get(position).second).getTeamName();
        holder.mTeamName.setText(text);
        holder.itemView.setTag(text);
        //set team images
        holder.addPokemonTeam((mItemList.get(position).second));

        //set button listeners
        holder.mDeleteTeam.setOnClickListener(deleteListener);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {

        public TextView mTeamName;
        public LinearLayout mPokemonTeam;
        public ImageButton mDeleteTeam;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mTeamName = (TextView) itemView.findViewById(R.id.team_name_textView);
            mPokemonTeam = (LinearLayout) itemView.findViewById(R.id.team_all_pokemon);
            mDeleteTeam = (ImageButton) itemView.findViewById(R.id.team_delete_imageButton);
        }

        @Override
        public void onItemClicked(View view) {
            //
        }

        @Override
        public boolean onItemLongClicked(View view) {
            //
            return true;
        }

        public void addPokemonTeam(PokemonTeam team) {
            mPokemonTeam.removeAllViews();
            for(Pokemon poke : team.getPokemons()) {
                addPokemonToTeam(poke);
            }
        }

        private void addPokemonToTeam(Pokemon pokemon) {
            View pokemonItem;
            pokemonItem = inflater.inflate(R.layout.saved_team_pokemon_item, mPokemonTeam, false);

            //get views
            TextView mPokemonName = (TextView) pokemonItem.findViewById(R.id.pokemon_team_item_textview);
            ImageView mPokemonImage = (ImageView) pokemonItem.findViewById(R.id.pokemon_team_item_imageview);

            //set values
            mPokemonName.setText(pokemon.getName());
            int PokemonImageId = pokemonItem.getContext().getResources().getIdentifier("ic_pokemon_" + pokemon.getName().toLowerCase(), "drawable", pokemonItem.getContext().getPackageName());
            mPokemonImage.setImageResource(PokemonImageId);

            mPokemonTeam.addView(pokemonItem);
        }
    }
}