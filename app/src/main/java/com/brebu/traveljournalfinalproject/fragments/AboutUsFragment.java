package com.brebu.traveljournalfinalproject.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brebu.traveljournalfinalproject.R;


public class AboutUsFragment extends Fragment {

    FragmentActivity mFragmentActivity;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        mFragmentActivity = getActivity();
        if (mFragmentActivity != null) {
            mFragmentActivity.setTitle("About");
        }
        return view;
    }
}
