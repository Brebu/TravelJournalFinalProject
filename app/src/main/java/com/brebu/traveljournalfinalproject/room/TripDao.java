package com.brebu.traveljournalfinalproject.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import com.brebu.traveljournalfinalproject.models.Trip;

import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface TripDao {

    @Query("DELETE FROM trip WHERE tripId = :tripId")
    void deleteByTripId(String tripId);

    @Delete
    void deleteTrip(Trip trip);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("Select * from trip where user_id=:mMail")
    List<Trip> getAllTrips(String mMail);

    @Insert
    void insertTrip(Trip trip);

    @Update
    void updateTrip(Trip trip);
}
