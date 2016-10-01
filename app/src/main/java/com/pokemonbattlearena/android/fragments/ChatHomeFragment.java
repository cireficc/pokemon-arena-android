package com.pokemonbattlearena.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokemonbattlearena.android.R;

/**
 * Created by droidowl on 9/25/16.
 */

public class ChatHomeFragment extends Fragment {

    public ChatHomeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chathome, container, false);
        return view;
    }
}
