package com.brebu.traveljournalfinalproject.models;

import java.util.Date;
import java.util.UUID;

public class Trip {

    private String tripId;
    private String tripName;
    private String tripDestination;
    private String tripType;
    private int tripPrice;
    private Date tripStartDate;
    private Date tripEndDate;
    private float tripRating;
    private String tripImagePath;
    private String tripImageFirestore;
    private boolean tripFavourite;

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

    public String getTripId() {
        return tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public int getTripPrice() {
        return tripPrice;
    }

    public void setTripPrice(int tripPrice) {
        this.tripPrice = tripPrice;
    }

    public Date getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(Date tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public Date getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(Date tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public float getTripRating() {
        return tripRating;
    }

    public void setTripRating(float tripRating) {
        this.tripRating = tripRating;
    }

    public String getTripImagePath() {
        return tripImagePath;
    }

    public void setTripImagePath(String tripImagePath) {
        this.tripImagePath = tripImagePath;
    }

    public String getTripImageFirestore() {
        return tripImageFirestore;
    }

    public void setTripImageFirestore(String tripImageFirestore) {
        this.tripImageFirestore = tripImageFirestore;
    }

    public boolean isTripFavourite() {
        return tripFavourite;
    }

    public void setTripFavourite(boolean tripFavourite) {
        this.tripFavourite = tripFavourite;
    }
}
