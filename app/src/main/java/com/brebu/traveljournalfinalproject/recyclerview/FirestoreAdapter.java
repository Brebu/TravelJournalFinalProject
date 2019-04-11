package com.brebu.traveljournalfinalproject.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot> {

    private static final String TAG = "Firestore Adapter";
    private DocumentSnapshot mDocumentSnapshot;
    private Query mQuery;
    private ListenerRegistration mRegistration;
    private ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots,
                        FirebaseFirestoreException e) {

        //This is important!
        notifyDataSetChanged();

        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            return;
        }


        // Dispatch the event
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            // Snapshot of the changed document
            mDocumentSnapshot = change.getDocument();

            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemoved(change);
                    break;
            }
        }
    }

    FirestoreAdapter(Query query) {
        mQuery = query;
    }

    public DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    private void onDocumentAdded(DocumentChange change) {

        int newIndex = change.getNewIndex();
        mSnapshots.add(newIndex, mDocumentSnapshot);
        notifyItemInserted(newIndex);
    }

    private void onDocumentModified(DocumentChange change) {

        int newIndex = change.getNewIndex();
        int oldIndex = change.getOldIndex();

        if (oldIndex == newIndex) {
            // Item was changed but is still in same position
            mSnapshots.set(oldIndex, mDocumentSnapshot);
            notifyItemChanged(oldIndex);
        } else {
            // Item was changed and also change position
            mSnapshots.remove(oldIndex);
            mSnapshots.add(newIndex, mDocumentSnapshot);
            notifyItemMoved(oldIndex, newIndex);
        }
    }

    private void onDocumentRemoved(DocumentChange change) {
        int oldInt = change.getOldIndex();
        mSnapshots.remove(oldInt);
        notifyItemRemoved(oldInt);
    }

//    public void setQuery(Query query) {
//
//        stopListening();
//
//        mSnapshots.clear();
//        notifyDataSetChanged();
//
//        mQuery = query;
//        startListening();
//    }

    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

//    private void stopListening() {
//        if (mRegistration != null) {
//            mRegistration.remove();
//            mRegistration = null;
//        }
//
//        mSnapshots.clear();
//        notifyDataSetChanged();
//    }
}