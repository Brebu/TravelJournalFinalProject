package com.brebu.traveljournalfinalproject.utils;

import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnTripSelectedListener {

    void onTripSelected(DocumentSnapshot trip);

    void onTripLongPressed(DocumentSnapshot trip);

    void onIconPressed(DocumentSnapshot trip, ImageButton imageButton);

    void onDeletePressed(DocumentSnapshot trip, ImageButton imageButton);

    void onDeleteLongPressed(DocumentSnapshot trip, ImageButton imageButton);

}
