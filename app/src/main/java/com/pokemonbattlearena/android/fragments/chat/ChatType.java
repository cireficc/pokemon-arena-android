package com.pokemonbattlearena.android.fragments.chat;

/**
 * Created by mitchcout on 11/12/2016.
 */

public enum ChatType {
    GLOBAL("Global"),
    IN_GAME("In-Game");

    private String chatRoomType;

    ChatType (String chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public String getChatRoomType() {return chatRoomType; }
}
