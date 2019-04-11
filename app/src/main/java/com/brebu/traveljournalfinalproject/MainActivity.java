package com.brebu.traveljournalfinalproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.fragments.AboutUsFragment;
import com.brebu.traveljournalfinalproject.fragments.FavouriteFragment;
import com.brebu.traveljournalfinalproject.fragments.TabbedFragment;
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
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static com.brebu.traveljournalfinalproject.utils.BitmapProcess.bitmapToData;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, Constants {

    public static final Handler HANDLER = new Handler();

    //Constants
    private static final String TAG = "MainActivity";

    //View instances
    private DrawerLayout mDrawerLayout;
    private ImageView mImageViewProfilePicture;
    private NavigationView mNavigationView;
    private TextView mTextViewProfileMail;
    private TextView mTextViewProfileName;

    //All fragments from activity
    private Fragment mFragmentAboutUsFragment;
    private Fragment mFragmentFavouriteFragment;
    private Fragment mFragmentTabbed;
    private Fragment mFragmentWelcomeFragment;

    //Google instances
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCrashlytics();
        initView();
        initFragments();
        initFirebase();
        initGoogleClient();
        initLocalDatabase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_TRIP && data != null) {
            if (resultCode == Activity.RESULT_OK) {

                //Get all data from intent
                String tripName = data.getStringExtra(TRIP_NAME);
                String tripDestination = data.getStringExtra(TRIP_DESTINATION);
                String tripType = data.getStringExtra(TRIP_TYPE);
                String tripStart = data.getStringExtra(START_DATE);
                String tripEnd = data.getStringExtra(END_DATE);
                String tripPrice = data.getStringExtra(TRIP_PRICE);
                String tripRating = data.getStringExtra(TRIP_RATING);
                String tripFirestorePath = data.getStringExtra(FIRESTORE_PATH);
                final String tripPhotoPath = data.getStringExtra(PHOTO_PATH);

                //Convert photo path to file image uri
                File image = new File(tripPhotoPath);
                Uri tempUri = Uri.fromFile(image);

                //Convert price from snackBar
                int convertedPrice = Integer.parseInt(tripPrice) * 50;

                //Convert rating from ratingBar
                float convertedRating = Float.parseFloat(tripRating);

                //Convert string to date
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

                //Image to byte[]
                byte[] dataBitmap = bitmapToData(tempUri, this);

                //Generate trip object with all parameters gained above
                final Trip trip = new Trip(tripName, tripDestination, tripType, convertedPrice,
                        tempStartDate, tempEndDate, convertedRating, tempUri.toString(),
                        tripFirestorePath, false);

                //Declare and initialize storage reference
                StorageReference storageRef = FirebaseRepository.getFirebaseStorage().getReference();

                //Declare and initialize the image storage reference
                StorageReference imgStorageRef = storageRef.child(FirebaseRepository.getMail()).child(trip.getTripId() + getString(R.string.jpg_ext));

                //Begin upload image to firebase storage
                UploadTask uploadTask = imgStorageRef.putBytes(dataBitmap);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //Try to get the url of uploaded image
                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();

                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            public void onSuccess(Uri uri) {

                                //Replace user id on trip object
                                trip.setUserId(FirebaseRepository.getMail());

                                //Replace firebase image location on trip object
                                trip.setTripImageFirestore(uri.toString());

                                FirebaseRepository.getTrips().document(trip.getTripId()).set(trip).addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //Focus home menu item
                                        mNavigationView.getMenu().getItem(0).setChecked(true);

                                        //Replace fragment with home fragment
                                        createDynamicFragment(mFragmentTabbed);

                                        Toast.makeText(MainActivity.this, "Trip added successfully!", Toast.LENGTH_SHORT).show();

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
        //Close drawer if is opened
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        //PopBackStack if exist
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Failed to connect");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            //Set home fragment
            createDynamicFragment(new TabbedFragment());

        } else if (id == R.id.nav_favourites) {

            //Set favourites fragment
            createDynamicFragment(mFragmentFavouriteFragment);

        } else if (id == R.id.nav_about_us) {

            //Set about us fragment
            createDynamicFragment(mFragmentAboutUsFragment);

        } else if (id == R.id.nav_contact) {

            //Create sendIntent to send mail
            Intent sendIntent = new Intent(Intent.ACTION_SEND);

            //Set type
            sendIntent.setType(getString(R.string.message_rfc822));

            //Set destination mail
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_mail_address)});

            //Set subject with authenticated user name
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_from) + FirebaseRepository.getUsername());

            //Check for installed client
            try {

                startActivity(Intent.createChooser(sendIntent, getString(R.string.contact_application_developer)));

            } catch (android.content.ActivityNotFoundException ex) {

                Toast.makeText(this, getString(R.string.no_mail_client_installed), Toast.LENGTH_SHORT).show();

            }

        } else if (id == R.id.nav_share) {

            //Create an intent used for share
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            //Set message
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here you will find it!   https://traveljournalfinalproject.page.link/install");

            //Set type
            shareIntent.setType("text/plain");

            //Set message
            startActivity(Intent.createChooser(shareIntent, "Share with friends"));

        } else if (id == R.id.find_cv) {

            //Create cv download intent
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cv_link)));
            startActivity(intent);

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

        //Create fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        //Create fragment transaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //Replace a fragment
        fragmentTransaction.replace(R.id.frame_layout_drawer_fragment, fragment);

        //Commit transaction
        fragmentTransaction.commit();
    }

    private void initCrashlytics() {

        //Initialize Crashlytics
        Fabric.with(this, new Crashlytics());

    }

    private void initFirebase() {

        //Check for sign in
        if (FirebaseRepository.getFirebaseAuth() != null && FirebaseRepository.getFirebaseUser() == null) {

            //Return to sign in activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();

        } else {

            //Set welcome fragment
            createDynamicFragment(mFragmentWelcomeFragment);

            if (FirebaseRepository.getUsername() != null && !FirebaseRepository.getUsername().isEmpty()) {

                //Set profile name in header
                mTextViewProfileName.setText(FirebaseRepository.getUsername());

            }
            if (FirebaseRepository.getPhotoUrl() != null) {

                //Set profile picture in header with Glide
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

                //Set profile mail address in header
                mTextViewProfileMail.setText(FirebaseRepository.getMail());

            }
        }
    }

    private void initFragments() {

        //Initiate all activity fragments
        mFragmentFavouriteFragment = new FavouriteFragment();
        mFragmentTabbed = new TabbedFragment();
        mFragmentWelcomeFragment = new WelcomeFragment();
        mFragmentAboutUsFragment = new AboutUsFragment();

    }

    private void initGoogleClient() {

        //Initialize Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

    }

    private void initLocalDatabase() {

        //Initialize local database
        DatabaseInitializer.populateAsync(LocalDatabase.getTravelJournalDatabase(this));

    }

    private void initView() {

        //Initialize and set action bar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize FAB and set onClickListener
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

        //Initialize drawer layout
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //Initialize navigation view, header view and all of header view views
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View view = mNavigationView.getHeaderView(0);
        mImageViewProfilePicture = view.findViewById(R.id.imageView_profilePicture);
        mTextViewProfileName = view.findViewById(R.id.textView_profileName);
        mTextViewProfileMail = view.findViewById(R.id.textView_emailProfile);
    }

}
