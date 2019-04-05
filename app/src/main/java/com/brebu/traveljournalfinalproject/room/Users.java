package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users")
public class Users {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String userId;

    public Users(String userId) {
        this.userId = userId;
    }

    @Ignore
    public Users() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
