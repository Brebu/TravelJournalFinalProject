package com.brebu.traveljournalfinalproject.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.brebu.traveljournalfinalproject.R;

class TripsViewHolder extends RecyclerView.ViewHolder {

    private ImageButton imageButtonTrip;
    private ImageView imageViewTrip;
    private View mItemView;
    private TextView textViewDestinationTrip;
    private TextView textViewEndDate;
    private TextView textViewStartDate;
    private TextView textViewTitleTrip;
    private RatingBar mRatingBarTrip;

    TripsViewHolder(@NonNull View itemView) {
        super(itemView);

        mRatingBarTrip = itemView.findViewById(R.id.ratingBar_travelRating);
        imageViewTrip = itemView.findViewById(R.id.imageView_travelImage);
        imageButtonTrip = itemView.findViewById(R.id.imageButton);
        textViewTitleTrip = itemView.findViewById(R.id.textView_travelTitle);
        textViewDestinationTrip = itemView.findViewById(R.id.textView_travelDestination);
        textViewStartDate = itemView.findViewById(R.id.textView_tripStart);
        textViewEndDate = itemView.findViewById(R.id.textView_tripEnd);
        mItemView = itemView;
    }


    ImageButton getImageButtonTrip() {
        return imageButtonTrip;
    }

    ImageView getImageViewTrip() {
        return imageViewTrip;
    }

    View getItemView() {
        return mItemView;
    }

    TextView getTextViewDestinationTrip() {
        return textViewDestinationTrip;
    }

    TextView getTextViewEndDate() {
        return textViewEndDate;
    }

    TextView getTextViewStartDate() {
        return textViewStartDate;
    }

    TextView getTextViewTitleTrip() {
        return textViewTitleTrip;
    }

    RatingBar getRatingBarTrip() {
        return mRatingBarTrip;
    }
}