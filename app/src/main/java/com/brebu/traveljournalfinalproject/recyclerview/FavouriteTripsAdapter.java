package com.brebu.traveljournalfinalproject.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavouriteTripsAdapter extends RecyclerView.Adapter<TripsViewHolder> {

    private Context mContext;
    private OnTripSelectedListener<Trip> mListener;
    private List<Trip> mTripList;

    @Override
    public int getItemCount() {
        return mTripList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final TripsViewHolder tripsViewHolder, @SuppressLint("RecyclerView") final int i) {

        Trip currentTrip = mTripList.get(i);
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

        if (!currentTrip.isTripFavourite()) {
            tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_full));
        } else {
            tripsViewHolder.getImageButtonTrip().setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_border));
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
                    mListener.onTripSelected(mTripList.get(i));
                }
            }
        });
        tripsViewHolder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null) {
                    mListener.onTripLongPressed(mTripList.get(i));
                }
                return true;
            }
        });

        tripsViewHolder.getImageButtonTrip().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onIconPressed(mTripList.get(i), tripsViewHolder.getImageButtonTrip());
                }
            }
        });

    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_travel,
                viewGroup, false);
        return new TripsViewHolder(view);
    }


    public FavouriteTripsAdapter(List<Trip> trips, Context context, OnTripSelectedListener<Trip> listener) {
        this.mTripList = trips;
        mContext = context;
        mListener = listener;
    }

}