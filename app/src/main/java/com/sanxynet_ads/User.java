package com.sanxynet_ads;

import android.content.Context;
import android.content.SharedPreferences;


public class User {

    Context context;

    public void removeUser(){
        sharedPreferences.edit().clear().apply();
    }

    public String getUsername() {
        Username = sharedPreferences.getString("userdata", "");
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
        sharedPreferences.edit().putString("userdata", username).apply();
    }

    private String Username;

    private SharedPreferences sharedPreferences;

    public  User(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
    }
}
