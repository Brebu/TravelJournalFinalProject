package com.brebu.traveljournalfinalproject.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.squareup.picasso.Picasso;


public class ViewTripFragment extends Fragment implements Constants {


    private FragmentActivity mFragmentActivity;
    private ImageView mImageViewTripFavourite;
    private ImageView mImageViewTripImage;
    private RatingBar mRatingBarTripRating;
    private TextView mTextViewTripDestination;
    private TextView mTextViewTripEndDate;
    private TextView mTextViewTripName;
    private TextView mTextViewTripPrice;
    private TextView mTextViewTripStartDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_trip, container, false);
        mFragmentActivity = getActivity();
        initView(view);
        inflateView();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void inflateView() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mTextViewTripName.setText(bundle.getString(TRIP_NAME));
            mTextViewTripDestination.setText(bundle.getString(TRIP_DESTINATION));
            mTextViewTripPrice.setText(" " + bundle.getInt(TRIP_PRICE) + " € ");
            mTextViewTripStartDate.setText("Start date: " + bundle.getString(START_DATE));
            mTextViewTripEndDate.setText("End date: " + bundle.getString(END_DATE));

            float convertedRating = bundle.getFloat(TRIP_RATING);
            mRatingBarTripRating.setRating(convertedRating);

            String isFavourite = bundle.getString(TRIP_FAVOURITE);
            if (Boolean.parseBoolean(isFavourite)) {
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_full);
            } else {
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_border);
            }

            Picasso.get().load(bundle.getString(FIRESTORE_PATH)).noPlaceholder().resize(6000,6000).centerCrop().onlyScaleDown()
                    .into(mImageViewTripImage);

        }
    }


    private void initView(View view) {
        mTextViewTripName = view.findViewById(R.id.textView_ViewTravelName);
        mTextViewTripDestination = view.findViewById(R.id.textView_ViewTravelDestination);
        mTextViewTripPrice = view.findViewById(R.id.textView_ViewTravelPrice);
        mTextViewTripStartDate = view.findViewById(R.id.textView_ViewTravelStartDate);
        mTextViewTripEndDate = view.findViewById(R.id.textView_ViewTravelEndDate);
        mRatingBarTripRating = view.findViewById(R.id.ratingBar_ViewTripRating);
        mImageViewTripImage = view.findViewById(R.id.imageView_ViewTravelImage);
        mImageViewTripFavourite = view.findViewById(R.id.imageView_ViewTravelFavourite);
    }

}
