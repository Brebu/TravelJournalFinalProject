package com.brebu.traveljournalfinalproject.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.brebu.traveljournalfinalproject.R;

public class TripsViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageViewTrip;
    public ImageButton imageButtonTrip;
    public ImageButton imageButtonDelete;
    public TextView textViewTitleTrip;
    public TextView textViewDestinationTrip;
    public TextView textViewStartDate;
    public TextView textViewEndDate;
    public View mItemView;

    public TripsViewHolder(@NonNull View itemView) {
        super(itemView);

        imageViewTrip = itemView.findViewById(R.id.imageView_travelImage);
        imageButtonTrip = itemView.findViewById(R.id.imageButton);
        imageButtonDelete = itemView.findViewById(R.id.imageButton_delete);
        textViewTitleTrip = itemView.findViewById(R.id.textView_travelTitle);
        textViewDestinationTrip = itemView.findViewById(R.id.textView_travelDestination);
        textViewStartDate = itemView.findViewById(R.id.textView_tripStart);
        textViewEndDate = itemView.findViewById(R.id.textView_tripEnd);
        mItemView = itemView;
    }
}