/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pokemonbattlearena.android;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;


public class ItemAdapter extends DragItemAdapter<Pokemon, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;


    public ItemAdapter (ArrayList<Pokemon> pokemons, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(false);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(pokemons);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //get info from pokemon object
        int pId = mItemList.get(position).getId();
        String pName =mItemList.get(position).getName();
        String pType1 = mItemList.get(position).getType1();
        String pType2 = mItemList.get(position).getType2();

        //set views with pokemon info
        holder.mName.setText(pName);
        holder.mId.setText("#"+pId);

        int imageId = holder.mImage.getContext().getResources().getIdentifier("ic_pokemon_"+pName.toLowerCase(), "drawable", holder.mImage.getContext().getPackageName());
        holder.mImage.setImageResource(imageId);

        int type1Id = holder.mType1.getContext().getResources().getIdentifier("ic_type_"+pType1.toLowerCase(), "drawable", holder.mType1.getContext().getPackageName());
        holder.mType1.setImageResource(type1Id);
        int type2Id = holder.mType2.getContext().getResources().getIdentifier("ic_type_"+pType2.toLowerCase(), "drawable", holder.mType2.getContext().getPackageName());
        holder.mType2.setImageResource(type2Id);

        holder.itemView.setTag(pName);
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mId;
        public TextView mName;
        public ImageView mImage;
        public ImageView mType1;
        public ImageView mType2;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mId = (TextView) itemView.findViewById(R.id.pokemon_number_textview);
            mName = (TextView) itemView.findViewById(R.id.pokemon_name_title_textview);
            mImage = (ImageView) itemView.findViewById(R.id.pokemon_grid_imageview);
            mType1 = (ImageView) itemView.findViewById(R.id.pokemon_type1_imageview);
            mType2 = (ImageView) itemView.findViewById(R.id.pokemon_type2_imageview);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}