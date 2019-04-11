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
            tripsViewHolder.getRatingBarTrip().setRating(currentTrip.getTripRating());

            //Set Price
            int tripPrice = currentTrip.getTripPrice();
            if (tripPrice != 0) {
                tripsViewHolder.getTextViewStartDate().setText("Trip price: " + tripPrice + " €");
            }

            //Set Rating
            float tripRating = currentTrip.getTripRating();
            if (tripRating != 0.0f) {
                tripsViewHolder.getTextViewEndDate().setText("Trip rating: " + tripRating);
            }

            if (currentTrip.isTripFavourite()) {
                tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable
                        .ic_bookmark_full));
            } else {
                tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable
                        .ic_bookmark_border));
            }

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.no_picture)
                    .error(R.drawable.no_picture)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(mContext)
                    .load(currentTrip.getTripImageFirestore())
                    .apply(options)
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