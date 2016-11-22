package com.pokemonbattlearena.android.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.pokemonbattlearena.android.R;

/**
 * Created by Spencer Amann on 11/22/16.
 */
public class NameFragment extends Fragment {

    private EditText mNameEditText;
    private String mName;
    private Button mSaveButton;

    private SharedPreferences mPreferences;

    public NameFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_name, container, false);
        mPreferences = getActivity().getSharedPreferences("Pokemon Battle Prefs", Context.MODE_PRIVATE);
        mNameEditText = (EditText) view.findViewById(R.id.name_edit_text);
        mSaveButton = (Button) view.findViewById(R.id.save_name_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileName();
            }
        });
        return view;
    }

    private void saveProfileName() {
        mName = mNameEditText.getText().toString().toLowerCase().trim();
        if (!mName.isEmpty()) {
            SharedPreferences.Editor edit = mPreferences.edit();
            edit.putString("default_name", mName);
            edit.apply();
            edit.commit();
        }
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
    }
}