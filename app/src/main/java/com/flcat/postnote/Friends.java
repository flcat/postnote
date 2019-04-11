package com.flcat.postnote;

public class Friends {

    public String email;
    public String userphoto;
    public String key;
    public String uid;

    public Friends() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserphoto() {
        return userphoto;
    }

    public void setUserphoto(String userphoto) {
        this.userphoto = userphoto;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
