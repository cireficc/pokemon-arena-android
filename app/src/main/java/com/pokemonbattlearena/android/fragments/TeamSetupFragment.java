package com.pokemonbattlearena.android.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.pokemonbattlearena.android.R;

/**
 * @author Mitch Couturier
 * @version 10/07/2016
 */
public class TeamSetupFragment extends Fragment implements View.OnClickListener {

    private Button selectButton;
    private Spinner selectSpinner;

    public TeamSetupFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_setup, container, false);

        selectButton = (Button) view.findViewById(R.id.select_pokemon_button);

        selectSpinner = (Spinner) view.findViewById(R.id.select_pokemon_spinner);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, );

        return view;
    }

    @Override
    public void onClick(View v) {

    }
}
