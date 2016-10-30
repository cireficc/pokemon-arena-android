package com.pokemonbattlearena.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Spencer Amann on 10/28/16.
 */

public class TypeBanAdapter extends BaseAdapter {
    Context mContext;

    public TypeBanAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        return TypeModel.typeNames.length;
    }

    @Override
    public Object getItem(int position) {
        return TypeModel.typeNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TypeBanViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.type_ban_item, parent, false);
            holder = new TypeBanViewHolder();
            holder.typeImage = (ImageView) convertView.findViewById(R.id.type_ban_image);
            holder.typeText = (TextView) convertView.findViewById(R.id.type_ban_text);
            convertView.setTag(holder);
        } else {
            holder = (TypeBanViewHolder) convertView.getTag();
        }

        holder.typeImage.setImageDrawable(mContext.getDrawable(TypeModel.typeImageIds[position]));
        holder.typeText.setText(TypeModel.typeNames[position]);
        return convertView;
    }

    private class TypeBanViewHolder {
        TextView typeText;
        ImageView typeImage;
    }
}
