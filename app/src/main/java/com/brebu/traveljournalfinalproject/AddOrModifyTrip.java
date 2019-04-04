package com.brebu.traveljournalfinalproject;

import android.Manifest;
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

import com.brebu.traveljournalfinalproject.utils.Constants;
import com.brebu.traveljournalfinalproject.utils.CustomDatePickerFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddOrModifyTrip extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, Constants {

    String TAG = "AddOrModifyTrip";

    //View instances
    private EditText mEditTextNameTrip;
    private EditText mEditTextDestinationTrip;
    private RadioGroup mRadioGroupTrip;
    private Button mButtonStartDateTrip;
    private Button mButtonEndDateTrip;
    private RadioButton mRadioButtonCityBreak;
    private RadioButton mRadioButtonSeaSide;
    private RadioButton mRadioButtonMountains;
    private TextView mTextViewPriceTrip;
    private SeekBar mSeekBarPriceTrip;
    private RatingBar mRatingBarRatingTrip;

    //Class instances
    private Uri mImageUri;
    private String mImagePath;
    private String mImageFirestore;
    private String mTripName;
    private String mTripDestination;
    private String mStartDate;
    private String mEndDate;
    private String mTripType;
    private String mPrice;
    private String mRating;
    public static String mTripId;
    private String mTripIdFromFirestore;
    private String mFirestoreId;
    private boolean mSelectedDate;
    public static boolean mPictureChanged;

    //Firestore instances
    private FirebaseFirestore mFirebaseFirestore;
    private DocumentReference mTripReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_modify_trip);

        mTripId = getIntent().getExtras().getString(MainActivity.TRIP_UUID);
        mFirestoreId = getIntent().getExtras().getString(MainActivity.USER_ID);
        initView();
        initFirestore();

    }

    private void initFirestore() {
        mFirebaseFirestore = null;
        mTripReference = null;
        if (mFirestoreId != null && mTripId != null) {

            mFirebaseFirestore = FirebaseFirestore.getInstance();
            mTripReference = mFirebaseFirestore.collection(mFirestoreId).document(mTripId);

            mTripReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {

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

                }
            });
        }
    }

    private void initView() {
        mEditTextNameTrip = findViewById(R.id.editText_tripName);
        mEditTextDestinationTrip = findViewById(R.id.editText_tripDestination);
        mRadioGroupTrip = findViewById(R.id.radioGroup);

        mButtonStartDateTrip = findViewById(R.id.button_startDateTrip);
        mButtonStartDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(Calendar.getInstance().getTime()));
        mStartDate = mButtonStartDateTrip.getHint().toString();

        mButtonEndDateTrip = findViewById(R.id.button_endDateTrip);
        mButtonEndDateTrip.setHint(DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).format(Calendar.getInstance().getTime()));
        mEndDate = mButtonEndDateTrip.getHint().toString();

        mTextViewPriceTrip = findViewById(R.id.textView_priceTrip);
        mSeekBarPriceTrip = findViewById(R.id.seekBar_priceTrip);
        mTextViewPriceTrip.setText("Price ( " + mSeekBarPriceTrip.getProgress() + " EUR )");
        mSeekBarPriceTrip.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 50;
                progress = progress * 50;
                mTextViewPriceTrip.setText("Price ( " + progress + " EUR )");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRatingBarRatingTrip = findViewById(R.id.ratingBar);
        mRadioButtonCityBreak = findViewById(R.id.radioButton_cityBreak);
        mRadioButtonSeaSide = findViewById(R.id.radioButton_seaSide);
        mRadioButtonMountains = findViewById(R.id.radioButton_mountains);
    }

    private void openAndDisplay() {
        File photoFile = createImageFile();
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        assert photoFile != null;
        mImageUri = FileProvider.getUriForFile(this, "com.brebu.traveljournalfinalproject" +
                ".provider", photoFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(intent, TAKE_RC);
    }

    private boolean checkPermission() {
        int cameraResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        return cameraResult == 0 && writeResult == 0;
    }

    private File createImageFile() {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM + "/Camera");
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (image != null) {
            mImagePath = image.getAbsolutePath();

        }
        return image;
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mImagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void onClickStartDate(View view) {
        mSelectedDate = false;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onClickEndDate(View view) {
        mSelectedDate = true;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onClickSelectPhoto(View view) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"),
                SELECT_RC);
    }

    public void onClickTakePhoto(View view) {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(AddOrModifyTrip.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_RC);
        } else {
            openAndDisplay();
        }
    }

    private String checkTripType() {
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

    private boolean isValidEntries() {
        boolean completed = true;
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(mStartDate);
            endDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.UK).parse(mEndDate);

            if (startDate.compareTo(endDate) > 0) {
                completed = false;
                Toast.makeText(this, "End date must be greater than start date",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (mEditTextNameTrip.getText() == null || mEditTextNameTrip.getText().toString().isEmpty()) {
            mEditTextNameTrip.setError("Please enter a trip name");
            completed = false;
        }
        if (mEditTextDestinationTrip.getText() == null || mEditTextDestinationTrip.getText().toString().isEmpty()) {
            mEditTextDestinationTrip.setError("Please enter a destination");
            completed = false;
            Toast.makeText(this, "Please add a photo to this trip", Toast.LENGTH_LONG).show();
        }
        if (mImagePath == null) {
            completed = false;
            Toast.makeText(this, "Please add a photo to this trip", Toast.LENGTH_LONG).show();
        }
        if (completed) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted",
                            Toast.LENGTH_SHORT).show();
                    openAndDisplay();
                }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        String tempMonth = String.valueOf(month + 1);
        String tempDay = String.valueOf(dayOfMonth);

        if (month < 10) {
            tempMonth = "0" + tempMonth;
        }
        if (dayOfMonth < 10) {
            tempDay = "0" + tempDay;
        }

        if (!mSelectedDate) {
            mStartDate = tempDay + "/" + (tempMonth) + "/" + year;
            mButtonStartDateTrip.setHint(mStartDate);

        } else {
            mEndDate = tempDay + "/" + (tempMonth) + "/" + year;
            mButtonEndDateTrip.setHint(mEndDate);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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

            mPictureChanged = true;


        } else if (requestCode == TAKE_RC) {
            if (resultCode == Activity.RESULT_OK) {
                galleryAddPic();
                mPictureChanged = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mTripId == null) {
            finish();
        }
        Intent intent = new Intent(AddOrModifyTrip.this, MainActivity.class);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    public void onClickSaveButton(View view) {
        if (!isValidEntries()) {
            return;
        }


        mTripName = mEditTextNameTrip.getText().toString();
        mTripDestination = mEditTextDestinationTrip.getText().toString();
        mTripType = checkTripType();
        mPrice = Integer.toString(mSeekBarPriceTrip.getProgress() / 50);
        mStartDate = mButtonStartDateTrip.getHint().toString();
        mEndDate = mButtonEndDateTrip.getHint().toString();
        mRating = String.valueOf(mRatingBarRatingTrip.getRating());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(TRIP_NAME, mTripName);
        intent.putExtra(TRIP_DESTINATION, mTripDestination);
        intent.putExtra(TRIP_TYPE, mTripType);
        intent.putExtra(START_DATE, mStartDate);
        intent.putExtra(END_DATE, mEndDate);
        intent.putExtra(TRIP_PRICE, mPrice);
        intent.putExtra(TRIP_RATING, mRating);
        intent.putExtra(PHOTO_PATH, mImagePath);


        //This is for a new trip
        if (mFirestoreId == null && mTripId == null) {
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
}
