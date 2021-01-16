package com.learnado.reminder;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "login")
public class UsernamePassword {

    @PrimaryKey
    @NonNull
    String usename;
    @NonNull
    String password;
    int isloggedIn = 0;


}
