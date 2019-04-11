package com.brebu.traveljournalfinalproject.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.AddOrModifyTrip;
import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.recyclerview.TripsAdapter;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.brebu.traveljournalfinalproject.room.DatabaseInitializer;
import com.brebu.traveljournalfinalproject.room.LocalDatabase;
import com.brebu.traveljournalfinalproject.room.Users;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.brebu.traveljournalfinalproject.MainActivity.HANDLER;
import static com.brebu.traveljournalfinalproject.room.LocalDatabase.getTravelJournalDatabase;
import static com.brebu.traveljournalfinalproject.utils.BitmapProcess.bitmapToData;

public class HomeFragment extends Fragment implements OnTripSelectedListener<DocumentSnapshot>, Constants {

    private FragmentActivity mFragmentActivity;
    private RecyclerView mRecyclerViewItems;
    private Fragment mFragmentViewTripFragment;
    private TripsAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentActivity = getActivity();

        Query query = null;

        Bundle bundle = this.getArguments();

        if (bundle != null) {

            String order = bundle.getString("order");
            String direction = bundle.getString("direction");

            if (direction != null && order != null) {

                switch (direction) {

                    case "ASCENDING":
                        query = FirebaseRepository.getFirebaseFirestore().collection(FirebaseRepository.getMail())
                                .orderBy(order, Query.Direction.ASCENDING)
                                .limit(DISPLAY_LIMIT);
                        break;

                    case "DESCENDING":
                        query = FirebaseRepository.getFirebaseFirestore().collection(FirebaseRepository.getMail())
                                .orderBy(order, Query.Direction.DESCENDING)
                                .limit(DISPLAY_LIMIT);
                        break;
                }
            }
        }

