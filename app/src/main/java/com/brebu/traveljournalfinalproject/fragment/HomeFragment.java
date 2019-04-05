package com.brebu.traveljournalfinalproject.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.brebu.traveljournalfinalproject.AddOrModifyTrip;
import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.SignInActivity;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.recyclerview.TripsAdapter;
import com.brebu.traveljournalfinalproject.room.TravelJournalDatabase;
import com.brebu.traveljournalfinalproject.room.Users;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.brebu.traveljournalfinalproject.utils.BitmapProcess.bitmapToData;

public class HomeFragment extends Fragment implements OnTripSelectedListener<DocumentSnapshot>, Constants {


    //Constants
    private static final String TAG = "HomeFragment";

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private CollectionReference mTrips;
    private FirebaseStorage mFirebaseStorage;
    private Query mQuery;

    private RecyclerView mRecyclerViewTrips;
    private TripsAdapter mAdapter;
    private FragmentActivity mFragmentActivity;
    private String mMail;
    public static List<Trip> mTripList;

    private TravelJournalDatabase mTravelJournalDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_navigation_drawer, container, false);
        mFragmentActivity = getActivity();
        mFragmentActivity.setTitle("Home fragment");
        initFirebase();
        initView(view);
        new LoadItemAsync().execute();
        mTravelJournalDatabase =
                TravelJournalDatabase.getTravelJournalDatabase(getActivity());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initView(View view) {
        mRecyclerViewTrips = view.findViewById(R.id.recycler_view_x);
        mRecyclerViewTrips.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mQuery = mFirebaseFirestore.collection(mMail)
                .orderBy("tripStartDate", Query.Direction.DESCENDING)
                .limit(DISPLAY_LIMIT);
        mAdapter = new TripsAdapter(mQuery, this, mFragmentActivity);
        mRecyclerViewTrips.setAdapter(mAdapter);
        mAdapter.startListening();
        mAdapter = new TripsAdapter(mQuery, this, mFragmentActivity);
    }


    private void initFirebase() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(mFragmentActivity, SignInActivity.class));
            mFragmentActivity.finish();
        } else {
            mMail = mFirebaseUser.getEmail();
            if (mMail != null) {
                mFirebaseFirestore = FirebaseFirestore.getInstance();
                mTrips = mFirebaseFirestore.collection(mMail);
            }
        }
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    @Override
    public void onTripSelected(DocumentSnapshot trip) {

        ViewTripFragment displayTrip = new ViewTripFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TRIP_NAME, trip.getString(TRIP_NAME));
        bundle.putString(TRIP_DESTINATION, trip.getString(TRIP_DESTINATION));
        bundle.putString(TRIP_PRICE, String.valueOf(trip.getDouble(TRIP_PRICE)));
        bundle.putString(TRIP_RATING, String.valueOf(trip.getDouble(TRIP_RATING)));
        bundle.putString(START_DATE,
                DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getDate(START_DATE)));
        bundle.putString(END_DATE,
                DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getDate(END_DATE)));
        bundle.putString(FIRESTORE_PATH, trip.getString(FIRESTORE_PATH));
        bundle.putString("tripFavourite", String.valueOf(trip.getBoolean("tripFavourite")));
        displayTrip.setArguments(bundle);
        createDynamicFragment(displayTrip);
    }

    @Override
    public void onTripLongPressed(DocumentSnapshot trip) {
        Intent intent = new Intent(mFragmentActivity, AddOrModifyTrip.class);
        intent.putExtra(TRIP_UUID, trip.getId());
        intent.putExtra(USER_ID, mMail);
        startActivityForResult(intent, EDIT_TRIP);
    }

    @Override
    public void onIconPressed(final DocumentSnapshot trip, ImageButton imageButton) {

        String tripId = trip.getId();
        final Trip tripWithId = trip.toObject(Trip.class);
        tripWithId.setUserId(mMail);

        if (!(boolean) trip.get("tripFavourite")) {
            mTrips.document(tripId).update("tripFavourite", true);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    boolean contain = false;

                    for (Trip trip : mTravelJournalDatabase.tripsDao().getAllTrips()) {
                        if (trip.getUserId().equals(tripWithId.getTripId())) {
                            contain = true;
                        }
                    }

                    if (!contain) {
                        mTravelJournalDatabase.tripsDao().insertTrip(tripWithId);
                    }
                    
                    new LoadItemAsync().execute();
                }
            });
            Toast.makeText(mFragmentActivity, "Trip added to favourites!",
                    Toast.LENGTH_SHORT).show();
        } else {
            mTrips.document(tripId).update("tripFavourite", false);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    mTravelJournalDatabase.tripsDao().deleteTrip(tripWithId);
                    new LoadItemAsync().execute();
                }
            });
            Toast.makeText(mFragmentActivity, "Trip removed from favourites!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeletePressed(DocumentSnapshot trip, ImageButton imageButton) {

        Toast.makeText(mFragmentActivity, mTripList.size() + "dsa", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDeleteLongPressed(DocumentSnapshot trip, ImageButton imageButton) {
        final String tripId = trip.getId();
        mTrips.document(tripId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                StorageReference storageRef = mFirebaseStorage.getReference();
                StorageReference imgStorageRef =
                        storageRef.child(mMail).child(tripId + ".jpg");
                imgStorageRef.delete();
                Toast.makeText(mFragmentActivity, "The item was removed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NEW_TRIP) {
            if (resultCode == Activity.RESULT_OK) {

                assert data != null;
                String tripName = data.getStringExtra(TRIP_NAME);
                String tripDestination = data.getStringExtra(TRIP_DESTINATION);
                String tripType = data.getStringExtra(TRIP_TYPE);
                String tripStart = data.getStringExtra(START_DATE);
                String tripEnd = data.getStringExtra(END_DATE);
                String tripPrice = data.getStringExtra(TRIP_PRICE);
                String tripRating = data.getStringExtra(TRIP_RATING);
                String tripFirestorePath = data.getStringExtra(FIRESTORE_PATH);
                final String tripPhotoPath = data.getStringExtra(PHOTO_PATH);
                File image = new File(tripPhotoPath);
                Uri tempUri = Uri.fromFile(image);

                int convertedPrice = Integer.parseInt(tripPrice) * 50;
                float convertedRating = Float.parseFloat(tripRating);
                Date tempStartDate = null;
                Date tempEndDate = null;
                try {
                    tempStartDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripStart);
                    tempEndDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripEnd);
                } catch (ParseException e) {
                    e.getStackTrace();
                }


                byte[] dataBitmap = bitmapToData(tempUri, mFragmentActivity);

                final Trip trip = new Trip(tripName, tripDestination, tripType, convertedPrice,
                        tempStartDate, tempEndDate, convertedRating, tempUri.toString(),
                        tripFirestorePath, false);

                StorageReference storageRef = mFirebaseStorage.getReference();
                StorageReference imgStorageRef =
                        storageRef.child(mMail).child(trip.getTripId() + ".jpg");

                UploadTask uploadTask = imgStorageRef.putBytes(dataBitmap);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                trip.setTripImageFirestore(uri.toString());
                                trip.setUserId(mMail);
                                mTrips.document(trip.getTripId()).set(trip);
                                Toast.makeText(mFragmentActivity, "Trip added successfully!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        } else if (requestCode == EDIT_TRIP) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                String tripName = data.getStringExtra(TRIP_NAME);
                String tripDestination = data.getStringExtra(TRIP_DESTINATION);
                String tripType = data.getStringExtra(TRIP_TYPE);
                String tripStart = data.getStringExtra(START_DATE);
                String tripEnd = data.getStringExtra(END_DATE);
                String tripPrice = data.getStringExtra(TRIP_PRICE);
                String tripRating = data.getStringExtra(TRIP_RATING);
                final String tripPhotoPath = data.getStringExtra(PHOTO_PATH);
                File image = new File(tripPhotoPath);
                Uri tempUri = Uri.fromFile(image);

                int convertedPrice = Integer.parseInt(tripPrice) * 50;
                float convertedRating = Float.parseFloat(tripRating);
                Date tempStartDate = null;
                Date tempEndDate = null;
                try {
                    tempStartDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripStart);
                    tempEndDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripEnd);
                } catch (ParseException e) {
                    e.getStackTrace();
                }

                final String tripIdFromFirestore = data.getStringExtra(TRIP_ID);

                mTrips.document(tripIdFromFirestore).update(
                        TRIP_NAME, tripName,
                        TRIP_DESTINATION, tripDestination,
                        TRIP_TYPE, tripType,
                        TRIP_PRICE, convertedPrice,
                        START_DATE, tempStartDate,
                        END_DATE, tempEndDate,
                        TRIP_RATING, convertedRating
                );

                if (AddOrModifyTrip.mPictureChanged) {

                    Toast.makeText(mFragmentActivity, "Picture changed", Toast.LENGTH_LONG).show();

                    byte[] dataBitmap = bitmapToData(tempUri, mFragmentActivity);

                    StorageReference storageRef = mFirebaseStorage.getReference();
                    StorageReference imgStorageRef =
                            storageRef.child(mMail).child(tripIdFromFirestore + ".jpg");

                    UploadTask uploadTask = imgStorageRef.putBytes(dataBitmap);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mTrips.document(tripIdFromFirestore).update(
                                            PHOTO_PATH, tripPhotoPath,
                                            FIRESTORE_PATH, uri.toString()
                                    );
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    private class LoadItemAsync extends AsyncTask<Void, Void, List<Trip>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TravelJournalDatabase.getTravelJournalDatabase(mFragmentActivity);
        }

        @Override
        protected List<Trip> doInBackground(Void... voids) {
            TravelJournalDatabase database =
                    TravelJournalDatabase.getTravelJournalDatabase(mFragmentActivity);

            boolean contain = false;

            for (Users user : database.usersDao().getAllUsers()) {
                if (user.getUserId().equals(mMail)) {
                    contain = true;
                }
            }

            if (!contain) {
                database.usersDao().insertUser(new Users(mMail));
            }

            return database.tripsDao().getAllTrips();
        }

        @Override
        protected void onPostExecute(List<Trip> items) {
            super.onPostExecute(items);
            mTripList = items;
        }
    }
}
