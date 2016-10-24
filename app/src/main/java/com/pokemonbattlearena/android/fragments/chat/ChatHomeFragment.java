package com.pokemonbattlearena.android.fragments.chat;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pokemonbattlearena.android.BottomBarActivity;
import com.pokemonbattlearena.android.R;

/**
 * Created by mitchcout on 10/22/2016.
 */

public class ChatHomeFragment extends Fragment{

    private BottomBarActivity activity;
    private DatabaseReference root;

    private ImageButton sendMessage;
    private EditText editText;
    private TextView chatList;
    private String chatRoom;
    private String tempKey;
    private String chatMsg, chatUser;
    private String mUsername;

    private ChildEventListener mChildListener;

    public ChatHomeFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BottomBarActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chathome, container, false);

        chatRoom = "Global";

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        sendMessage = (ImageButton) activity.findViewById(R.id.chat_send_button);
        editText = (EditText) activity.findViewById(R.id.chat_message_input);
        chatList = (TextView) activity.findViewById(R.id.chat_list);

        setupUsername();

        root = FirebaseDatabase.getInstance().getReference().child(chatRoom);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creates a unique message key for the database
                Map<String, Object> map = new HashMap<String, Object>();
                tempKey = "Message"+root.push().getKey();
                root.updateChildren(map);

                //adds the message to the database
                DatabaseReference messageRoot = root.child(tempKey);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("author",mUsername);
                map2.put("msg",editText.getText().toString());

                messageRoot.updateChildren(map2);

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
    }

    @Override
    public void onStop() {
        super.onStop();

        root.removeEventListener(mChildListener);
    }

    private void setupUsername() {
        if (mUsername == null) {
            Random r = new Random();
            // Assign a random user name if we don't have one saved.
            mUsername = "PokeMaster" + r.nextInt(100000);
        }
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

            chatList.append(chatUser + " : " + chatMsg + " \n");
            editText.setText("");
        }
    }
}
