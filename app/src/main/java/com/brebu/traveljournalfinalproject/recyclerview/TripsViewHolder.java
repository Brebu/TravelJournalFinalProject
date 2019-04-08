package com.brebu.traveljournalfinalproject.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.brebu.traveljournalfinalproject.R;

public class TripsViewHolder extends RecyclerView.ViewHolder {

    private ImageButton imageButtonDelete;
    private ImageButton imageButtonTrip;
    private ImageView imageViewTrip;
    private View mItemView;
    private TextView textViewDestinationTrip;
    private TextView textViewEndDate;
    private TextView textViewStartDate;
    private TextView textViewTitleTrip;

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

    public ImageButton getImageButtonDelete() {
        return imageButtonDelete;
    }

    public ImageButton getImageButtonTrip() {
        return imageButtonTrip;
    }

    public ImageView getImageViewTrip() {
        return imageViewTrip;
    }

    public View getItemView() {
        return mItemView;
    }

    public TextView getTextViewDestinationTrip() {
        return textViewDestinationTrip;
    }

    public TextView getTextViewEndDate() {
        return textViewEndDate;
    }

    public TextView getTextViewStartDate() {
        return textViewStartDate;
    }

    public TextView getTextViewTitleTrip() {
        return textViewTitleTrip;
    }
}