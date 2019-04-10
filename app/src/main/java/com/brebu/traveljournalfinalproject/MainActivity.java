package com.brebu.traveljournalfinalproject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.brebu.traveljournalfinalproject.fragments.FavouriteFragment;
import com.brebu.traveljournalfinalproject.fragments.HomeFragment;
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
import com.google.android.gms.appinvite.AppInviteInvitation;
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
import java.util.List;
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
    private GoogleApiClient mGoogleApiClient;
    private ImageView mImageViewProfilePicture;
    private NavigationView mNavigationView;
    private TextView mTextViewProfileMail;
    private TextView mTextViewProfileName;
    private TextView mTextViewWelcome;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "id of sent invitation: " + id);
                }
            } else {
                Toast.makeText(this,"Failed to send invitation",Toast.LENGTH_LONG).show();
            }
        }
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
                                            createDynamicFragment(new TabbedFragment());
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
        }
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
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
            getSupportFragmentManager().popBackStack();
            createDynamicFragment(new TabbedFragment());
        } else if (id == R.id.nav_favourites) {
            getSupportFragmentManager().popBackStack();
            createDynamicFragment(new FavouriteFragment());

        } else if (id == R.id.nav_about_us) {
            getSupportFragmentManager().popBackStack();
        } else if (id == R.id.nav_contact) {

            getSupportFragmentManager().popBackStack();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"brebu.ciprian@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Message from: " + FirebaseRepository.getUsername());
            try {
                startActivity(Intent.createChooser(i, "Contact this developer"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_share) {
            getSupportFragmentManager().popBackStack();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Here is a good project! \n https://traveljournalfinalproject.page.link/install");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's install!");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent,
                    "Send to.."));

        } else if (id == R.id.nav_send) {

                Intent intent = new AppInviteInvitation.IntentBuilder("Try out my cool app!")
                        .setMessage("Hey, this app is really cool. I thought you might like it!")
                        .setDeepLink(Uri.parse("https://traveljournalfinalproject.page.link/install"))
                        .setCallToActionText("Let's do this!")
                        .build();
                startActivityForResult(intent, 999);

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
        fragmentTransaction.commit();
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
