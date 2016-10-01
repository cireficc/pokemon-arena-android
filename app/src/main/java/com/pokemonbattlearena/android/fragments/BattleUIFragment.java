package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;
import com.pokemonbattlearena.android.engine.database.PokemonMove;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
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

        TextView pokemon1NameTextView = (TextView) view.findViewById(R.id.p1_active_pokemon_name);
        TextView pokemon2NameTextView = (TextView) view.findViewById(R.id.p2_active_pokemon_name);

        Random random = new Random();

        int randomPokemon = random.nextInt(151);
        int randomPokemon2 = random.nextInt(151);
        Pokemon p1 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon);
        Pokemon p2 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon2);

        pokemon1NameTextView.setText(p1.getName());
        pokemon2NameTextView.setText(p2.getName());


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.e("BATTLE", " NOT NUlL");
        }
    }
}
