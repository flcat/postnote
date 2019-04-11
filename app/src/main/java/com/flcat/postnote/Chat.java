package com.flcat.postnote;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    public Map<String,Boolean> users = new HashMap<>(); //채팅방 유저 목록
    //public Map<String,Comment> comments = new HashMap<>(); //대화 내용

        public String Uid;
        public String text;
        public String email;



    public String chatTime;
    public String friendUid;
    public String userphoto;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChatTime() { return chatTime; }

    public void setChatTime(String chatTime) { this.chatTime = chatTime; }

    public String getUserphoto() {
        return userphoto;
    }

    public void setUserphoto(String userphoto) {
        this.userphoto = userphoto;
    }
}