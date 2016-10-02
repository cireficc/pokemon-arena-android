package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

        int randomPokemon = random.nextInt(151);
        int randomPokemon2 = random.nextInt(151);
        Pokemon p1 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon);
        Pokemon p2 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon2);

        TextView p1Name = (TextView) player1View.findViewById(R.id.active_name_textview);
        p1Name.setText(p1.getName());
        TextView p2Name = (TextView) player2View.findViewById(R.id.active_name_textview);
        p2Name.setText(p2.getName());



        setupMoveButtons(view, p1);
//        Resources res = getContext().getResources();
//        Drawable healthbar = res.getDrawable(R.drawable.healthbar);
//        ImageView p1Health = (ImageView) view.findViewById(R.id.p1_health_bar_container);
//        ImageView p2Health = (ImageView) view.findViewById(R.id.p2_health_bar_container);
//        p1Health.setImageDrawable(healthbar);
//        p2Health.setImageDrawable(healthbar);
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
}
