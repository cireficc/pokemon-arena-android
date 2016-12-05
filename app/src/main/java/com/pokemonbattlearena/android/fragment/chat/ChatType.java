package com.pokemonbattlearena.android.fragment.chat;

/**
 * Created by mitchcout on 11/12/2016.
 */

public enum ChatType {
    GLOBAL("Global Chat"),
    IN_GAME("Battle Chat");

    private String chatRoomType;

    ChatType (String chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public String getChatRoomType() {return chatRoomType; }
}
