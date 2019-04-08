package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users")
public class Users {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String userId;

    public Users(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

}
