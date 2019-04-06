package com.brebu.traveljournalfinalproject.room;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.brebu.traveljournalfinalproject.MainActivity;
import com.brebu.traveljournalfinalproject.models.Trip;

import java.util.List;

public class DatabaseInitializer {

    public static List<Trip> sTripList;

    private static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(@NonNull final TravelJournalDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    public static Trip addTrip(final TravelJournalDatabase db, Trip trip) {
        db.tripsDao().insertTrip(trip);
        return trip;
    }


    private static class PopulateDbAsync extends AsyncTask<Void, Void, List<Trip>> {

        private final TravelJournalDatabase mDb;

        PopulateDbAsync(TravelJournalDatabase db) {
            mDb = db;
        }

        @Override
        protected List<Trip> doInBackground(Void... voids) {

            boolean contain = false;

            for (Users user : mDb.usersDao().getAllUsers()) {
                if (user.getUserId().equals(MainActivity.getMail())) {
                    contain = true;
                }
            }

            if (!contain) {
                mDb.usersDao().insertUser(new Users(MainActivity.getMail()));
            }

            return mDb.tripsDao().getAllTrips(MainActivity.getMail());
        }

        @Override
        protected void onPostExecute(List<Trip> items) {
            super.onPostExecute(items);
            sTripList = items;
        }
    }
}