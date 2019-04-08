package com.brebu.traveljournalfinalproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.fragments.FavouriteFragment;
import com.brebu.traveljournalfinalproject.fragments.HomeFragment;
import com.brebu.traveljournalfinalproject.fragments.WelcomeFragment;
import com.brebu.traveljournalfinalproject.models.Trip;
import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.brebu.traveljournalfinalproject.room.DatabaseInitializer;
import com.brebu.traveljournalfinalproject.room.LocalDatabase;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static com.brebu.traveljournalfinalproject.utils.BitmapProcess.bitmapToData;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, Constants {

    public static final Handler HANDLER = new Handler();
    //Constants
    private static final String TAG = "MainActivity";
    //View instances
    private DrawerLayout mDrawerLayout;
    private GoogleApiClient mGoogleApiClient;
    private ImageView mImageViewProfilePicture;
    private NavigationView mNavigationView;
    private TextView mTextViewProfileMail;
    private TextView mTextViewProfileName;
    private TextView mTextViewWelcome;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_TRIP) {
            if (resultCode == Activity.RESULT_OK) {

                mTextViewWelcome.setVisibility(View.INVISIBLE);

                assert data != null;
                String tripName = data.getStringExtra(TRIP_NAME);
                String tripDestination = data.getStringExtra(TRIP_DESTINATION);
                String tripType = data.getStringExtra(TRIP_TYPE);
                String tripStart = data.getStringExtra(START_DATE);
                String tripEnd = data.getStringExtra(END_DATE);
                String tripPrice = data.getStringExtra(TRIP_PRICE);
                String tripRating = data.getStringExtra(TRIP_RATING);
                String tripFirestorePath = data.getStringExtra(FIRESTORE_PATH);
                final String tripPhotoPath = data.getStringExtra(PHOTO_PATH);
                File image = new File(tripPhotoPath);
                Uri tempUri = Uri.fromFile(image);

                int convertedPrice = Integer.parseInt(tripPrice) * 50;
                float convertedRating = Float.parseFloat(tripRating);
                Date tempStartDate = null;
                Date tempEndDate = null;
                try {
                    tempStartDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripStart);
                    tempEndDate =
                            DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(tripEnd);
                } catch (ParseException e) {
                    e.getStackTrace();
                }


                byte[] dataBitmap = bitmapToData(tempUri, this);

                final Trip trip = new Trip(tripName, tripDestination, tripType, convertedPrice,
                        tempStartDate, tempEndDate, convertedRating, tempUri.toString(),
                        tripFirestorePath, false);

                StorageReference storageRef = FirebaseRepository.getFirebaseStorage().getReference();
                StorageReference imgStorageRef =
                        storageRef.child(FirebaseRepository.getMail()).child(trip.getTripId() + ".jpg");

                UploadTask uploadTask = imgStorageRef.putBytes(dataBitmap);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                trip.setUserId(FirebaseRepository.getMail());
                                trip.setTripImageFirestore(uri.toString());
                                FirebaseRepository.getTrips().document(trip.getTripId()).set(trip);
                                Toast.makeText(MainActivity.this, "Trip added successfully!",
                                        Toast.LENGTH_SHORT).show();
                                mNavigationView.getMenu().getItem(0).setChecked(true);
                                FirebaseRepository.getTrips().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                        if (documentSnapshots.size() == 1) {
                                            setTitle("Home");
                                            mTextViewWelcome.setVisibility(View.GONE);
                                            createDynamicFragment(new HomeFragment());
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFirebase();
        initGoogleClient();
        DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            FirebaseRepository.getTrips().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (documentSnapshots.isEmpty()) {
                        mTextViewWelcome.setText("No trip!");
                        mTextViewWelcome.setVisibility(View.VISIBLE);
                    } else {
                        mTextViewWelcome.setVisibility(View.GONE);
                        createDynamicFragment(new HomeFragment());
                    }
                }
            });

            this.setTitle("Home");

        } else if (id == R.id.nav_gallery) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    if (LocalDatabase.getTravelJournalDatabase(getApplicationContext()).tripsDao().getAllTrips(FirebaseRepository.getMail()).isEmpty()) {
                        mTextViewWelcome.setText("No favorite trips :(");
                        mTextViewWelcome.setVisibility(View.VISIBLE);
                        createDynamicFragment(new FavouriteFragment());
                    } else {
                        mTextViewWelcome.setVisibility(View.GONE);
                        createDynamicFragment(new FavouriteFragment());
                    }
                }
            });
            mTextViewWelcome.setVisibility(View.VISIBLE);

            this.setTitle("Favourites");

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                FirebaseRepository.getFirebaseAuth().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createDynamicFragment(Fragment fragment) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);
        fragmentTransaction.addToBackStack("MainActivity").commit();
    }

    private void initFirebase() {

        if (FirebaseRepository.getFirebaseAuth() != null && FirebaseRepository.getFirebaseUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
                setTitle("Welcome");
                createDynamicFragment(new WelcomeFragment());
                mTextViewWelcome.setVisibility(View.GONE);

            if (FirebaseRepository.getUsername() != null && !FirebaseRepository.getUsername().isEmpty()) {
                mTextViewProfileName.setText(FirebaseRepository.getUsername());
            }
            if (FirebaseRepository.getPhotoUrl() != null) {


                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.no_picture)
                        .error(R.drawable.no_picture)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .circleCrop();

                Glide.with(this)
                        .load(FirebaseRepository.getPhotoUrl())
                        .apply(options)
                        .into(mImageViewProfilePicture);
            }
            if (FirebaseRepository.getMail() != null && !FirebaseRepository.getMail().isEmpty()) {
                mTextViewProfileMail.setText(FirebaseRepository.getMail());
            }
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
    }

    private void initView() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddOrModifyTrip.class);
                intent.putExtra(TRIP_UUID, (String) null);
                intent.putExtra(USER_ID, (String) null);
                startActivityForResult(intent, ADD_NEW_TRIP);
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View view = mNavigationView.getHeaderView(0);

        mImageViewProfilePicture = view.findViewById(R.id.imageView_profilePicture);
        mTextViewProfileName = view.findViewById(R.id.textView_profileName);
        mTextViewProfileMail = view.findViewById(R.id.textView_emailProfile);
        mTextViewWelcome = findViewById(R.id.TextView_Welcome);
        mTextViewWelcome.setVisibility(View.GONE);
    }
}
