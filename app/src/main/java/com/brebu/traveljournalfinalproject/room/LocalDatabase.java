package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.brebu.traveljournalfinalproject.models.Trip;

@Database(entities = {Users.class, Trip.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {

    private static LocalDatabase INSTANCE;

    public static void destroyInstance(){
        INSTANCE = null;
    }

    public static LocalDatabase getTravelJournalDatabase(Context context) {
        if (INSTANCE == null) {
            String DATABASE_NAME = "db_3";
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    LocalDatabase.class,
                    DATABASE_NAME).build();
        }
        return INSTANCE;
    }

    public abstract TripDao tripsDao();

    public abstract UsersDao usersDao();

}
