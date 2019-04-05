package com.brebu.traveljournalfinalproject.fragment;


import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.recyclerview.FavouriteTripsAdapter;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FavouriteFragment extends Fragment implements OnTripSelectedListener<Trip>, Constants {

    private RecyclerView mRecyclerViewMovies;
    private FavouriteTripsAdapter mAdapter;
    private FragmentActivity mFragmentActivity;
    private List<Movie> mMovieList;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_navigation_drawer, container, false);
        mFragmentActivity = getActivity();
        if (mFragmentActivity != null) {
            mFragmentActivity.setTitle("Favourite Trips");
        }
        initView(view);
        return view;
    }

    private void initView(View view) {
        mFragmentActivity = getActivity();
        mRecyclerViewMovies = view.findViewById(R.id.recycler_view_x);
        mRecyclerViewMovies.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mMovieList = new ArrayList<>();
        mAdapter = new FavouriteTripsAdapter(HomeFragment.mTripList, mFragmentActivity,this);
        mRecyclerViewMovies.setAdapter(mAdapter);
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    @Override
    public void onTripSelected(Trip trip) {
        ViewTripFavouriteFragment displayTrip = new ViewTripFavouriteFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TRIP_NAME, trip.getTripName());
        bundle.putString(TRIP_DESTINATION, trip.getTripDestination());
        bundle.putInt(TRIP_PRICE, trip.getTripPrice());
        bundle.putFloat(TRIP_RATING, trip.getTripRating());
        bundle.putString(START_DATE,
                DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getTripStartDate()));
        bundle.putString(END_DATE,
                DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getTripEndDate()));
        bundle.putString(FIRESTORE_PATH, trip.getTripImageFirestore());
        bundle.putString("tripFavourite", String.valueOf(true));
        displayTrip.setArguments(bundle);
        createDynamicFragment(displayTrip);
    }

    @Override
    public void onTripLongPressed(Trip trip) {

    }

    @Override
    public void onIconPressed(Trip trip, ImageButton imageButton) {

    }

    @Override
    public void onDeletePressed(Trip trip, ImageButton imageButton) {
        HomeFragment.mTripList.remove(trip);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDeleteLongPressed(Trip trip, ImageButton imageButton) {

    }
}
