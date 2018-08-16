package com.dng.fragment;

import android.Manifest;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.CustomListner.SendLocation;
import com.dng.R;
import com.dng.activity.MainActivity;
import com.dng.activity.util.Utils;
import com.dng.app.DNG;
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.helper.LocationRuntimePermission;
import com.dng.helper.TimerAlertBroadcastReceiver;
import com.dng.model.RequestInfo;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;


public class NewTaskFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SendLocation {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Session session;
    private ScrollView main_view;
    private TextView tv_no_request, tv_pitAddress, tv_order_number, tv_custmor_name,
            tv_delivery_add, tv_Pit_to_delivery, tv_quentity, tv_product, tv_payment_method,
            tv_amount, tv_pit_name, tv_pitPlaceHolder, tv_po_number;

    public static TextView tv_timer;
    private RequestInfo requestInfo;

    private ProgressBar progress;
    private Boolean isGPSEnable;
    private LocationManager lmgr;
    private TextView btn_accept, btn_reject, btn_start_route;
    private boolean isAccpt = false;

    private Context mContext;

    private RelativeLayout rl_shift_description, rl_customer_payment_status;
    private TextView tv_shift;

    private LocationManager locationManager;
    private long milles;

    // Get Current Location
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private String mLatitude, mLongitude;

    private boolean isGPSEnabled, isNetworkEnabled;

    private Dialog start_route_dialog;
    private LinearLayout ly_new_task_status;
    private int numberofshift = 0;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    Intent myIntent;

    public NewTaskFragment() {
        // Required empty public constructor
    }


    private void init(View view) {
        main_view = view.findViewById(R.id.main_view);
        tv_no_request = view.findViewById(R.id.tv_no_request);
        tv_pitAddress = view.findViewById(R.id.tv_pitAddress);
        tv_order_number = view.findViewById(R.id.tv_order_number);
        tv_custmor_name = view.findViewById(R.id.tv_custmor_name);
        tv_delivery_add = view.findViewById(R.id.tv_delivery_add);
        tv_quentity = view.findViewById(R.id.tv_quentity);
        tv_product = view.findViewById(R.id.tv_product);
        tv_payment_method = view.findViewById(R.id.tv_payment_method);
        tv_amount = view.findViewById(R.id.tv_amount);
        tv_pit_name = view.findViewById(R.id.tv_pit_name);
        tv_Pit_to_delivery = view.findViewById(R.id.tv_Pit_to_delivery);
        progress = view.findViewById(R.id.progress);
        btn_accept = view.findViewById(R.id.btn_accept);
        btn_reject = view.findViewById(R.id.btn_reject);

        tv_pitPlaceHolder = view.findViewById(R.id.tv_pitPlaceHolder);
        tv_po_number = view.findViewById(R.id.tv_po_number);

        btn_start_route = view.findViewById(R.id.btn_start_route);
        tv_timer = view.findViewById(R.id.tv_timer);

        ly_new_task_status = view.findViewById(R.id.ly_new_task_status);

        rl_shift_description = view.findViewById(R.id.rl_shift_description);
        rl_customer_payment_status = view.findViewById(R.id.rl_customer_payment_status);
        tv_shift = view.findViewById(R.id.tv_shift);
    }


    public static NewTaskFragment newInstance(String param1, String param2) {
        NewTaskFragment fragment = new NewTaskFragment();
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
            String current_lat = getArguments().getString(ARG_PARAM1);
            String current_lng = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);

        session = new Session(mContext);
        init(view);

        session.setScreen("NewTaskFragment");

        lmgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (isAccpt) {
            btn_start_route.setVisibility(View.VISIBLE);
        }

