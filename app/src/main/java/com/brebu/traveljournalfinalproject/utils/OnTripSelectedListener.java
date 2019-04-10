package com.brebu.traveljournalfinalproject.utils;

import android.widget.ImageButton;

public interface OnTripSelectedListener<T> {

    void onIconPressed(T trip, ImageButton imageButton);

    void onTripLongPressed(T trip);

    void onTripSelected(T trip);

}
