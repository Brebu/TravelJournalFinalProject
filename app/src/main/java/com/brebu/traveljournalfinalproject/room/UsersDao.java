package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UsersDao {

    @Query("Select * from users")
    List<Users> getAllUsers();

    @Insert
    void insertUser(Users user);

    @Delete
    void deletetUser(Users user);

    @Update
    void updatetUser(Users user);

}