        MainActivity.setListner(this);

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isConnectingToInternet(mContext)) {
                    accptRejectMethod(requestInfo.data.deliveryId, "accept", view);
                } else Utils.defaultDialog(mContext);

            }
        });

        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isConnectingToInternet(mContext)) {
                    accptRejectMethod(requestInfo.data.deliveryId, "reject", view);
                } else Utils.defaultDialog(mContext);

            }
        });

        btn_start_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext != null) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(mContext)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + mContext.getPackageName()));
                            startActivityForResult(intent, 1234);
                        } else {
                            openStartRouteDialog(mContext);
                        }
                    } else {
                        openStartRouteDialog(mContext);
                    }
                }

            }
        });

        view.findViewById(R.id.ry_call_customer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesToCallDialog("Do you want to make call to " + requestInfo.data.orderInfo.customerPhoneNumber + " ?", requestInfo.data.orderInfo.customerPhoneNumber);

            }
        });

        view.findViewById(R.id.rl_call_pit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesToCallDialog("Do you want to make call to " + requestInfo.data.pitInfo.pitContact + " ?", requestInfo.data.pitInfo.pitContact);

            }
        });

        // Get Latitude and Longitude
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }
        displayCurrentLocation();

        // Api call to get order delivery info
        getRequestMethod();

        // Broadcast pending intent to launch pop up for launch app after finish 5 mins
        myIntent = new Intent(mContext, TimerAlertBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(mContext, 234324243, myIntent, 0);
        alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    // Getting permission for calling
    public void getPermissionMakeCall(String phone_number) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CALL_PHONE},
                        Constant.CALL_PHONE);
            } else {
                makeCall(phone_number);
            }
        } else makeCall(phone_number);
    }

    // Make call
    private void makeCall(String phone_number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constant.CALL_PHONE:
                //getPermissionMakeCall("1234567890");
                break;

            case Constant.MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayCurrentLocation();
                } else {
                    displayCurrentLocation();
                }
                break;
        }
    }

    // Alert Dialog for calling
    private void yesToCallDialog(String msg, final String phone_number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Make Call");
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getPermissionMakeCall(phone_number);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Make Call");
        alert.show();

    }

    // Api call to get order delivery info
    synchronized private void getRequestMethod() {
        main_view.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);
                        Log.e("REQUEST_METHOD", response.toString());
                        progress.setVisibility(View.GONE);
                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {
                            Gson gson = new Gson();
                            requestInfo = gson.fromJson(response, RequestInfo.class);
                            session.createRequest(requestInfo);

                           /* mContext.stopService(new Intent(mContext, MyService.class));*/

                            if (requestInfo.data.requestStatus != null) {
                                if (requestInfo.data.requestStatus.equals("1")) {
                                    btn_accept.setVisibility(View.GONE);
                                    btn_reject.setVisibility(View.GONE);
                                    btn_start_route.setVisibility(View.VISIBLE);

                                    requestInfo.data.requestStatus = "0";
                                    session.createRequest(requestInfo);

                                    isAccpt = true;
                                    setData(requestInfo);

                                } else {
                                    setData(requestInfo);
                                }

                                if (requestInfo.data.driverInfo.onDuty != null) {
                                    if (requestInfo.data.driverInfo.onDuty.equals("0")) {
                                        onDutyMethod();
                                    }
                                }

                                session.setIsOffRouteLodingDetails("");
                                session.setIsOffRouteDeliveryDetails("");
                            } else {
                                tv_no_request.setVisibility(View.VISIBLE);
                            }

                            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                AppHelper.startAlarmService(mContext);
                            }else {
                                Intent trackIntent = new Intent(mContext, MyService.class);
                                mContext.startService(trackIntent);
                            }


                        } else {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ErrorDialog.showSessionError(error, mContext);
                    progress.setVisibility(View.GONE);
                }
            });
            api.callApi("user/delivery", Request.Method.GET, null, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Set data from response of delivery order
    private void setData(RequestInfo requestInfo) {
        if (requestInfo.data.invoiceOrderId != null) {
            tv_pitAddress.setText(requestInfo.data.pitInfo.pitAddress);
            tv_order_number.setText(requestInfo.data.invoiceOrderId);
            tv_custmor_name.setText(requestInfo.data.orderInfo.customerName);
            tv_delivery_add.setText(requestInfo.data.orderInfo.deliveryAddress);
            tv_quentity.setText("" + requestInfo.data.productInfo.quantity + " " + requestInfo.data.productInfo.unitType);
            tv_product.setText(requestInfo.data.productInfo.productName);

            if (requestInfo.data.orderInfo.paymentMode != null) {
                if (requestInfo.data.orderInfo.paymentMode.equals("COD")) {
                    tv_payment_method.setText("C.O.D");
                } else {
                    tv_payment_method.setText(requestInfo.data.orderInfo.paymentMode);
                }
            }

            main_view.setVisibility(View.VISIBLE);
            ly_new_task_status.setVisibility(View.VISIBLE);
            tv_no_request.setVisibility(View.GONE);

            // Display Shifts
            numberofshift = Integer.parseInt(requestInfo.data.completeShift) + 1;
            if (numberofshift == 0) {
                numberofshift = numberofshift + 1;
            } else if (numberofshift <= Integer.parseInt(requestInfo.data.totalShift)) {
                numberofshift = (Integer.parseInt(requestInfo.data.completeShift) + 1);
            }

            if (requestInfo.data.totalShift.equals("1")) {
                rl_shift_description.setVisibility(View.GONE);
            } else if (Integer.parseInt(requestInfo.data.totalShift) > 1 && numberofshift <= Integer.parseInt(requestInfo.data.totalShift)) {
                rl_shift_description.setVisibility(View.VISIBLE);
                tv_shift.setText(numberofshift + "/" + requestInfo.data.totalShift);
            }

            // Display customer payment status
            if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                rl_customer_payment_status.setVisibility(View.VISIBLE);
            } else if (requestInfo.data.orderInfo.customerPaymentStatus.equals("0")) {
                rl_customer_payment_status.setVisibility(View.GONE);
            }


            tv_amount.setText("$" + requestInfo.data.orderInfo.totalAmount);
            tv_pit_name.setText(requestInfo.data.pitInfo.pitName);
            tv_po_number.setText(requestInfo.data.mainOrderId);

            // Set distance in miles from pit to delivery location
            tv_Pit_to_delivery.setText(requestInfo.data.orderInfo.deliveryDistance + " " + "Miles from PIT location");

            // Set distance in miles from current location to pit
            setDistanceCurrentToPit(mLatitude, mLongitude);

        } else {
            main_view.setVisibility(View.GONE);
            ly_new_task_status.setVisibility(View.GONE);
            tv_no_request.setVisibility(View.VISIBLE);
        }
    }

    // Set distance from current location to pit in miles
    private void setDistanceCurrentToPit(String latitude, String longitude) {
        Location startPoint = new Location("locationA");
        if (latitude != null && longitude != null) {
            startPoint.setLatitude(Double.parseDouble(latitude));
            startPoint.setLongitude(Double.parseDouble(longitude));
        }

        Location endPoint = new Location("locationA");
        endPoint.setLatitude(Double.parseDouble(requestInfo.data.pitInfo.pitLatitude));
        endPoint.setLongitude(Double.parseDouble(requestInfo.data.pitInfo.pitLongitude));

        double distance = startPoint.distanceTo(endPoint);

        // Set distance in miles from current location to pit
        tv_pitPlaceHolder.setText(String.format("%.2f", (distance * 0.000621371)) + " " + "Miles from your location");
    }

    // Api call to accept and reject delivery request
    private void accptRejectMethod(String deliveryId, String status, final View view) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            Map<String, String> map = new HashMap<>();
            map.put("deliveryId", deliveryId);
            map.put("status", status);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);
                        Log.e("ACCEPT REJECT RESPONSE", response.toString());

                        String status = js.getString("status");
                        String massage = js.getString("message");
                        String data = js.getString("data");

                        if (status.equals("success")) {
                            if (data.equals("accept")) {
                                btn_accept.setVisibility(View.GONE);
                                btn_reject.setVisibility(View.GONE);
                                ly_new_task_status.setVisibility(View.VISIBLE);

                                btn_start_route.setVisibility(View.VISIBLE);

                                isAccpt = true;
                                getRequestMethod();

                            /*requestInfo.data.requestStatus = "1";

                            session.createRequest(requestInfo);*/

                            } else if (data.equals("reject")) {
                                main_view.setVisibility(View.GONE);
                                tv_no_request.setVisibility(View.VISIBLE);
                                ly_new_task_status.setVisibility(View.GONE);
                            } else if (data.equals("fail")) {
                                main_view.setVisibility(View.GONE);
                                tv_no_request.setVisibility(View.VISIBLE);
                            }

                        } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();

                    } catch (
                            JSONException e)

                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callApi("user/deliveryRequest", Request.Method.POST, map, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Dialog to open Start route to pit or remember for 5 mins
    private void openStartRouteDialog(final Context mContext) {
        start_route_dialog = new Dialog(mContext);
        start_route_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        start_route_dialog.setContentView(R.layout.dialog_start_route_to_the_pit);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(start_route_dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        start_route_dialog.getWindow().setAttributes(lWindowParams);

        final TextView start_route_button = start_route_dialog.findViewById(R.id.start_route_button);
        final TextView remember_me_button = start_route_dialog.findViewById(R.id.remember_me_button);

        start_route_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cancel timer broadcast pending intent
                cancelPendingTask();
                requestInfo.data.requestStatus = "1";
                session.createRequest(requestInfo);

                startStopRoute();

                boolean isAppInstalledWaze = appInstalledOrNot("com.waze");
                boolean isAppInstalledGmail = appInstalledOrNot("com.google.android.apps.maps");

                String pitLat = requestInfo.data.pitInfo.pitLatitude;
                String pitLng = requestInfo.data.pitInfo.pitLongitude;

                if (DNG.countDownTimer != null) {
                    DNG.countDownTimer.cancel();
                }
                if ((tv_timer.getVisibility() == View.VISIBLE) && (!tv_timer.getText().toString().equals(""))) {
                    tv_timer.setVisibility(View.GONE);
                    tv_timer.setText("");
                }

                if (session.getDefaultMap() == 0) {
                    //googleMap
                    if (isAppInstalledGmail) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pitLat + ",+" + pitLng);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (isAdded()) {
                            startActivityForResult(mapIntent, Constant.mapRequestCode);
                        }

                        start_route_dialog.dismiss();
                    } else {
                        Toast.makeText(mContext, "google map is not installed please install first", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    //Waze
                    if (isAppInstalledWaze) {
                        String uri = "waze://?ll=" + pitLat + ", " + pitLng + "&navigate=yes";
                        if (isAdded()) {
                            startActivityForResult(new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse(uri)), Constant.wazeRequestCode);
                        }

                        start_route_dialog.dismiss();
                    } else {
                        Toast.makeText(mContext, "Waze is not installed please install first", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });

        remember_me_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (DNG.countDownTimer != null) {
                    DNG.countDownTimer.cancel();
                    startRemainingTimer();
                } else {
                    startRemainingTimer();
                }
                start_route_dialog.dismiss();
            }
        });
        //start_route_dialog.getWindow().setGravity(Gravity.CENTER);
        start_route_dialog.show();
    }

    // Timer for remember for 5 mins
    private void startRemainingTimer() {
        // Timer for 5min
        if (DNG.countDownTimer != null) {
            DNG.countDownTimer.cancel();
            DNG.countDownTimer = null;
        }

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (300003), pendingIntent);
        }

        DNG.countDownTimer = new CountDownTimer(300000, 1000) { //300000
            public void onTick(long millis) {
                milles = millis;
                tv_timer.setVisibility(View.VISIBLE);
                tv_timer.setText("" + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
            }

            public void onFinish() {
                if (DNG.isActivityVisible()) {
                    cancelPendingTask();
                }

                tv_timer.setText("");
                tv_timer.setVisibility(View.GONE);
                if (mContext != null && isAdded()) {
                    openStartRouteDialog(mContext);
                }
            }
        };
        DNG.countDownTimer.start();

    }

    // Cancel timer broadcast pending intent
    private void cancelPendingTask() {
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void onDutyMethod() {
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

                            User user;
                            if (dutyStatus.equals("1")) {

                                user = session.getUser();
                                user.data.onDuty = 1;
                                session.createSession(user);

                            } else {
                                user = session.getUser();
                                user.data.onDuty = 0;
                                session.createSession(user);

                            }


                        } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ErrorDialog.showSessionError(error, mContext);
                }

            });
            api.callApi("user/duty", Request.Method.GET, null);

        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayCurrentLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayCurrentLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Display current location using Fused Location Api
    synchronized private void displayCurrentLocation() {
        if (LocationRuntimePermission.checkLocationPermission(mContext)) {

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if (isGPSEnabled) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    mLatitude = String.valueOf(mLastLocation.getLatitude());
                    mLongitude = String.valueOf(mLastLocation.getLongitude());
                }
            } else if (!isNetworkEnabled) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setCancelable(false);
                builder.setMessage("Please Enable GPS");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        dialogInterface.dismiss();
                    }
                });
                android.app.AlertDialog alert = builder.create();
                alert.show();

            } else if (!isGPSEnabled && !isNetworkEnabled) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setCancelable(false);
                builder.setMessage("Please Enable Network and GPS");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        dialogInterface.dismiss();
                    }
                });
                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    /**
     * Method to verify google play services on the device
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) getActivity());
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // Api call to set ON Route or OFF Route
    private void startStopRoute() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {
                            String route = js.getString("route");

                            if (route.equals("ON ROUTE")) {
                                startService();
                            } else if (route.equals("OFF ROUTE")) {
                                session.getRequestInfo().data.driverInfo.onRoute = "0";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "0";
                                session.createRequest(requestInfo);
                                startStopRoute();

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
                    ErrorDialog.showSessionError(error, mContext);
                }

            });
            api.callApi("user/route", Request.Method.GET, null, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Start My service
    private void startService() {
        session.getRequestInfo().data.driverInfo.onRoute = "1";
        RequestInfo requestInfo = session.getRequestInfo();
        requestInfo.data.driverInfo.onRoute = "1";
        session.createRequest(requestInfo);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(mContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + mContext.getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                    AppHelper.startAlarmService(mContext);
                }else {
                    mContext.stopService(new Intent(mContext, MyService.class));
                    Intent intent = new Intent(mContext, MyService.class);
                    mContext.startService(intent);
                }
            }
        } else {

            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                AppHelper.startAlarmService(mContext);
            }else {
                mContext.stopService(new Intent(mContext, MyService.class));
                Intent intent = new Intent(mContext, MyService.class);
                mContext.startService(intent);
            }
        }
    }


    @Override
    public void sendData(String lat, String lng) {
        mLatitude = lat;
        mLongitude = lng;

        if (requestInfo != null) {
            if (requestInfo.data.pitInfo.pitLatitude != null && requestInfo.data.pitInfo.pitLongitude != null) {
                setDistanceCurrentToPit(mLatitude, mLongitude);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.setListner(null);
    }


}
