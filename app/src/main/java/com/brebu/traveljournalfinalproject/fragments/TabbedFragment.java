package com.brebu.traveljournalfinalproject.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.NonSwipeAbleViewPager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class TabbedFragment extends Fragment implements Constants {

    private FragmentActivity mFragmentActivity;
    private Fragment mFragmentHomeDateAsc;
    private Fragment mFragmentHomeDateDsc;
    private Fragment mFragmentHomePriceAsc;
    private Fragment mFragmentHomePriceDsc;
    private Fragment mFragmentHomeRatingAsc;
    private Fragment mFragmentHomeRatingDsc;


    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("home").commit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragmentActivity = getActivity();
        FirebaseRepository.getTrips().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {

                if (documentSnapshots.isEmpty()) {

                    createDynamicFragment(new EmptyFragment());
                }

            }

        });

        if (mFragmentActivity != null) {

            mFragmentActivity.setTitle("Home");

        }

        View view = inflater.inflate(R.layout.fragment_tabbed, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabs);

        final NonSwipeAbleViewPager viewPager = view.findViewById(R.id.viewpager);

        viewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        tabLayout.setTabRippleColor(null);

        mFragmentHomeDateAsc = new HomeFragment();
        mFragmentHomeDateDsc = new HomeFragment();
        mFragmentHomePriceAsc = new HomeFragment();
        mFragmentHomePriceDsc = new HomeFragment();
        mFragmentHomeRatingAsc = new HomeFragment();
        mFragmentHomeRatingDsc = new HomeFragment();

        return view;
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private Fragment setFragmentArguments(Fragment fragment, String order, String direction) {
            Bundle bundle = new Bundle();
            bundle.putString("order", order);
            bundle.putString("direction", direction);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return setFragmentArguments(mFragmentHomeDateAsc,START_DATE,Query.Direction.ASCENDING.toString());
                case 1:
                    return setFragmentArguments(mFragmentHomeDateDsc,START_DATE,Query.Direction.DESCENDING.toString());
                case 2:
                    return setFragmentArguments(mFragmentHomePriceAsc,TRIP_PRICE,Query.Direction.ASCENDING.toString());
                case 3:
                    return setFragmentArguments(mFragmentHomePriceDsc,TRIP_PRICE,Query.Direction.DESCENDING.toString());
                case 4:
                    return setFragmentArguments(mFragmentHomeRatingAsc,TRIP_RATING,Query.Direction.ASCENDING.toString());
                case 5:
                    return setFragmentArguments(mFragmentHomeRatingDsc,TRIP_RATING,Query.Direction.DESCENDING.toString());

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 6;
        }

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

}
