package com.brebu.traveljournalfinalproject.recyclerview;

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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavouriteTripsAdapter extends RecyclerView.Adapter<TripsViewHolder> {

    private List<Trip> mTripList;
    private Context mContext;
    private OnTripSelectedListener mListener;


    public FavouriteTripsAdapter(List<Trip> trips, Context context, OnTripSelectedListener listener) {
        this.mTripList = trips;
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_travel,
                viewGroup, false);
        return new TripsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TripsViewHolder tripsViewHolder, final int i) {

        Trip currentTrip = mTripList.get(i);
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

        if (!currentTrip.isTripFavourite()) {
            tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_full));
        } else {
            tripsViewHolder.imageButtonTrip.setImageDrawable(mContext.getDrawable(R.drawable.ic_bookmark_border));
        }

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
                    mListener.onTripSelected(mTripList.get(i));
                }
            }
        });
        tripsViewHolder.mItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null) {
                    mListener.onTripLongPressed(mTripList.get(i));
                }
                return true;
            }
        });

        tripsViewHolder.imageButtonTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onIconPressed(mTripList.get(i), tripsViewHolder.imageButtonTrip);
                }
            }
        });


        tripsViewHolder.imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeletePressed(mTripList.get(i), tripsViewHolder.imageButtonDelete);
                }
            }
        });

        tripsViewHolder.imageButtonDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.onDeleteLongPressed(mTripList.get(i),
                            tripsViewHolder.imageButtonDelete);
                }
                return true;
            }
        });


    }


    @Override
    public int getItemCount() {
        return mTripList.size();
    }

}