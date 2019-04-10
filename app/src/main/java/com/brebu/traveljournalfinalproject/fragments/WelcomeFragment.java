package com.brebu.traveljournalfinalproject.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brebu.traveljournalfinalproject.R;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;

public class WelcomeFragment extends Fragment {

    private boolean isIdle;
    private FragmentActivity mFragmentManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragmentManager = getActivity();
        if (mFragmentManager != null) {
            PowerManager powerManager = (PowerManager) mFragmentManager.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                if (powerManager.isInteractive()) {
                    checkFirebaseDatabase();
                }
            }
        }

        return getView();
    }

    @Override
    public void onPause() {
        super.onPause();
        isIdle = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isIdle) {
            checkFirebaseDatabase();
        }
    }

    private void checkFirebaseDatabase() {
        FirebaseRepository.getTrips().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (!documentSnapshots.isEmpty()) {
                    createDynamicFragment(new TabbedFragment());
                } else {
                    createDynamicFragment(new EmptyFragment());
                }
            }
        });
    }

    private void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = mFragmentManager.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.commit();
    }

}
