package com.brebu.traveljournalfinalproject.fragment;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


public class ViewTripFragment extends Fragment implements Constants {


    private FragmentActivity mFragmentActivity;

    private TextView mTextViewTripName;
    private TextView mTextViewTripDestination;
    private TextView mTextViewTripPrice;
    private TextView mTextViewTripStartDate;
    private TextView mTextViewTripEndDate;
    private RatingBar mRatingBarTripRating;
    private ImageView mImageViewTripImage;
    private ImageView mImageViewTripFavourite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_trip, container, false);
        mFragmentActivity = getActivity();
        initView(view);
        inflateView();
        return view;
    }

    private void inflateView() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mTextViewTripName.setText(bundle.getString(TRIP_NAME));
            mTextViewTripDestination.setText(bundle.getString(TRIP_DESTINATION));

            String tempPrice = bundle.getString(TRIP_PRICE);
            if (tempPrice != null) {
                String price = tempPrice.substring(0, tempPrice.length() - 2);
                mTextViewTripPrice.setText(" " + price + " € ");
            }

            mTextViewTripStartDate.setText("Start date: "+bundle.getString(START_DATE));
            mTextViewTripEndDate.setText("End date: "+bundle.getString(END_DATE));

            float convertedRating = Float.parseFloat(bundle.getString(TRIP_RATING));
            mRatingBarTripRating.setRating(convertedRating);

            String isFavourite = bundle.getString("tripFavourite");
            if(Boolean.parseBoolean(isFavourite)){
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_full);
            }else{
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_border);
            }


            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.no_picture)
                    .error(R.drawable.no_picture)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform();

            Glide.with(mFragmentActivity)
                    .load(bundle.getString(FIRESTORE_PATH))
                    .apply(options)
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