        mAdapter = new TripsAdapter(query, this, mFragmentActivity);
        mAdapter.startListening();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TRIP) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                final String tripName = data.getStringExtra(TRIP_NAME);
                final String tripDestination = data.getStringExtra(TRIP_DESTINATION);
                final String tripType = data.getStringExtra(TRIP_TYPE);
                final String tripStart = data.getStringExtra(START_DATE);
                final String tripEnd = data.getStringExtra(END_DATE);
                final String tripPrice = data.getStringExtra(TRIP_PRICE);
                final String tripRating = data.getStringExtra(TRIP_RATING);
                final String tripPhotoPath = data.getStringExtra(PHOTO_PATH);
                final String tripIdFromFirestore = data.getStringExtra(TRIP_ID);
                File image = new File(tripPhotoPath);
                Uri tempUri = Uri.fromFile(image);

                final int convertedPrice = Integer.parseInt(tripPrice) * 50;
                final float convertedRating = Float.parseFloat(tripRating);


                FirebaseRepository.getFirebaseFirestore().collection(FirebaseRepository.getMail())
                        .document(tripIdFromFirestore).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Trip editedTrip = documentSnapshot.toObject(Trip.class);
                                if (editedTrip != null) {
                                    editedTrip.setTripName(tripName);
                                    editedTrip.setTripDestination(tripDestination);
                                    editedTrip.setTripType(tripType);
                                    editedTrip.setTripPrice(convertedPrice);

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

                                    editedTrip.setTripStartDate(tempStartDate);
                                    editedTrip.setTripEndDate(tempEndDate);
                                    editedTrip.setTripRating(convertedRating);
                                    editedTrip.setUserId(FirebaseRepository.getMail());

                                    FirebaseRepository.getTrips().document(tripIdFromFirestore).set(editedTrip);
                                }
                                editLocalTrip(editedTrip);
                            }
                        });


                if (AddOrModifyTrip.mPictureChanged) {

                    Toast.makeText(mFragmentActivity, "Picture changed", Toast.LENGTH_LONG).show();

                    byte[] dataBitmap = bitmapToData(tempUri, mFragmentActivity);

                    StorageReference storageRef =
                            FirebaseRepository.getFirebaseStorage().getReference();
                    StorageReference imgStorageRef =
                            storageRef.child(FirebaseRepository.getMail()).child(tripIdFromFirestore + ".jpg");

                    UploadTask uploadTask = imgStorageRef.putBytes(dataBitmap);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {

                                    FirebaseRepository.getFirebaseFirestore().collection(FirebaseRepository.getMail())
                                            .document(tripIdFromFirestore).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Trip editedTrip = documentSnapshot.toObject(Trip.class);
                                                    if (editedTrip != null) {
                                                        editedTrip.setTripName(tripName);
                                                        editedTrip.setTripDestination(tripDestination);
                                                        editedTrip.setTripImagePath(tripPhotoPath);
                                                        editedTrip.setTripImageFirestore(uri.toString());
                                                        editedTrip.setUserId(FirebaseRepository.getMail());
                                                        FirebaseRepository.getTrips().document(tripIdFromFirestore).set(editedTrip);
                                                    }
                                                    editLocalTrip(editedTrip);
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recycler_view, container, false);

        initView(view);
        checkForTrips();

        return view;
    }

    @Override
    public void onIconPressed(final DocumentSnapshot trip, ImageButton imageButton) {

        final String tripId = trip.getId();

        final Trip currentTrip = trip.toObject(Trip.class);

        if (currentTrip != null) {

            currentTrip.setUserId(FirebaseRepository.getMail());

        }

        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {

                List<Users> usersList = getTravelJournalDatabase(mFragmentActivity).usersDao().getAllUsers();

                boolean containUser = false;

                for (Users user : usersList) {

                    if (user.getUserId().equals(FirebaseRepository.getMail())) {

                        containUser = true;

                    }
                }

                if (!containUser) {

                    getTravelJournalDatabase(mFragmentActivity).usersDao().insertUser(new Users(FirebaseRepository.getMail()));

                }

                List<Trip> databaseTripsList = getTravelJournalDatabase(mFragmentActivity).tripsDao().getAllTrips(FirebaseRepository.getMail());

                boolean containTrip = false;

                for (Trip t : databaseTripsList) {

                    if (currentTrip != null && t.getTripId().equals(currentTrip.getTripId())) {

                        containTrip = true;

                    }

                }

                if (!containTrip) {

                    FirebaseRepository.getTrips().document(tripId).update("tripFavourite", true);

                    DatabaseInitializer.addTrip(getTravelJournalDatabase(mFragmentActivity), currentTrip);
                    DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(mFragmentActivity));

                    HANDLER.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(mFragmentActivity, "Trip added to favourites!", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {

                    FirebaseRepository.getTrips().document(tripId).update("tripFavourite", false);

                    getTravelJournalDatabase(mFragmentActivity).tripsDao().deleteTrip(currentTrip);
                    DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(mFragmentActivity));

                    HANDLER.post(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(mFragmentActivity, "Trip removed from favourites!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


    }

    @Override
    public void onTripLongPressed(DocumentSnapshot trip) {
        Intent intent = new Intent(mFragmentActivity, AddOrModifyTrip.class);
        intent.putExtra(TRIP_UUID, trip.getId());
        intent.putExtra(USER_ID, FirebaseRepository.getMail());
        startActivityForResult(intent, EDIT_TRIP);
    }

    @Override
    public void onTripSelected(DocumentSnapshot trip) {

        Bundle bundle = new Bundle();
        bundle.putString(TRIP_NAME, trip.getString(TRIP_NAME));
        bundle.putString(TRIP_DESTINATION, trip.getString(TRIP_DESTINATION));

        Double price = trip.getDouble(TRIP_PRICE);
        int convertedPrice = 0;
        if (price != null) {
            convertedPrice = price.intValue();
        }
        bundle.putInt(TRIP_PRICE, convertedPrice);

        Double rating = trip.getDouble(TRIP_RATING);
        float convertedRating = 0.0f;

        if (rating != null) {

            convertedRating = rating.floatValue();

        }

        bundle.putFloat(TRIP_RATING, convertedRating);

        bundle.putString(START_DATE, DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getDate(START_DATE)));
        bundle.putString(END_DATE, DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(trip.getDate(END_DATE)));
        bundle.putString(FIRESTORE_PATH, trip.getString(FIRESTORE_PATH));
        bundle.putString(TRIP_FAVOURITE, String.valueOf(trip.getBoolean(TRIP_FAVOURITE)));
        mFragmentViewTripFragment.setArguments(bundle);

        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, mFragmentViewTripFragment);
        fragmentTransaction.addToBackStack("View").commit();
    }

    private void checkForTrips() {

        FirebaseRepository.getTrips().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {

                if (documentSnapshots.isEmpty()) {

                    createDynamicFragment(new EmptyFragment());

                }
            }
        });
    }

    private void createDynamicFragment(Fragment fragment) {

        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.commit();

    }

    private void editLocalTrip(final Trip trip) {

        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {

                trip.setTripFavourite(false);
                LocalDatabase.getTravelJournalDatabase(mFragmentActivity).tripsDao().updateTrip(trip);
                DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(mFragmentActivity));

            }
        });
    }

    private void initView(View view) {

        mFragmentViewTripFragment = new ViewTripFragment();
        mRecyclerViewItems = view.findViewById(R.id.recycler_view_x);
        mRecyclerViewItems.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mRecyclerViewItems.setAdapter(mAdapter);
        setItemHelper(mAdapter);

    }

    private void setItemHelper(final TripsAdapter adapter) {

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            Drawable background;
            boolean initiated;
            Drawable xMark;

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int xMarkTop = itemView.getTop() + (itemView.getHeight() - xMark.getIntrinsicHeight()) / 2;
                int xMarkBottom = xMarkTop + xMark.getIntrinsicHeight();
                int xMarkLeft = itemView.getRight() - (itemView.getWidth() - xMark.getIntrinsicWidth()) / 6;
                int xMarkRight = xMarkLeft + xMark.getIntrinsicWidth();


                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                xMark.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mFragmentActivity);
                builder.setTitle("Attention!");
                builder.setMessage("Are you sure to delete?");


                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int position = viewHolder.getAdapterPosition();
                        final Trip currentTrip = adapter.getSnapshot(position).toObject(Trip.class);
                        if (currentTrip != null) {
                            FirebaseRepository.getTrips().document(currentTrip.getTripId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    StorageReference storageRef =
                                            FirebaseRepository.getFirebaseStorage().getReference();
                                    StorageReference imgStorageRef =
                                            storageRef.child(FirebaseRepository.getMail()).child(currentTrip.getTripId() + ".jpg");
                                    imgStorageRef.delete();
                                    Toast.makeText(mFragmentActivity, "The trip was removed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        checkForTrips();

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (currentTrip != null) {
                                    LocalDatabase.getTravelJournalDatabase(mFragmentActivity).tripsDao().deleteByTripId(currentTrip.getTripId());
                                }
                                DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(mFragmentActivity));
                            }
                        });
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.notifyDataSetChanged();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }

            private void init() {
                background = new ColorDrawable(Color.WHITE);
                xMark = ContextCompat.getDrawable(mFragmentActivity, R.drawable.ic_delete_black_24dp);
                if (xMark != null) {
                    xMark.setColorFilter(mFragmentActivity.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                }
                initiated = true;
            }
        });
        helper.attachToRecyclerView(mRecyclerViewItems);
    }

}
