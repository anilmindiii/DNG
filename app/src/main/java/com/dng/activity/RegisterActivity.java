package com.dng.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dng.R;
import com.dng.activity.util.NumberFormatter;
import com.dng.activity.util.UsPhoneNumberFormatter;
import com.dng.activity.util.Utils;
import com.dng.helper.Constant;
import com.dng.helper.Validation;
import com.dng.image.picker.ImagePicker;
import com.dng.model.Country;
import com.dng.model.FaceBookInfo;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.session.Session;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dng.activity.util.Utils.isConnectingToInternet;

public class RegisterActivity extends AppCompatActivity {

    private ImageView profileImg;
    private EditText ed_fullname, ed_email, ed_phone, ed_driver_license, ed_emergency_name,
            ed_emergencyRelationship_name, ed_emergencyPhoneNumber_name;
    private ProgressDialog progressDialog;
    private Bitmap profileImageBitmap, licenseImageBitmap;
    private Session session;
    private TextView tv_license_ex_date, tv_license_image, tv_address, tv_2, tv_upload_profile;
    private List<Country> countries;
    private RelativeLayout ly_licence_date, ly_license_upload, ly_address, ly_profile_img;
    private int ImageType = 0;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "RegisterActivity";
    private String address = "";
    private DatePickerDialog fromDate;
    private ViewSwitcher simpleViewSwitcher;
    private ImageView buttonNext, buttonBack;
    private View view_connector_line2;
    private FaceBookInfo faceBookInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        session = new Session(RegisterActivity.this);
        countries = Utils.loadCountries(this);

        buttonNext =  findViewById(R.id.buttonNext);
        buttonBack = findViewById(R.id.buttonBack);
        simpleViewSwitcher =  findViewById(R.id.simpleViewSwitcher);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        simpleViewSwitcher.setInAnimation(in);
        simpleViewSwitcher.setOutAnimation(out);

