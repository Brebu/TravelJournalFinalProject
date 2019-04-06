package com.brebu.traveljournalfinalproject.fragment;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.SignInActivity;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.recyclerview.FavouriteTripsAdapter;

import com.brebu.traveljournalfinalproject.room.DatabaseInitializer;
import com.brebu.traveljournalfinalproject.room.TravelJournalDatabase;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;

import java.util.Locale;

public class FavouriteFragment extends Fragment implements OnTripSelectedListener<Trip>, Constants {

    private RecyclerView mRecyclerViewMovies;
    private FavouriteTripsAdapter mAdapter;
    private FragmentActivity mFragmentContext;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;
    private CollectionReference mTrips;
    private String mMail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_navigation_drawer, container, false);
        initView(view);
        initFirebase();

        return view;
    }

    private void initView(View view) {

        mFragmentContext = getActivity();
        if (mFragmentContext != null) {
            mFragmentContext.setTitle("Favourite Trips");
        }

        mRecyclerViewMovies = view.findViewById(R.id.recycler_view_x);
        mRecyclerViewMovies.setLayoutManager(new LinearLayoutManager(mFragmentContext));
        mAdapter = new FavouriteTripsAdapter(DatabaseInitializer.sTripList, mFragmentContext,this);
        mRecyclerViewMovies.setAdapter(mAdapter);
    }

    private void initFirebase() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(mFragmentContext, SignInActivity.class));
            mFragmentContext.finish();
        } else {
            mMail = mFirebaseUser.getEmail();
            if (mMail != null) {
                mFirebaseFirestore = FirebaseFirestore.getInstance();
                mTrips = mFirebaseFirestore.collection(mMail);
            }
        }
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    @Override
    public void onTripSelected(Trip trip) {
        ViewTripFragment displayTrip = new ViewTripFragment();
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
        Toast.makeText(mFragmentContext, "Please edit only from home", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onIconPressed(Trip trip, ImageButton imageButton) {
        Toast.makeText(mFragmentContext, "Please make favourite from home", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeletePressed(Trip trip, ImageButton imageButton) {
        Toast.makeText(mFragmentContext, "For delete press long", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteLongPressed(final Trip trip, ImageButton imageButton) {
        DatabaseInitializer.sTripList.remove(trip);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TravelJournalDatabase.getTravelJournalDatabase(mFragmentContext).tripsDao().deleteTrip(trip);
            }
        });
        mFirebaseFirestore.collection(mMail).document(trip.getTripId()).update("tripFavourite",false);

        mAdapter.notifyDataSetChanged();
    }
}
