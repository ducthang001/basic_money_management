package com.app.moneyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.app.moneyapp.Authentication.RegisterActivity;
import com.app.moneyapp.Models.User;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Utils {
    private static final String TAG = "Utils";

    private Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public void addUserToSharedPreferences (User user) {
        Log.d(TAG, "addUserToSharedPreferences: adding " + user.toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        editor.putString("user", gson.toJson(user));
        editor.apply();

    }

    public User isUserLoggedIn() {
        Log.d(TAG, "isUserLoggedIn: started");
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        Type type = new TypeToken<User>(){}.getType();
        return gson.fromJson(sharedPreferences.getString("user", null), type);

    }

    public void signOutUser() {
        Log.d(TAG, "signOutUser: started");
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged_in_user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("user");
        editor.commit();
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

    }
}
