package com.pokemonbattlearena.android.fragment.chat;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pokemonbattlearena.android.util.PokemonUtils;
import com.pokemonbattlearena.android.R;
import com.pokemonbattlearena.android.activity.BaseActivity;

/**
 * Created by mitchcout on 10/22/2016.
 */

public class ChatHomeFragment extends Fragment{

    private static final String TAG = ChatHomeFragment.class.getSimpleName();
    private BaseActivity activity;
    private DatabaseReference root;

    private ImageButton sendMessage;
    private EditText editText;
    private ScrollView scroller;

    private String chatRoom;
    private String tempKey;
    private String chatMsg, chatUser;
    private String mUsername;

    private ChildEventListener mChildListener;
    private LayoutInflater layoutInflater;

    private OnChatLoadedListener mCallback;


    public ChatHomeFragment() {
        super();
    }

    public interface OnChatLoadedListener {
        void onChatLoaded();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
        try {
            mCallback = (ChatHomeFragment.OnChatLoadedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
            throw new ClassCastException(context.toString() + "must implement listener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chathome, container, false);
        SharedPreferences prefs = activity.getSharedPreferences(PokemonUtils.PREFS_KEY, Context.MODE_PRIVATE);
        chatRoom = getString(R.string.global);
        mUsername = prefs.getString(PokemonUtils.PROFILE_NAME_KEY, PokemonUtils.DEFAULT_NAME);
        layoutInflater = inflater;

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        sendMessage = (ImageButton) activity.findViewById(R.id.chat_send_button);
        editText = (EditText) activity.findViewById(R.id.chat_message_input);
        scroller = (ScrollView) activity.findViewById(R.id.chat_scroller);

        root = FirebaseDatabase.getInstance().getReference().child(chatRoom);

        //adds send button listener
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString();
                if(message.equals("")){
                    return;
                }

                //creates a unique message key for the database
                Map<String, Object> map = new HashMap<String, Object>();
                tempKey = "Message"+root.push().getKey();
                root.updateChildren(map);

                //adds the message to the database
                DatabaseReference messageRoot = root.child(tempKey);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("author",mUsername);
                map2.put("msg",message);

                messageRoot.updateChildren(map2);
                editText.setText("");
            }
        });

        //if enter key is pressed, auto-clicks the send button
        editText.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMessage.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        //updates the UI with messages
        mChildListener = root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                appendChat(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                appendChat(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mCallback.onChatLoaded();
    }

    @Override
    public void onStop() {
        super.onStop();

        root.removeEventListener(mChildListener);
    }

    /**
     * Adds database changes to chat UI
     * @param dataSnapshot
     */
    private void appendChat(DataSnapshot dataSnapshot){
        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()) {
            chatUser = (String) ((DataSnapshot)i.next()).getValue();
            chatMsg = (String) ((DataSnapshot)i.next()).getValue();

            createNewMessage(layoutInflater, (ViewGroup) activity.findViewById(R.id.chat_list), chatUser, chatMsg);
        }
    }

    /**
     * Creates a new chat_message item for the UI
     */
    private void createNewMessage(LayoutInflater inflater, ViewGroup container, String author, String msg){
        View chatMessage;
        if(mUsername != null && mUsername.equals(author)) {
            chatMessage = inflater.inflate(R.layout.chat_message_user, container, false);
        } else {
            chatMessage = inflater.inflate(R.layout.chat_message_other, container, false);
        }

        //get views
        TextView mAuthorView = (TextView) chatMessage.findViewById(R.id.chat_author);
        TextView mMessageView = (TextView) chatMessage.findViewById(R.id.chat_message);

        //set text
        mAuthorView.setText(author);
        mMessageView.setText(msg);

        container.addView(chatMessage);
        scroller.post(new Runnable() {
            @Override
            public void run() {
                scroller.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