        buttonNext.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (isValidDataSwith1()) {
                    simpleViewSwitcher.showNext();
                    buttonBack.setVisibility(View.VISIBLE);
                    buttonNext.setVisibility(View.GONE);
                    tv_2.setBackgroundResource(R.drawable.circle_solid_dark_primary);
                    view_connector_line2.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                simpleViewSwitcher.showPrevious();
                buttonNext.setVisibility(View.VISIBLE);
                buttonBack.setVisibility(View.GONE);
                tv_2.setBackgroundResource(R.drawable.circle_solid);
                view_connector_line2.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });


        findViewById(R.id.activity_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.hideSoftKeyboard(RegisterActivity.this);
            }
        });

        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        ly_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageType = Constant.ProfileType;
                getPermissionAndPicImage(ImageType);
            }
        });

        // Click action of Sign Up button
        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.hideSoftKeyboard(RegisterActivity.this);
                String fullname = ed_fullname.getText().toString();
                String email = ed_email.getText().toString();
                String phone = ed_phone.getText().toString();
                String licenseId = ed_driver_license.getText().toString().trim();
                String date = tv_license_ex_date.getText().toString().trim();
                String emergencyName = ed_emergency_name.getText().toString().trim();
                String emergencyPhone = ed_emergencyPhoneNumber_name.getText().toString().trim();
                String emergencyRelation = ed_emergencyRelationship_name.getText().toString().trim();
                String address = tv_address.getText().toString().trim();

                if (isValidDataSwith2()) {
                    if (isConnectingToInternet(RegisterActivity.this)) {
                        if (faceBookInfo == null) {
                            signupTask(fullname, email, phone, licenseId, date, emergencyName, emergencyPhone, emergencyRelation, address, "", "", "");

                        } else {
                            signupTask(fullname, email, phone, licenseId, date, emergencyName, emergencyPhone, emergencyRelation, address, faceBookInfo.socialId, "facebook", faceBookInfo.profileImage);

                        }
                    }
                }
            }
        });

        ly_licence_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateField();
            }
        });

        ly_license_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageType = Constant.LicenseType;
                getPermissionAndPicImage(ImageType);
            }
        });

        ly_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ly_address.setEnabled(false);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(RegisterActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        if (getIntent() != null) {
            faceBookInfo = (FaceBookInfo) getIntent().getSerializableExtra("facebookInfo");
            if (faceBookInfo != null) {
                ed_fullname.setText(faceBookInfo.fullname);
                ed_email.setText(faceBookInfo.email);

                if (!faceBookInfo.profileImage.equals("") && faceBookInfo.profileImage != null) {
                    Glide.with(this).asBitmap().load(faceBookInfo.profileImage).into(profileImg);
                    tv_upload_profile.setText("Image uploaded");

                    Glide.with(getApplicationContext()).asBitmap()
                            .load(faceBookInfo.profileImage)
                            .into(new SimpleTarget<Bitmap>(100, 100) {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    profileImageBitmap = resource;
                                }

                            });

                }
            }
        }

    }

    private void init() {
        profileImg = findViewById(R.id.profileImg);
        ly_license_upload = findViewById(R.id.ly_license_upload);
        ed_fullname = findViewById(R.id.ed_fullname);
        ed_email = findViewById(R.id.ed_email);

        ed_phone = findViewById(R.id.ed_phone);
        ed_phone.addTextChangedListener(new UsPhoneNumberFormatter(new WeakReference<>(ed_phone)));

        ed_driver_license = findViewById(R.id.ed_driver_license);
        ed_emergency_name = findViewById(R.id.ed_emergency_name);
        ed_emergencyRelationship_name = findViewById(R.id.ed_emergencyRelationship_name);

        ed_emergencyPhoneNumber_name = findViewById(R.id.ed_emergencyPhoneNumber_name);
        ed_emergencyPhoneNumber_name.addTextChangedListener(new NumberFormatter(new WeakReference<>(ed_emergencyPhoneNumber_name)));

        view_connector_line2 = findViewById(R.id.view_connector_line2);

        tv_2 =  findViewById(R.id.tv_2);
        tv_license_ex_date =  findViewById(R.id.tv_license_ex_date);
        ly_licence_date =  findViewById(R.id.ly_licence_date);
        ly_profile_img = findViewById(R.id.ly_profile_img);
        tv_license_image =  findViewById(R.id.tv_license_image);
        ly_address =  findViewById(R.id.ly_address);
        tv_address =  findViewById(R.id.tv_address);
        tv_upload_profile =  findViewById(R.id.tv_upload_profile);

        faceBookInfo = new FaceBookInfo();
    }

    // Api call for sign up
    private void signupTask(String fullname, String email, String phone, String licenseId,
                            String date, String emergencyName, String emergencyPhone,
                            String emergencyRelation, String Address, String socialId, String socialType, String profileImage) {
        progressDialog.show();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, String> map = new HashMap<>();
        map.put("fullName", fullname);
        map.put("email", email);
        map.put("contact", phone);
        map.put("deviceType", "1");
        map.put("deviceToken", token);
        map.put("socialId", socialId);
        map.put("socialType", socialType);
        map.put("address", Address);
        map.put("driversLicense", licenseId);
        map.put("licenseExpiryDate", date);

        map.put("emergencyName", emergencyName);
        map.put("emergencyRelationship", emergencyRelation);
        map.put("emergencyPhoneNumber", emergencyPhone);


        Map<String, Bitmap> bitmapList = new HashMap<>();
        bitmapList.put("profileImage", profileImageBitmap);
        bitmapList.put("licenseImage", licenseImageBitmap);

        WebService api = new WebService(RegisterActivity.this, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {
                progressDialog.dismiss();
                try {
                    JSONObject js = new JSONObject(response);
                    Gson gson = new Gson();
                    User user = gson.fromJson(String.valueOf(js), User.class);

                    if (user.status.equals("success")) {
                        session.createSession(user);
                        Intent intent = new Intent(RegisterActivity.this, TemplateActivity.class);
                        intent.putExtra("isVerify", user.data.isVerify);
                        startActivity(intent);
                        finish();
                    } else if (user.status.equals("fail")) {
                        Toast.makeText(RegisterActivity.this, user.message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    Log.d("error", e + "");
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
        api.callMultiPartApi("userRegistration", map, bitmapList);
    }


    public void getPermissionAndPicImage(int imageType) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 105);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Constant.SELECT_FILE);
                    getPermissionAndPicImage(ImageType);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.external_storage_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constant.REQUEST_CAMERA);
                    getPermissionAndPicImage(ImageType);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 234) {

                if (ImageType == Constant.ProfileType) {

                    profileImageBitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                    if (profileImageBitmap != null)
                        profileImg.setImageBitmap(profileImageBitmap);
                    tv_upload_profile.setText("Image uploaded");

                } else if (ImageType == Constant.LicenseType) {

                    licenseImageBitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                    if (licenseImageBitmap != null) {
                        tv_license_image.setText("Image uploaded");

                    }
                }
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            ly_address.setEnabled(true);
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                address = place.getAddress().toString();
                // lat = latLng.latitude;
                // lng = latLng.longitude;
                tv_address.setText(address);

                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            }
        }
    }

    // Validation for first phase of sign up process
    public boolean isValidDataSwith1() {
        Validation v = new Validation();
        String img = tv_upload_profile.getText().toString();

        if (!v.isNullValue(ed_fullname)) {
            Toast.makeText(this, getResources().getString(R.string.register_full_name_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_email)) {
            Toast.makeText(this, getResources().getString(R.string.register_email_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isEmailValid(ed_email)) {
            Toast.makeText(this, getResources().getString(R.string.register_email_invalid), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_phone)) {
            Toast.makeText(this, getResources().getString(R.string.register_phone_no_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(tv_address)) {
            Toast.makeText(this, getResources().getString(R.string.register_address_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (profileImageBitmap == null && !img.equals("Image uploaded")) {
            Toast.makeText(this, getResources().getString(R.string.register_profile_image_null), Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;
    }

    // Validation for second phase of sign up process
    public boolean isValidDataSwith2() {
        Validation v = new Validation();

        if (!v.isNullValue(ed_driver_license)) {
            Toast.makeText(this, getResources().getString(R.string.driving_license_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(tv_license_ex_date)) {
            Toast.makeText(this, getResources().getString(R.string.license_expiry_date_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (licenseImageBitmap == null) {
            Toast.makeText(this, getResources().getString(R.string.license_image_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_emergency_name)) {
            Toast.makeText(this, getResources().getString(R.string.emergency_name_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_emergencyRelationship_name)) {
            Toast.makeText(this, getResources().getString(R.string.relationship_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_emergencyPhoneNumber_name)) {
            Toast.makeText(this, getResources().getString(R.string.contact_no_null), Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;

    }

    // Calendar to set license expiry date
    private void setDateField() {
        Calendar now = Calendar.getInstance();
        fromDate = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
                // String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                String day, month;
                day = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                monthOfYear += 1;
                month = (monthOfYear < 10) ? "0" + monthOfYear : String.valueOf(monthOfYear);

                String date = year + "-" + month + "-" + day;


                tv_license_ex_date.setText(date);

            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        fromDate.setMinDate(Calendar.getInstance());
        fromDate.show(getFragmentManager(), "");
        fromDate.setAccentColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorPrimary));
        fromDate.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
                fromDate.dismiss();
            }
        });
    }
}
