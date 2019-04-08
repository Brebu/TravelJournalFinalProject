package com.brebu.traveljournalfinalproject.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.utils.OnTripSelectedListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class TripsAdapter extends FirestoreAdapter<TripsViewHolder> {

    private Context mContext;
    private OnTripSelectedListener<DocumentSnapshot> mListener;

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final TripsViewHolder tripsViewHolder, @SuppressLint("RecyclerView") final int i) {

        final Trip currentTrip = getSnapshot(i).toObject(Trip.class);

        if (currentTrip != null) {
            tripsViewHolder.getTextViewTitleTrip().setText(currentTrip.getTripName());

            tripsViewHolder.getTextViewDestinationTrip().setText(currentTrip.getTripDestination());

            //Set DateStart hint
            Date startDate = currentTrip.getTripStartDate();
            if (startDate != null) {
                tripsViewHolder.getTextViewStartDate().setText("Start date: " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(startDate));
            }

            //Set DateEnd hint
            Date endDate = currentTrip.getTripEndDate();
            if (startDate != null) {
                tripsViewHolder.getTextViewEndDate().setText("End date: " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(endDate));
            }

            if (currentTrip.isTripFavourite()) {
                tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable
                        .ic_bookmark_full));
            } else {
                tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable
                        .ic_bookmark_border));
            }

            Picasso.get().load(currentTrip.getTripImageFirestore()).noPlaceholder().resize(6000,6000).centerCrop().onlyScaleDown()
                    .into(tripsViewHolder.getImageViewTrip());


            // Click listener
            tripsViewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onTripSelected(getSnapshot(i));
                    }
                }
            });
            tripsViewHolder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mListener != null) {
                        mListener.onTripLongPressed(getSnapshot(i));
                    }
                    return true;
                }
            });

            tripsViewHolder.getImageButtonTrip().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onIconPressed(getSnapshot(i), tripsViewHolder.getImageButtonTrip());
                    }
                }
            });


            tripsViewHolder.getImageButtonDelete().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDeletePressed(getSnapshot(i), tripsViewHolder.getImageButtonDelete());
                    }
                }
            });

            tripsViewHolder.getImageButtonDelete().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onDeleteLongPressed(getSnapshot(i),
                                tripsViewHolder.getImageButtonDelete());
                    }
                    return true;
                }
            });

        }


    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_travel,
                viewGroup, false);
        return new TripsViewHolder(itemView);
    }

    public TripsAdapter(Query query, OnTripSelectedListener<DocumentSnapshot> listener, Context context) {
        super(query);
        mListener = listener;
        mContext = context;
    }

}