package com.dng.fragment;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dng.R;
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.helper.TimerAlertBroadcastReceiver;
import com.dng.helper.Validation;
import com.dng.image.picker.ImagePicker;
import com.dng.model.RequestInfo;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static com.dng.helper.AppHelper.getEditTextFilterEmoji;

public class ProfileTerminalFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SwitchCompat switch_duty;
    private TextView tv_duty_status, user_name, email;
    private ImageView profileImg, iv_back;
    private Session session;
    private RelativeLayout ly_personal, ly_legal, ly_change_password;
    private RelativeLayout daily_report;
    private Context mContext;
    private String mParam1;
    private String mParam2;
    private Dialog changePasswordDialog;
    private User user;

    private EditText ed_old_password, ed_new_password;
    private ImageView dialog_decline_button;
    private TextView btn_done, password_update_success;
    private boolean isToLogout;
    private ProgressBar progress;
    private Bitmap profileImageBitmap;

    public ProfileTerminalFragment() {

    }

    public static ProfileTerminalFragment newInstance(String param1, String param2) {
        ProfileTerminalFragment fragment = new ProfileTerminalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    void init(View view) {
        session = new Session(mContext);
        user = new User();
        user = session.getUser();

        getProfile();

        switch_duty = view.findViewById(R.id.switch_duty);
        tv_duty_status = view.findViewById(R.id.tv_duty_status);
        user_name = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.email);
        profileImg = view.findViewById(R.id.profileImg);
        ly_personal = view.findViewById(R.id.ly_personal);
        ly_legal = view.findViewById(R.id.ly_legal);
        daily_report = view.findViewById(R.id.daily_report);
        iv_back = view.findViewById(R.id.iv_back);
        ly_change_password = view.findViewById(R.id.ly_change_password);

        if(user.data.userSoicalStatus != 0){
            ly_change_password.setVisibility(View.GONE);
        }
    }

    private void stopEmojisAndCopyPast(EditText editText){
        InputFilter[] filterArray = new InputFilter[] {
                getEditTextFilterEmoji()
        };
        editText.setFilters(filterArray);

        editText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  Do Something or Don't
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_terminal, container, false);
        init(view);

        // Setting On or OFF duty
        switch_duty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch_duty.isChecked()) {
                    onDutyMethod();
                } else {
                    onDutyMethod();
                }
            }
        });

        if (user.data.onDuty == 1) {
            switch_duty.setChecked(true);
            tv_duty_status.setText(R.string.on);
        } else {
            tv_duty_status.setText(R.string.off);
            switch_duty.setChecked(false);
        }

        // Setting driver profile image
        if (user.data.profile != null) {
            Glide.with(mContext)
                    .load(user.data.profile).apply(new RequestOptions()
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform())
                    .into(profileImg);
        }

        // Set driver user name and email
        user_name.setText(user.data.fullName.toString());
        email.setText(user.data.email.toString());

        iv_back.setOnClickListener(this);
        ly_personal.setOnClickListener(this);
        ly_legal.setOnClickListener(this);
        daily_report.setOnClickListener(this);
        ly_change_password.setOnClickListener(this);
        profileImg.setOnClickListener(this);

        return view;
    }

    // Method to open dialog for change password
    private void openChangePasswordDialog() {
        changePasswordDialog = new Dialog(mContext);
        changePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        changePasswordDialog.setContentView(R.layout.dialog_change_password);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(changePasswordDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        changePasswordDialog.getWindow().setAttributes(lWindowParams);

        ed_old_password = changePasswordDialog.findViewById(R.id.ed_old_password);
        ed_new_password = changePasswordDialog.findViewById(R.id.ed_new_password);
        dialog_decline_button = changePasswordDialog.findViewById(R.id.dialog_decline_button);
        password_update_success = changePasswordDialog.findViewById(R.id.password_update_success);
        btn_done = changePasswordDialog.findViewById(R.id.btn_done);
        progress = changePasswordDialog.findViewById(R.id.progress);


        stopEmojisAndCopyPast(ed_old_password);
        stopEmojisAndCopyPast(ed_new_password);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePasswordDialog.dismiss();
            }
        });

        btn_done.setOnClickListener(this);

        changePasswordDialog.getWindow().setGravity(Gravity.CENTER);
        changePasswordDialog.show();
    }

    // Api call to change password
    private void changePasswordApi(String new_password) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);

            Map<String, String> map = new HashMap<>();
            map.put("password", new_password);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        Log.e("CHANGE PASSWORD", response.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");


                        if (status.equals("success")) {
                          /*  progress.setVisibility(View.GONE);
                            password_update_success.setVisibility(View.VISIBLE);
                            isToLogout = true;*/
                            progress.setVisibility(View.GONE);

                            resultMsg(mContext,"Your password has been changed successfully. please login again");

                        } else {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callApi("user/updateInfo", Request.Method.POST, map);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }


    public void resultMsg(final Context mContext, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("DNG");
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                logout();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Api call to logout after change password
    private void logout() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {

                            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                AppHelper.stopAlarmService(mContext);
                            }else {
                                mContext.stopService(new Intent(mContext, MyService.class));
                            }

                            if (session != null && session.getRequestInfo().data.driverInfo != null) {
                                if (session.getRequestInfo().data.driverInfo.onRoute != null && !session.getRequestInfo().data.driverInfo.onRoute.equals("")) {
                                    if (session.getRequestInfo().data.driverInfo.onRoute.equals("1")) {
                                        startStopRoute();
                                    }
                                }
                            }

                            session.logout();

                            Intent myIntent = new Intent(mContext, TimerAlertBroadcastReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                                    234324243, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            pendingIntent.cancel();

                            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                            assert alarmManager != null;
                            alarmManager.cancel(pendingIntent);

                        } else {
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error", e + "");
                        session.logout();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    session.logout();
                }

            });
            api.callApi("user/logout", Request.Method.GET, null);

        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Api call to start stop route to stop route on logout
    private void startStopRoute() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);
                        Log.e("Log Out Route", response.toString());

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {

                            String route = js.getString("route");

                            if (route.equals("ON ROUTE")) {
                                //  tv_route.setText("Stop Route");
                                if (session.getRequestInfo() != null) {
                                    session.getRequestInfo().data.driverInfo.onRoute = "1";
                                    RequestInfo requestInfo = session.getRequestInfo();
                                    requestInfo.data.driverInfo.onRoute = "1";
                                    session.createRequest(requestInfo);
                                }

                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (!Settings.canDrawOverlays(mContext)) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + mContext.getPackageName()));
                                        startActivityForResult(intent, 1234);
                                    }
                                }

                                startStopRoute();

                            } else if (route.equals("OFF ROUTE")) {
                                if (session.getRequestInfo() != null) {
                                    session.getRequestInfo().data.driverInfo.onRoute = "0";
                                    RequestInfo requestInfo = session.getRequestInfo();
                                    requestInfo.data.driverInfo.onRoute = "0";
                                    session.createRequest(requestInfo);
                                }
                            }

                        } else {
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }

            });
            api.callApi("user/route", Request.Method.GET, null, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Validation to check old password and new password is valid or not
    public boolean isInfoValid() {
        Validation v = new Validation();

        if (!v.isNullValue(ed_old_password)) {
            Toast.makeText(mContext, getResources().getString(R.string.old_password_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isNullValue(ed_new_password)) {
            Toast.makeText(mContext, getResources().getString(R.string.new_password_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!session.getUserPassword().equals(ed_old_password.getText().toString().trim())) {
            Toast.makeText(mContext, getResources().getString(R.string.old_password_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (session.getUserPassword().equals(ed_new_password.getText().toString().trim())) {
            Toast.makeText(mContext, getResources().getString(R.string.old_new_pasword_same), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isPasswordValid(ed_old_password)) {
            Toast.makeText(mContext, getString(R.string.login_password_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isPasswordValid(ed_new_password)) {
            Toast.makeText(mContext, getString(R.string.login_password_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getFragmentManager();
       /* int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }*/
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // Api call to set driver On duty or Off duty
    public void onDutyMethod() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {
                            String dutyStatus = js.getString("duty");

                            if (dutyStatus.equals("1")) {
                                tv_duty_status.setText(R.string.on);
                                Toast.makeText(mContext, R.string.on, Toast.LENGTH_SHORT).show();
                                user = session.getUser();
                                user.data.onDuty = 1;
                                session.createSession(user);
                                switch_duty.setChecked(true);
                            } else {
                                tv_duty_status.setText(R.string.off);
                                user = session.getUser();
                                user.data.onDuty = 0;
                                session.createSession(user);
                                switch_duty.setChecked(false);
                                Toast.makeText(mContext, R.string.off, Toast.LENGTH_SHORT).show();
                            }


                        } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error", e + "");
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }

            });
            api.callApi("user/duty", Request.Method.GET, null);

        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Api call to get driver profile
    private void getProfile() {
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {
                            Gson gson = new Gson();
                            User user = gson.fromJson(String.valueOf(js), User.class);
                            session.createSession(user);

                        } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("error", e + "");
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }

            });
            api.callApi("user/userInfo", Request.Method.GET, null);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done:
                if (isInfoValid()) {
                    changePasswordApi(ed_new_password.getText().toString().trim());
                }
                break;

            case R.id.iv_back:
                replaceFragment(new SettingsFragment(), false, R.id.fragment_place);
                break;

            case R.id.ly_personal:
                replaceFragment(new PersonalInfoFragment(), true, R.id.fragment_place);
                break;

            case R.id.ly_legal:
                replaceFragment(new LegalInfoFragment(), true, R.id.fragment_place);
                break;

            case R.id.daily_report:
                replaceFragment(new DailyReports(), true, R.id.fragment_place);
                break;

            case R.id.ly_change_password:
                openChangePasswordDialog();
                break;

            case R.id.profileImg:
                getPermissionAndPicImage();
                break;
        }
    }


    public void getPermissionAndPicImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                    getPermissionAndPicImage();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.external_storage_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constant.REQUEST_CAMERA);
                    getPermissionAndPicImage();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 234) {
            profileImageBitmap = ImagePicker.getImageFromResult(mContext, requestCode, resultCode, data);
            if (profileImageBitmap != null)
                updatePersonalInfo();
            profileImg.setImageBitmap(profileImageBitmap);
        }
    }


    // Api call to update Profile image
    private void updatePersonalInfo() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            Map<String, Bitmap> bitmapList = new HashMap<>();
            bitmapList.put("profileImage", profileImageBitmap);

            Map<String, String> map = new HashMap<>();

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(final String response, String apiName) {

                    try {
                        final JSONObject jsonObject = new JSONObject(response);

                        Log.e("PROFILE IMAGE RESPONSE", response.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Gson gson = new Gson();
                            User user = gson.fromJson(String.valueOf(jsonObject), User.class);
                            session.createSession(user);

                            Toast.makeText(mContext, getResources().getString(R.string.profile_image_updated_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callMultiPartApi("user/updateInfo", map, bitmapList);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }
}


