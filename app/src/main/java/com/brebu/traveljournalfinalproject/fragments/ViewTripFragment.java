package com.brebu.traveljournalfinalproject.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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

    //View instances
    private ImageView mImageViewTripFavourite;
    private ImageView mImageViewTripImage;
    private RatingBar mRatingBarTripRating;
    private TextView mTextViewTripDestination;
    private TextView mTextViewTripName;
    private TextView mTextViewTripPrice;
    private TextView mTextViewTripStartDate;
    private ImageView mExpandedImageView;

    //Class instances
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private FragmentActivity mFragmentActivity;
    private Bundle mBundle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_trip, container, false);
        mFragmentActivity = getActivity();
        initView(view);
        inflateView();

        mImageViewTripImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View newView) {
                zoomImageFromThumb(mFragmentActivity.findViewById(R.id.cardView));
            }
        });

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void inflateView() {

        mBundle = this.getArguments();
        if (mBundle != null) {
            mTextViewTripName.setText(mBundle.getString(TRIP_NAME));
            mTextViewTripDestination.setText(mBundle.getString(TRIP_DESTINATION));
            mTextViewTripPrice.setText(" " + mBundle.getInt(TRIP_PRICE) + " € ");
            mTextViewTripStartDate.setText("Period: " + mBundle.getString(START_DATE) + " - " + mBundle.getString(END_DATE));

            float convertedRating = mBundle.getFloat(TRIP_RATING);
            mRatingBarTripRating.setRating(convertedRating);

            String isFavourite = mBundle.getString(TRIP_FAVOURITE);
            if (Boolean.parseBoolean(isFavourite)) {
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_full);
            } else {
                mImageViewTripFavourite.setImageResource(R.drawable.ic_bookmark_border);
            }

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.no_picture)
                    .error(R.drawable.no_picture)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(this)
                    .load(mBundle.getString(FIRESTORE_PATH))
                    .apply(options)
                    .into(mImageViewTripImage);

        }
    }


    private void initView(View view) {
        mTextViewTripName = view.findViewById(R.id.textView_ViewTravelName);
        mTextViewTripDestination = view.findViewById(R.id.textView_ViewTravelDestination);
        mTextViewTripPrice = view.findViewById(R.id.textView_ViewTravelPrice);
        mTextViewTripStartDate = view.findViewById(R.id.textView_ViewTravelStartDate);
        mRatingBarTripRating = view.findViewById(R.id.ratingBar_ViewTripRating);
        mImageViewTripImage = view.findViewById(R.id.imageView_ViewTravelImage);
        mImageViewTripFavourite = view.findViewById(R.id.imageView_ViewTravelFavourite);
        mExpandedImageView = view.findViewById(R.id.expanded_image);
    }

    private void zoomImageFromThumb(final View thumbView) {

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.no_picture)
                .error(R.drawable.no_picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(this)
                .load(mBundle.getString(FIRESTORE_PATH))
                .apply(options)
                .into(mExpandedImageView);

        mExpandedImageView.setElevation(100);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mFragmentActivity.findViewById(R.id.ctx)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        mExpandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mExpandedImageView.setPivotX(0f);
        mExpandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mExpandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(mExpandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        mExpandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(mExpandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        mExpandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        mExpandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
