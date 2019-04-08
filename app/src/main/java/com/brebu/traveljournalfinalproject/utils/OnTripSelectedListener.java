package com.brebu.traveljournalfinalproject.utils;

import android.widget.ImageButton;

public interface OnTripSelectedListener<T> {

    void onDeleteLongPressed(T trip, ImageButton imageButton);

    void onDeletePressed(T trip, ImageButton imageButton);

    void onIconPressed(T trip, ImageButton imageButton);

    void onTripLongPressed(T trip);

    void onTripSelected(T trip);

}
