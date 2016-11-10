package com.pokemonbattlearena.android.fragments.battle;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pokemonbattlearena.android.ApplicationPhase;
import com.pokemonbattlearena.android.PokemonBattleApplication;
import com.pokemonbattlearena.android.R;

/**
 * Created by Mitch Couturier
 */
public class MainMenuFragment extends Fragment {

    PokemonBattleApplication mApplication = PokemonBattleApplication.getInstance();
    private final static String TAG = MainMenuFragment.class.getSimpleName();

    private Button mBattleNowButton;
    private Button mBattleFriendButton;
    private Button mBattleAIButton;

    private OnMenuFragmentTouchListener mCallback;
    private OnFragmentInteractionListener mListener;

    public MainMenuFragment() {
        // Required empty public constructor
    }

    public interface OnMenuFragmentTouchListener {
        void onBattleNowClicked();
        void onAiBattleClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        mBattleNowButton = (Button) view.findViewById(R.id.battle_now_button);
        mBattleFriendButton = (Button) view.findViewById(R.id.battle_friend_button);
        mBattleAIButton = (Button) view.findViewById(R.id.battle_ai_button);

        mBattleNowButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //on button pressed
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBattleNowButton.setBackgroundResource(R.drawable.ic_battle_now_button_clicked);
                    return true;
                }
                //on button release
                else {
                    mCallback.onBattleNowClicked();
                    mBattleNowButton.setBackgroundResource(R.drawable.ic_battle_now_button);
                    return false;
                }
            }
        });
        mBattleFriendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //on button pressed
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBattleFriendButton.setBackgroundResource(R.drawable.ic_battle_friend_button_clicked);
                    return true;
                }
                //on button release
                else {
                    mBattleFriendButton.setBackgroundResource(R.drawable.ic_battle_friend_button);
                    return false;
                }
            }
        });
        mBattleAIButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //on button pressed
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBattleAIButton.setBackgroundResource(R.drawable.ic_battle_ai_button_clicked);
                    return true;
                }
                //on button release
                else {
                    mCallback.onAiBattleClicked();
                    mBattleAIButton.setBackgroundResource(R.drawable.ic_battle_ai_button);
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (MainMenuFragment.OnMenuFragmentTouchListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
