package com.brebu.traveljournalfinalproject.fragments;

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
import android.widget.TextView;
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.recyclerview.FavouriteTripsAdapter;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.brebu.traveljournalfinalproject.room.DatabaseInitializer;
import com.brebu.traveljournalfinalproject.room.LocalDatabase;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;

import java.text.DateFormat;
import java.util.Locale;

public class FavouriteFragment extends Fragment implements OnTripSelectedListener<Trip>, Constants {

    private FavouriteTripsAdapter mAdapter;
    private FragmentActivity mFragmentContext;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recycler_view, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onDeleteLongPressed(final Trip trip, ImageButton imageButton) {

        DatabaseInitializer.getTripList().remove(trip);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                LocalDatabase.getTravelJournalDatabase(mFragmentContext).tripsDao().deleteTrip(trip);
            }
        });
        FirebaseRepository.getTrips().document(trip.getTripId()).update(TRIP_FAVOURITE,false);
        mAdapter.notifyDataSetChanged();
        if (DatabaseInitializer.getTripList().isEmpty()) {
            TextView mTextViewWelcome;
            mTextViewWelcome = mFragmentContext.findViewById(R.id.TextView_Welcome);
            mTextViewWelcome.setText("No favourite trips :(");
            mTextViewWelcome.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeletePressed(Trip trip, ImageButton imageButton) {
        Toast.makeText(mFragmentContext, "For delete press long", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onIconPressed(Trip trip, ImageButton imageButton) {
        Toast.makeText(mFragmentContext, "Please make favourite from home", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTripLongPressed(Trip trip) {
        Toast.makeText(mFragmentContext, "Please edit only from home", Toast.LENGTH_LONG).show();
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
        bundle.putString(TRIP_FAVOURITE, String.valueOf(true));
        displayTrip.setArguments(bundle);
        createDynamicFragment(displayTrip);
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentContext.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    private void initView(View view) {

        mFragmentContext = getActivity();

        RecyclerView recyclerViewTrips = view.findViewById(R.id.recycler_view_x);
        recyclerViewTrips.setLayoutManager(new LinearLayoutManager(mFragmentContext));
        mAdapter = new FavouriteTripsAdapter(DatabaseInitializer.getTripList(), mFragmentContext, this);
        recyclerViewTrips.setAdapter(mAdapter);
    }
}
