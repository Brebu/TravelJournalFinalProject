package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.brebu.traveljournalfinalproject.models.Trip;

@Database(entities = {Users.class, Trip.class}, version = 2, exportSchema = false)
public abstract class TravelJournalDatabase extends RoomDatabase {

    private static TravelJournalDatabase INSTANCE;
    private static String DATABASE_NAME = "acr";

    public abstract UsersDao usersDao();
    public abstract TripDao tripsDao();

    public static TravelJournalDatabase getTravelJournalDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    TravelJournalDatabase.class,
                    DATABASE_NAME).build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }

}
