package com.brebu.traveljournalfinalproject.recyclerview;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.room.TravelJournalDatabase;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TripsAdapter extends FirestoreAdapter<TripsViewHolder> {

    private Context mContext;
    private OnTripSelectedListener mListener;

    public TripsAdapter(Query query, OnTripSelectedListener listener, Context context) {
        super(query);
        mListener = listener;
        mContext = context;
    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_travel,
                viewGroup, false);
        return new TripsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TripsViewHolder tripsViewHolder, final int i) {

        final Trip currentTrip = getSnapshot(i).toObject(Trip.class);

        tripsViewHolder.textViewTitleTrip.setText(currentTrip.getTripName());
        tripsViewHolder.textViewDestinationTrip.setText(currentTrip.getTripDestination());

        //Set DateStart hint
        Date startDate = currentTrip.getTripStartDate();
        if (startDate != null) {
            tripsViewHolder.textViewStartDate.setText("Start date: " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(startDate));
        }

        //Set DateEnd hint
        Date endDate = currentTrip.getTripEndDate();
        if (startDate != null) {
            tripsViewHolder.textViewEndDate.setText("End date: " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(endDate));
        }


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> tempList = new ArrayList<>();
                for (Trip trip :
                        TravelJournalDatabase.getTravelJournalDatabase(mContext).tripsDao().getAllTrips()) {
                    tempList.add(trip.getTripId());
                }
                if (tempList.contains(currentTrip.getTripId())) {
                    tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_full));
                } else {
                    tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_border));
                }
            }
        });


//        if (currentTrip.isTripFavourite()) {
//            tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable
// .ic_bookmark_full));
//        } else {
//            tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable
// .ic_bookmark_border));
//        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.no_picture)
                .error(R.drawable.no_picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform();

        Glide.with(mContext)
                .load(currentTrip.getTripImageFirestore())
                .apply(options)
                .into(tripsViewHolder.imageViewTrip);


        // Click listener
        tripsViewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onTripSelected(getSnapshot(i));
                }
            }
        });
        tripsViewHolder.mItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null) {
                    mListener.onTripLongPressed(getSnapshot(i));
                }
                return true;
            }
        });

        tripsViewHolder.imageButtonTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onIconPressed(getSnapshot(i), tripsViewHolder.imageButtonTrip);
                }
            }
        });


        tripsViewHolder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeletePressed(getSnapshot(i), tripsViewHolder.imageButtonDelete);
                }
            }
        });

        tripsViewHolder.imageButtonDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.onDeleteLongPressed(getSnapshot(i),
                            tripsViewHolder.imageButtonDelete);
                }
                return true;
            }
        });


    }


}