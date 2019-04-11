package com.brebu.traveljournalfinalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brebu.traveljournalfinalproject.repository.FirebaseRepository;
import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.CustomDatePickerFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddOrModifyTrip extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, Constants {


    //Class constant
    private static String TAG = "AddOrModifyTrip";

    //View instances
    private Button mButtonEndDateTrip;
    private Button mButtonStartDateTrip;
    private EditText mEditTextDestinationTrip;
    private EditText mEditTextNameTrip;
    private RadioButton mRadioButtonCityBreak;
    private RadioButton mRadioButtonMountains;
    private RadioButton mRadioButtonSeaSide;
    private RadioGroup mRadioGroupTrip;
    private RatingBar mRatingBarRatingTrip;
    private SeekBar mSeekBarPriceTrip;
    private TextView mTextViewPriceTrip;

    //Class instances
    private String mEndDate;
    private String mImageFirestore;
    private String mImagePath;
    private String mStartDate;
    private String mTripIdFromFirestore;
    public static String mTripId;
    private boolean mSelectedDate;
    public static boolean mPictureChanged;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Set image path if select was chose
        if (requestCode == SELECT_RC && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
            }

            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mImagePath = cursor.getString(columnIndex);
                cursor.close();
            }

            //Make a flag if picture was changed
            mPictureChanged = true;

            //Set image path if take was chose
        } else if (requestCode == TAKE_RC) {
            if (resultCode == Activity.RESULT_OK) {

                //Save picture to gallery
                galleryAddPic();

                //Make a flag if picture was changed
                mPictureChanged = true;
            }
        }
    }

    @Override
    public void onBackPressed() {

        //If is not in edit mode, just finish activity
        if (mTripId == null) {
            finish();
        }

        //If is in edit mode, return to main activity with Cancel result and finish activity
        Intent intent = new Intent(AddOrModifyTrip.this, MainActivity.class);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_modify_trip);

        //Get bundle
        Bundle bundle = getIntent().getExtras();

        //If bundle is !null set mTripId
        if (bundle != null) {
            mTripId = bundle.getString(TRIP_UUID);
        }

        initView();
        initFirestore();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //Convert date to string and ste hint on buttons
        String tempMonth = String.valueOf(month + 1);
        String tempDay = String.valueOf(dayOfMonth);

        if (month < 10) {
            tempMonth = "0" + tempMonth;
        }
        if (dayOfMonth < 10) {
            tempDay = "0" + tempDay;
        }

        //Check what button was pressed and set date
        if (!mSelectedDate) {
            mStartDate = tempDay + "/" + (tempMonth) + "/" + year;
            mButtonStartDateTrip.setHint(mStartDate);

        } else {
            mEndDate = tempDay + "/" + (tempMonth) + "/" + year;
            mButtonEndDateTrip.setHint(mEndDate);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Check permission
        switch (requestCode) {
            case PERMISSION_RC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //If camera permission granted continue with camera operation
                    openAndDisplay();

                }
                break;
            case PERMISSION_RT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //If gallery permission granted continue with image gallery select
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), SELECT_RC);

                }
                break;
        }
    }

    private boolean checkCameraPermission() {

        //Check for camera permission
        int cameraResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return cameraResult == 0;

    }

    private boolean checkGalleryPermission() {

        //Check for gallery permision
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return writeResult == 0;
    }

    private String checkTripType() {

        //Check selected trip type
        switch (mRadioGroupTrip.getCheckedRadioButtonId()) {
            case R.id.radioButton_cityBreak:
                return "City Break";
            case R.id.radioButton_seaSide:
                return "Sea Side";
            case R.id.radioButton_mountains:
                return "Mountain";
            default:
                return "";
        }
    }

    private File createImageFile() {

        //Get timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        //Set image name prefix
        String imageFileName = "IMG_" + timeStamp + "_";

        //Set directory for storage
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");


        File image = null;

        try {

            //Initialize image and create temp file
            image = File.createTempFile(imageFileName, ".jpg", storageDir);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (image != null) {

            //Get image path
            mImagePath = image.getAbsolutePath();

        }

        return image;
    }

    private void galleryAddPic() {

        //Add image to gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mImagePath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void initFirestore() {

        if (mTripId != null) {

            //Firestore instances
            DocumentReference tripReference = FirebaseRepository.getTrips().document(mTripId);
            tripReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    mPictureChanged = false;

                    //Set EditTextNameTrip with value from firebase
                    mEditTextNameTrip.setText((String) documentSnapshot.get(TRIP_NAME));

                    //Set EditTextDestinationTrip with value from firebase
                    mEditTextDestinationTrip.setText((String) documentSnapshot.get(TRIP_DESTINATION));

                    //Set EditTextTypeTrip with value from firebase
                    mRadioGroupTrip.clearCheck();
                    String checkValue;
                    checkValue = (String) documentSnapshot.get(TRIP_TYPE);
                    if (checkValue != null) {
                        switch (checkValue) {
                            case "City Break":
                                mRadioButtonCityBreak.setChecked(true);
                                break;
                            case "Sea Side":
                                mRadioButtonSeaSide.setChecked(true);
                                break;
                            case "Mountain":
                                mRadioButtonMountains.setChecked(true);
                                break;
                        }
                    }

                    //Set EditTextPriceTrip with value from firebase
                    long readPrice = (long) documentSnapshot.get(TRIP_PRICE);
                    int convertedPrice = (int) readPrice;
                    mSeekBarPriceTrip.setProgress(convertedPrice);
                    mTextViewPriceTrip.setText("Price ( " + mSeekBarPriceTrip.getProgress() + " EUR )");

                    //Set DateStart hint with value from firebase
                    Timestamp startDate = (Timestamp) documentSnapshot.get(START_DATE);
                    if (startDate != null) {
                        mButtonStartDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(startDate.toDate()));
                    }

                    //Set DateEnd hint with value from firebase
                    Timestamp endDate = (Timestamp) documentSnapshot.get(END_DATE);
                    if (endDate != null) {
                        mButtonEndDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(endDate.toDate()));
                    }

                    //Set Rating with value from firebase
                    double readRating = (double) documentSnapshot.get(TRIP_RATING);
                    float convertedRating = (float) readRating;
                    mRatingBarRatingTrip.setRating(convertedRating);

                    //Save to variable value ImageFirestore for image from firestore
                    mImageFirestore = (String) documentSnapshot.get(FIRESTORE_PATH);

                    //Save to variable value ImagePath for image from firestore
                    mImagePath = (String) documentSnapshot.get(PHOTO_PATH);

                    //Save to variable value TripId for from firestore
                    mTripIdFromFirestore = (String) documentSnapshot.get(TRIP_ID);

                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {

        mEditTextNameTrip = findViewById(R.id.editText_tripName);
        mEditTextDestinationTrip = findViewById(R.id.editText_tripDestination);
        mRatingBarRatingTrip = findViewById(R.id.ratingBar);
        mRadioGroupTrip = findViewById(R.id.radioGroup);
        mRadioButtonCityBreak = findViewById(R.id.radioButton_cityBreak);
        mRadioButtonSeaSide = findViewById(R.id.radioButton_seaSide);
        mRadioButtonMountains = findViewById(R.id.radioButton_mountains);
        mButtonStartDateTrip = findViewById(R.id.button_startDateTrip);
        mButtonEndDateTrip = findViewById(R.id.button_endDateTrip);
        mTextViewPriceTrip = findViewById(R.id.textView_priceTrip);
        mSeekBarPriceTrip = findViewById(R.id.seekBar_priceTrip);

        //Get system time and set hint on buttons
        mButtonStartDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(Calendar.getInstance().getTime()));
        mStartDate = mButtonStartDateTrip.getHint().toString();

        //Get system time and set hint on buttons
        mButtonEndDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(Calendar.getInstance().getTime()));
        mEndDate = mButtonEndDateTrip.getHint().toString();


        mTextViewPriceTrip.setText("Price ( " + mSeekBarPriceTrip.getProgress() + " EUR )");
        mSeekBarPriceTrip.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Set step for seekBar
                progress = progress / 50;
                progress = progress * 50;
                mTextViewPriceTrip.setText("Price ( " + progress + " EUR )");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                //Don't need

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //Don't need

            }
        });
    }

    private boolean isValidEntries() {

        //Flag for all completed
        boolean completed = true;

        //Get date from string and compare start with end
        Date startDate;
        Date endDate;

        try {

            startDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(mStartDate);
            endDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(mEndDate);

            if (startDate.compareTo(endDate) > 0) {

                completed = false;
                Toast.makeText(this, "End date must be greater than start date", Toast.LENGTH_SHORT).show();

            }
        } catch (ParseException e) {

            e.printStackTrace();
            Log.e(TAG, e.getMessage());

        }

        //Check if title are completed
        if (mEditTextNameTrip.getText() == null || mEditTextNameTrip.getText().toString().isEmpty()) {

            mEditTextNameTrip.setError("Please enter a trip name");
            completed = false;

        }

        //Check if destination are completed
        if (mEditTextDestinationTrip.getText() == null || mEditTextDestinationTrip.getText().toString().isEmpty()) {

            mEditTextDestinationTrip.setError("Please enter a destination");
            completed = false;

        }

        //Check if image are selected
        if (mImagePath == null) {

            completed = false;
            Toast.makeText(this, "Please add a photo to this trip", Toast.LENGTH_LONG).show();

        }

        return completed;
    }

    public void onClickEndDate(View view) {

        //Set flag
        mSelectedDate = true;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    public void onClickSaveButton(View view) {

        //Check if all field are valid
        if (!isValidEntries()) {
            return;
        }

        //Get completed and selected values from UI
        String tripName = mEditTextNameTrip.getText().toString();
        String tripDestination = mEditTextDestinationTrip.getText().toString();
        String tripType = checkTripType();
        String price = Integer.toString(mSeekBarPriceTrip.getProgress() / 50);
        mStartDate = mButtonStartDateTrip.getHint().toString();
        mEndDate = mButtonEndDateTrip.getHint().toString();
        String rating = String.valueOf(mRatingBarRatingTrip.getRating());

        //Add values to intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(TRIP_NAME, tripName);
        intent.putExtra(TRIP_DESTINATION, tripDestination);
        intent.putExtra(TRIP_TYPE, tripType);
        intent.putExtra(START_DATE, mStartDate);
        intent.putExtra(END_DATE, mEndDate);
        intent.putExtra(TRIP_PRICE, price);
        intent.putExtra(TRIP_RATING, rating);
        intent.putExtra(PHOTO_PATH, mImagePath);


        //This is for a new trip
        if (mTripId == null) {
            mImageFirestore = "";
            intent.putExtra(FIRESTORE_PATH, mImageFirestore);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            intent.putExtra(FIRESTORE_PATH, mImageFirestore);
            intent.putExtra(TRIP_ID, mTripIdFromFirestore);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }

    public void onClickSelectPhoto(View view) {

        //Check for WRITE External Storage permission
        if (!checkGalleryPermission()) {

            //Request permission
            ActivityCompat.requestPermissions(AddOrModifyTrip.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RT);
        } else {

            //Create intent for select photo from gallery
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), SELECT_RC);

        }
    }

    public void onClickStartDate(View view) {

        //Set flag for date and open fragment for select date
        mSelectedDate = false;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    public void onClickTakePhoto(View view) {

        //Check for camera permission
        if (!checkCameraPermission()) {

            //Request camera permission
            ActivityCompat.requestPermissions(AddOrModifyTrip.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_RC);
        } else {

            //Continue with camera take pictura
            openAndDisplay();

        }
    }

    private void openAndDisplay() {

        //Send final URI image from camera to intent
        File photoFile = createImageFile();
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri = FileProvider.getUriForFile(this, "com.brebu.traveljournalfinalproject.provider", photoFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(intent, TAKE_RC);
    }
}
