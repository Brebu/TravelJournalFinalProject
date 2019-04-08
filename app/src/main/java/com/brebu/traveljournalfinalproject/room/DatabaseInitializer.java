package com.brebu.traveljournalfinalproject.room;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;

import java.util.List;

public class DatabaseInitializer {

    private static final String TAG = DatabaseInitializer.class.getName();

    private static List<Trip> sTripList;

    public static void addTrip(final LocalDatabase db, Trip trip) {
        db.tripsDao().insertTrip(trip);
    }

    public static List<Trip> getTripList() {
        return sTripList;
    }

    public static void populateAsync(@NonNull final LocalDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, List<Trip>> {

        private final LocalDatabase mDb;

        @Override
        protected List<Trip> doInBackground(Void... voids) {

            return mDb.tripsDao().getAllTrips(FirebaseRepository.getMail());
        }

        @Override
        protected void onPostExecute(List<Trip> items) {
            super.onPostExecute(items);
            sTripList = items;
        }

        PopulateDbAsync(LocalDatabase db) {
            mDb = db;
        }
    }
}