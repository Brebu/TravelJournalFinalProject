package com.brebu.traveljournalfinalproject.repository;


import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseRepository {

    private static String mMail, mUsername, mPhotoUrl;

    public static FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseStorage getFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    public static FirebaseUser getFirebaseUser() {
        return getFirebaseAuth().getCurrentUser();
    }

    public static String getMail() {
        if (getFirebaseUser() != null) {
            mMail = getFirebaseUser().getEmail();
        }
        return mMail;
    }

    public static String getPhotoUrl() {
        if (getFirebaseUser() != null) {

            Uri photoProfile = getFirebaseUser().getPhotoUrl();

            if (photoProfile != null) {
                mPhotoUrl = photoProfile.toString();
            }
        }
        return mPhotoUrl;
    }

    public static CollectionReference getTrips() {
        return getFirebaseFirestore().collection(getMail());
    }

    public static String getUsername() {
        if (getFirebaseUser() != null) {
            mUsername = getFirebaseUser().getDisplayName();
        }
        return mUsername;
    }
}
