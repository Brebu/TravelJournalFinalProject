package com.brebu.traveljournalfinalproject.fragments;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private FragmentActivity mFragmentActivity;
    private RecyclerView mRecyclerViewItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recycler_view, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onIconPressed(Trip trip, ImageButton imageButton) {
        Toast.makeText(mFragmentActivity, "Please make favourite from home", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTripLongPressed(Trip trip) {
        Toast.makeText(mFragmentActivity, "Please edit only from home", Toast.LENGTH_LONG).show();
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
        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, displayTrip);
        fragmentTransaction.addToBackStack("View").commit();
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.commit();
    }

    private void initView(View view) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (LocalDatabase.getTravelJournalDatabase(mFragmentActivity).tripsDao().getAllTrips(FirebaseRepository.getMail()).isEmpty()) {
                    createDynamicFragment(new EmptyFragment());
                }
            }
        });

        mFragmentActivity = getActivity();
        if (mFragmentActivity != null) {
            mFragmentActivity.setTitle("Favourites");
        }
        mRecyclerViewItems = view.findViewById(R.id.recycler_view_x);
        mRecyclerViewItems.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mAdapter = new FavouriteTripsAdapter(DatabaseInitializer.getTripList(), mFragmentActivity, this);
        setItemHelper(mAdapter);
        mRecyclerViewItems.setAdapter(mAdapter);
    }

    private void setItemHelper(final FavouriteTripsAdapter adapter) {

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
                        final Trip currentTrip = DatabaseInitializer.getTripList().get(position);

                        DatabaseInitializer.getTripList().remove(currentTrip);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                LocalDatabase.getTravelJournalDatabase(mFragmentActivity).tripsDao().deleteTrip(currentTrip);
                            }
                        });
                        FirebaseRepository.getTrips().document(currentTrip.getTripId()).update(TRIP_FAVOURITE, false);
                        mAdapter.notifyDataSetChanged();
                        if (DatabaseInitializer.getTripList().isEmpty()) {
                            createDynamicFragment(new EmptyFragment());
                        }
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
