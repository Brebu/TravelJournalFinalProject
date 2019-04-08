package com.brebu.traveljournalfinalproject.models;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.brebu.traveljournalfinalproject.room.DateConverter;
import com.brebu.traveljournalfinalproject.room.Users;

import java.util.Date;
import java.util.UUID;


@Entity(foreignKeys = @ForeignKey(entity = Users.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE))
public class Trip {

    private String tripDestination;

    @TypeConverters(DateConverter.class)
    private Date tripEndDate;

    private boolean tripFavourite;

    @PrimaryKey
    @NonNull
    private String tripId;

    private String tripImageFirestore;

    @Ignore
    private String tripImagePath;

    private String tripName;

    private int tripPrice;

    private float tripRating;

    @TypeConverters(DateConverter.class)
    private Date tripStartDate;

    private String tripType;

    @ColumnInfo(name = "user_id")
    private String userId;

    @Ignore
    public Trip(String tripName, String tripDestination, String tripType,
                int tripPrice, Date tripStartDate, Date tripEndDate, float tripRating,
                String tripImagePath, String tripImageFirestore, boolean tripFavourite) {
        this.tripId = UUID.randomUUID().toString();
        this.tripName = tripName;
        this.tripDestination = tripDestination;
        this.tripType = tripType;
        this.tripPrice = tripPrice;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
        this.tripRating = tripRating;
        this.tripImagePath = tripImagePath;
        this.tripImageFirestore = tripImageFirestore;
        this.tripFavourite = tripFavourite;
    }


    public Trip() {
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
    }

    public Date getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(Date tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripImageFirestore() {
        return tripImageFirestore;
    }

    public void setTripImageFirestore(String tripImageFirestore) {
        this.tripImageFirestore = tripImageFirestore;
    }

    public String getTripImagePath() {
        return tripImagePath;
    }

    public void setTripImagePath(String tripImagePath) {
        this.tripImagePath = tripImagePath;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public int getTripPrice() {
        return tripPrice;
    }

    public void setTripPrice(int tripPrice) {
        this.tripPrice = tripPrice;
    }

    public float getTripRating() {
        return tripRating;
    }

    public void setTripRating(float tripRating) {
        this.tripRating = tripRating;
    }

    public Date getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(Date tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isTripFavourite() {
        return tripFavourite;
    }

    public void setTripFavourite(boolean tripFavourite) {
        this.tripFavourite = tripFavourite;
    }
}
