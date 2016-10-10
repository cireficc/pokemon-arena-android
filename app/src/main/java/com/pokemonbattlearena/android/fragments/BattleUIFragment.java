package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class BattleUIFragment extends Fragment implements View.OnClickListener, RealTimeMessageReceivedListener  {
    static final String TAG = "Pokemon Battle Room";

    PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();

    private TypeModel mTypeModel;

    private Pokemon mPlayerPokemon;

    private Pokemon mOppenentPokemon;

    private List<Pokemon> mPokemonList;

    private List<Move> mPlayerMoves;

    private Button[] mMoveButtons;

    private ImageView mPlayerImage;

    private ImageView mOpponentImage;

    private TextView mPlayerPokemonName;

    private TextView mOpponentPokemonName;



    // launch the player selection screen
// minimum: 1 other player; maximum: 3 other players

    public BattleUIFragment() {
        super();
        mTypeModel = new TypeModel();
    }

    /**
     * Pass any information the battle fragment will need
     * @param args A `Bundle` holding the information
     */
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        // Pull apart any information put in the bundle
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battleui, container, false);
        mPokemonList = mApplication.getBattleDatabase().getPokemons();

        View playerView = view.findViewById(R.id.player_1_ui);
        View opponentView = view.findViewById(R.id.player_2_ui);

        mPlayerPokemonName = (TextView) playerView.findViewById(R.id.active_name_textview);
        mOpponentPokemonName = (TextView) opponentView.findViewById(R.id.active_name_textview);


        mPlayerImage = (ImageView) playerView.findViewById(R.id.active_imageview);
        mOpponentImage = (ImageView) opponentView.findViewById(R.id.active_imageview);


        setupMoveButtons(view);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (mPlayerPokemon != null) {
            switch (v.getId()) {
                case R.id.move_button_0:
                    Toast.makeText(mApplication, mPlayerMoves.get(0).getName() + ": " + mPlayerMoves.get(0).getPower(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.move_button_1:
                    Toast.makeText(mApplication, mPlayerMoves.get(1).getName() + ": " + mPlayerMoves.get(1).getPower(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.move_button_2:
                    Toast.makeText(mApplication, mPlayerMoves.get(2).getName() + ": " + mPlayerMoves.get(2).getPower(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.move_button_3:
                    Toast.makeText(mApplication, mPlayerMoves.get(3).getName() + ": " + mPlayerMoves.get(3).getPower(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void setupMoveButtons(View v) {
        for (int i = 0; i < mPlayerMoves.size(); i++) {
            if (i < 4) {
                int buttonId = getResources().getIdentifier("move_button_" + i, "id", getActivity().getPackageName());
                Button b = (Button) v.findViewById(buttonId);
                b.setVisibility(View.VISIBLE);
                b.setOnClickListener(this);
                mMoveButtons[i] = b;
                b.setText(mPlayerMoves.get(i).getName());
                b.setBackgroundColor(getActivity().getColor(mTypeModel.getColorForType(mPlayerMoves.get(i).getType1())));
            }
        }
    }

    private Drawable getDrawableForPokemon(Context c, String name) {
        String key = "ic_pokemon_" + name.toLowerCase();
        int id = getResources().getIdentifier(key, "drawable", c.getPackageName());
        return c.getDrawable(id);
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        // Message format: pokemonName1:pokemonName2
        Log.d(TAG, "Message Received: " + realTimeMessage.toString());
        String[] pokemonNames = realTimeMessage.toString().split(":");
        try {
            mPlayerPokemon = mApplication.getBattleDatabase().getPokemonDao().queryForId(pokemonNames[0]);
            mPlayerImage.setImageDrawable(getDrawableForPokemon(getActivity(), pokemonNames[0]));
            mPlayerPokemonName.setText(mPlayerPokemon.getName());

            mOppenentPokemon = mApplication.getBattleDatabase().getPokemonDao().queryForId(pokemonNames[1]);
            mPlayerImage.setImageDrawable(getDrawableForPokemon(getActivity(), pokemonNames[1]));
            mOpponentPokemonName.setText(mOppenentPokemon.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
