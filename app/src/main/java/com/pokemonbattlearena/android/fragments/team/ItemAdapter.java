package com.pokemonbattlearena.android.fragments.team;

/**
 * Created by mitchcout on 11/20/2016.
 */

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.match.PokemonTeam;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<Pair<Long, PokemonTeam>, ItemAdapter.ViewHolder>
{
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public ItemAdapter(ArrayList<Pair<Long, PokemonTeam>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create unique layout here?
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        String text = "This is a test "+viewType;
        ((TextView) view.findViewById(R.id.team_name_textView)).setText(text);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

//        holder.itemView.setTag(text);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {

        public TextView mTeamName;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mTeamName = (TextView) itemView.findViewById(R.id.team_name_textView);
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
    }
}
