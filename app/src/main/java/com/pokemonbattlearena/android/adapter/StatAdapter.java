package com.pokemonbattlearena.android.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemon;
import com.pokemonbattlearena.android.engine.match.BattlePokemonTeam;
import com.pokemonbattlearena.android.util.PokemonUtils;

/**
 * Created by Spencer Amann on 12/5/16.
 */

public class StatAdapter extends BaseAdapter {
    BattlePokemonTeam team;
    Context context;

    public StatAdapter(Context c, BattlePokemonTeam team) {
        this.team = team;
        this.context = c;
    }

    @Override
    public int getCount() {
        return team.getBattlePokemons().size();
    }

    @Override
    public Object getItem(int position) {
        return team.getBattlePokemons().get(position);
    }

    @Override
    public long getItemId(int position) {
        return team.getBattlePokemons().get(position).getOriginalPokemon().getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Pokemon pokemon = team.getBattlePokemons().get(position).getOriginalPokemon();
        final BattlePokemon bPokemon = team.getBattlePokemons().get(position);
        StatAdapter.StatViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.stat_list_item, parent, false);
            holder = new StatAdapter.StatViewHolder();
            holder.name = (TextView)  convertView.findViewById(R.id.stat_name_textview);
            holder.pokemonImage = (ImageView) convertView.findViewById(R.id.pokemon_stat_imageview);
            holder.moves = (TextView) convertView.findViewById(R.id.stat_move_textview);
            holder.cardView = (CardView) convertView.findViewById(R.id.stat_cardview);
            holder.stats = (TextView) convertView.findViewById(R.id.pokemon_stat_textview);
            convertView.setTag(holder);
        } else {
            holder = (StatViewHolder) convertView.getTag();
        }
        holder.name.setText(pokemon.getName());
        holder.pokemonImage.setImageDrawable(PokemonUtils.getDrawableForPokemon(context, pokemon.getName(), PokemonUtils.typePokemon));
        StringBuilder builder = new StringBuilder("Moves: \n");
        for (Move move : bPokemon.getMoveSet()) {
            builder.append(move.getName() + "\n");
        }
        holder.moves.setText(builder.toString());

        StringBuilder statBuilder = new StringBuilder("Stats: ");
        statBuilder.append("\nAttack: " + pokemon.getAttack());
        statBuilder.append("\nSpecial Attack: " + pokemon.getSpecialAttack());
        statBuilder.append("\nDefense: " + pokemon.getDefense());
        statBuilder.append("\nSpecial Defense: " + pokemon.getSpecialDefense());
        statBuilder.append("\nSpeed: " + pokemon.getSpeed());
        statBuilder.append("\nType 1: " + pokemon.getType1());
        statBuilder.append("\nType 2: " + pokemon.getType2());

        holder.stats.setText(statBuilder.toString());

        return convertView;
    }


    public class StatViewHolder {
        CardView cardView;
        TextView name;
        ImageView pokemonImage;
        TextView moves;
        TextView stats;
    }
}
