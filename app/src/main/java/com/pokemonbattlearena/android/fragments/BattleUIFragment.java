package com.pokemonbattlearena.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.TypeModel;
import com.pokemonbattlearena.android.engine.database.Move;
import com.pokemonbattlearena.android.engine.database.Pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Spencer Amann on 10/1/16.
 */

public class BattleUIFragment extends Fragment implements View.OnClickListener  {
    static final String TAG = "Pokemon Battle Room";

    PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();

    private TypeModel mTypeModel;

    private Pokemon mPlayerPokemon;

    private List<Move> mPlayerMoves;

    private Button[] mMoveButtons;


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

        View player1View = view.findViewById(R.id.player_1_ui);
        View player2View = view.findViewById(R.id.player_2_ui);

        Random random = new Random();

        int randomPokemon = random.nextInt(150);
        int randomPokemon2 = random.nextInt(150);

        Pokemon p1 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon);
        Pokemon p2 = mApplication.getBattleDatabase().getPokemons().get(randomPokemon2);

        mPlayerPokemon = p1;
        mPlayerMoves = mApplication.getBattleDatabase().getMovesForPokemon(p1);

        mMoveButtons = new Button[mPlayerMoves.size()];

        TextView p1Name = (TextView) player1View.findViewById(R.id.active_name_textview);
        p1Name.setText(p1.getName());
        TextView p2Name = (TextView) player2View.findViewById(R.id.active_name_textview);
        p2Name.setText(p2.getName());

        Log.d("Battle-UI", "p1: " + p1.getName() + "\tp2: " + p2.getName());

        ImageView p1Image = (ImageView) player1View.findViewById(R.id.active_imageview);
        ImageView p2Image = (ImageView) player2View.findViewById(R.id.active_imageview);

        p1Image.setImageDrawable(getDrawableForPokemon(getContext(), p1.getName()));
        p2Image.setImageDrawable(getDrawableForPokemon(getContext(), p2.getName()));

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

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.

}
