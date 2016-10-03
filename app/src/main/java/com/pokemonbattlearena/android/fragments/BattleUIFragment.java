package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.List;
import java.util.Random;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class BattleUIFragment extends Fragment {

    PokemonBattleApplication mApplication;

    private TypeModel mTypeModel;

    public BattleUIFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battleui, container, false);
        mApplication = (PokemonBattleApplication) getActivity().getApplication();

        View player1View = view.findViewById(R.id.player_1_ui);
        View player2View = view.findViewById(R.id.player_2_ui);

        Random random = new Random();

        int randomPokemon = random.nextInt(150);
        int randomPokemon2 = random.nextInt(150);
        Pokemon p1 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon);
        Pokemon p2 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon2);

        TextView p1Name = (TextView) player1View.findViewById(R.id.active_name_textview);
        p1Name.setText(p1.getName());
        TextView p2Name = (TextView) player2View.findViewById(R.id.active_name_textview);
        p2Name.setText(p2.getName());

        ImageView p1Image = (ImageView) player1View.findViewById(R.id.active_imageview);
        ImageView p2Image = (ImageView) player2View.findViewById(R.id.active_imageview);
        p1Image.setImageDrawable(getContext().getDrawable(getDrawableForPokemon(p1.getName())));
        p2Image.setImageDrawable(getContext().getDrawable(getDrawableForPokemon(p2.getName())));

        setupMoveButtons(view, p1);

        return view;
    }

    private void setupMoveButtons(View v, Pokemon p) {
        Button m1 = (Button) v.findViewById(R.id.move_1_button);
        Button m2 = (Button) v.findViewById(R.id.move_2_button);
        Button m3 = (Button) v.findViewById(R.id.move_3_button);
        Button m4 = (Button) v.findViewById(R.id.move_4_button);


        List<Move> moves = mApplication.getBattleDatabase().getMovesForPokemon(p);

        if (moves.size() >= 4) {
            m1.setText(moves.get(0).getName());
            m2.setText(moves.get(1).getName());
            m3.setText(moves.get(2).getName());
            m4.setText(moves.get(3).getName());

            m1.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(moves.get(0).getType1())));
            m2.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(moves.get(1).getType1())));
            m3.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(moves.get(2).getType1())));
            m4.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(moves.get(3).getType1())));
        }
    }

    private int getDrawableForPokemon(String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        return getResources().getIdentifier(key,"drawable",getActivity().getPackageName());
    }
}
