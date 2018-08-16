package com.dng.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.CustomListner.SendLocation;
import com.dng.R;
import com.dng.app.DNG;
import com.dng.fragment.DeliveryDetailsFragment;
import com.dng.fragment.HistoryFragment;
import com.dng.fragment.LoadingDetailsFragment;
import com.dng.fragment.NewTaskFragment;
import com.dng.fragment.SettingsFragment;
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.model.RequestInfo;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Fragment fr;
    private LinearLayout ly_history;
    private LinearLayout ly_new_task;
    private LinearLayout iv_manager_tab;
    private LinearLayout ly_logout;

    private ImageView iv_newtask_tab;
    private ImageView iv_history_tab;
    private ImageView iv_setting_tab;

    boolean doubleBackToExitPressedOnce;
    private Runnable runnable;

    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1000 * 20;
    private static final long FASTEST_INTERVAL = 1000 * 10;
    public static String current_lat;
    public static String current_lng;
    private static SendLocation sendLocation;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Session app_session, session;

    private TextView tv_newtask_tab;
    private TextView tv_history_tab;
    private TextView tv_setting_tab;
    private RequestInfo requestInfo;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new Session(this);
          requestInfo = session.getRequestInfo();
        init();

        if (getIntent().getStringExtra("type") != null) {
            String type = getIntent().getStringExtra("type");

            if (!type.equals("")) {
                if (type.equals("deliveryRequest")) {  // You have a new delivery request
                    replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);

                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("nearPit")) {   // You are near to pit location.
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        AppHelper.startAlarmService(MainActivity.this);
                    }else {
                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        startService(intent);
                    }

                    startStopRoute();
                    replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);

                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("pitPaymentServer") || type.equals("pitPayment")) {  // Your assigned delivery reqeust, pit payment manually completed by office manager.
                    startStopRoute();

                    if (requestInfo != null) {
                        if (requestInfo.data.pitInfo.pitPayStatus != null) {
                            requestInfo.data.pitInfo.pitPayStatus = "1";

                            // If pit payment done from CRM
                            if (type.equals("pitPayment")) {
                                int index = requestInfo.data.pitInfo.pitPay.pay.size();
                                RequestInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX payBean = new RequestInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX();
                                payBean.driverId = "";
                                payBean.driverName = "office Manager";
                                payBean.paymentType = "FULL";
                                payBean.paymentMode = "CASH";
                                payBean.amount = String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount);
                                payBean.receipt = "";
                                payBean.description = "Payment paid manually by CRM";
                                payBean.createDate = "";

                                requestInfo.data.pitInfo.pitPay.pay.add(index, payBean);
                            }
                        }

                        session.createRequest(requestInfo);
                    }

                    if (session.getScreen().equals("NewTaskFragment")) {
                        replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);
                    } else if (session.getScreen().equals("LoadingDetailsFragment")) {
                        replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                    } else if (session.getScreen().equals("DeliveryDetailsFragment")) {
                        replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                    }

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);
                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("shiftCompleted")) {  // Your assigned delivery request, 1 shift manually completed by office manager.

                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        AppHelper.stopAlarmService(MainActivity.this);
                    }else {
                        stopService(new Intent(MainActivity.this, MyService.class));
                    }

                    replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);

                    User user = session.getUser();
                    session.clearSessionNewUser();
                    session.createSession(user);

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);
                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("customerPaymentServer") || type.equals("customerPayment")) {  // Your assigned delivery reqeust, customer payment manually completed by office manager.
                    startStopRoute();

                    if (requestInfo != null) {
                        if (requestInfo.data.orderInfo.customerPaymentStatus != null) {
                            requestInfo.data.orderInfo.customerPaymentStatus = "1";

                            // If customer payment done from CRM
                            if (type.equals("customerPayment")) {
                                int index = requestInfo.data.orderInfo.customerPay.pay.size();
                                RequestInfo.DataBean.OrderInfoBean.CustomerPayBean.PayBean payBean = new RequestInfo.DataBean.OrderInfoBean.CustomerPayBean.PayBean();
                                payBean.driverId = "";
                                payBean.driverName = "office Manager";
                                payBean.paymentType = "FULL";
                                payBean.paymentMode = "CASH";
                                payBean.amount = String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount);
                                payBean.receipt = "";
                                payBean.description = "Payment paid manually by CRM";
                                payBean.createDate = "";

                                requestInfo.data.orderInfo.customerPay.pay.add(index, payBean);
                            }
                        }
                        session.createRequest(requestInfo);
                    }

                    if (session.getScreen().equals("NewTaskFragment")) {
                        replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);
                    } else if (session.getScreen().equals("LoadingDetailsFragment")) {
                        replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                    } else if (session.getScreen().equals("DeliveryDetailsFragment")) {
                        replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                    }

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);
                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("deliveryTimeout")) { // Your delivery request cancelled by office.    &&   // Your delivery request time out
                    replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);
                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                } else if (type.equals("ok")) { // On click on near customer area pop up
                    startStopRoute();
                    replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);

                    iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                    iv_history_tab.setImageResource(R.drawable.history);
                    iv_setting_tab.setImageResource(R.drawable.settings);

                    ly_new_task.setEnabled(false);
                    ly_history.setEnabled(true);
                    iv_manager_tab.setEnabled(true);
                    ly_logout.setEnabled(true);

                    tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                    tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));
                } else if (type.equals("LaunchAppAlarm")) { // Timer broadcast
                    ly_new_task.callOnClick();
                }
            }
        } else {
            ly_new_task.callOnClick();
        }


        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void updateFragment(Fragment fragment){
        /*FragmentManager fm = getSupportFragmentManager();
        List<?> fragments = fm.getFragments();*/

        if(fragment!=null){
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(fragment);
            ft.attach(fragment);
            ft.commit();
        }

      /*  if(fragment.getClass().getName().equals(LoadingDetailsFragment.TAG)){

        }else if(fragment.getClass().getName().equals(LoadingDetailsFragment.TAG)){

        }
*/
    }




    public Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.fragment_place);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent1) {
           String type = intent1.getStringExtra("type");


            if (type.equals("pitPaymentServer") || type.equals("pitPayment")) {  // Your assigned delivery reqeust, pit payment manually completed by office manager.
                startStopRoute();

                if (requestInfo != null) {
                    if (requestInfo.data.pitInfo.pitPayStatus != null) {
                        requestInfo.data.pitInfo.pitPayStatus = "1";

                        // If pit payment done from CRM
                        if (type.equals("pitPayment")) {
                            int index = requestInfo.data.pitInfo.pitPay.pay.size();
                            RequestInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX payBean = new RequestInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX();
                            payBean.driverId = "";
                            payBean.driverName = "office Manager";
                            payBean.paymentType = "FULL";
                            payBean.paymentMode = "CASH";
                            payBean.amount = String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount);
                            payBean.receipt = "";
                            payBean.description = "Payment paid manually by CRM";
                            payBean.createDate = "";

                            requestInfo.data.pitInfo.pitPay.pay.add(index, payBean);
                        }
                    }

                    session.createRequest(requestInfo);
                }

                /*if (session.getScreen().equals("NewTaskFragment")) {
                    replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);
                } else if (session.getScreen().equals("LoadingDetailsFragment")) {
                    replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                } else if (session.getScreen().equals("DeliveryDetailsFragment")) {
                    replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                }*/

                iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                iv_history_tab.setImageResource(R.drawable.history);
                iv_setting_tab.setImageResource(R.drawable.settings);

                ly_new_task.setEnabled(false);
                ly_history.setEnabled(true);
                iv_manager_tab.setEnabled(true);
                ly_logout.setEnabled(true);

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

            } else if (type.equals("customerPaymentServer") || type.equals("customerPayment")) {  // Your assigned delivery reqeust, customer payment manually completed by office manager.
                startStopRoute();

                if (requestInfo != null) {
                    if (requestInfo.data.orderInfo.customerPaymentStatus != null) {
                        requestInfo.data.orderInfo.customerPaymentStatus = "1";

                        // If customer payment done from CRM
                        if (type.equals("customerPayment")) {
                            int index = requestInfo.data.orderInfo.customerPay.pay.size();
                            RequestInfo.DataBean.OrderInfoBean.CustomerPayBean.PayBean payBean = new RequestInfo.DataBean.OrderInfoBean.CustomerPayBean.PayBean();
                            payBean.driverId = "";
                            payBean.driverName = "office Manager";
                            payBean.paymentType = "FULL";
                            payBean.paymentMode = "CASH";
                            payBean.amount = String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount);
                            payBean.receipt = "";
                            payBean.description = "Payment paid manually by CRM";
                            payBean.createDate = "";

                            requestInfo.data.orderInfo.customerPay.pay.add(index, payBean);
                        }
                    }
                    session.createRequest(requestInfo);
                }

               /* if (session.getScreen().equals("NewTaskFragment")) {
                    replaceFragment(NewTaskFragment.newInstance("", ""), false, R.id.fragment_place);
                } else if (session.getScreen().equals("LoadingDetailsFragment")) {
                    replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                } else if (session.getScreen().equals("DeliveryDetailsFragment")) {
                    replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                }
*/
                iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                iv_history_tab.setImageResource(R.drawable.history);
                iv_setting_tab.setImageResource(R.drawable.settings);

                ly_new_task.setEnabled(false);
                ly_history.setEnabled(true);
                iv_manager_tab.setEnabled(true);
                ly_logout.setEnabled(true);

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

            }
            updateFragment(getCurrentFragment());
        }
    };

    // Api call to start or stop route of driver
    private void startStopRoute() {
        WebService api = new WebService(this, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);

                    String status = js.getString("status");
                    String massage = js.getString("message");

                    if (status.equals("success")) {

                        String route = js.getString("route");

                        if (route.equals("ON ROUTE")) {
                            if (session.getRequestInfo() != null) {
                                session.getRequestInfo().data.driverInfo.onRoute = "1";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "1";
                                session.createRequest(requestInfo);
                            }

                            if (Build.VERSION.SDK_INT >= 23) {
                                if (!Settings.canDrawOverlays(MainActivity.this)) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 1234);
                                }
                            }

                        } else if (route.equals("OFF ROUTE")) {
                            if (session.getRequestInfo() != null) {
                                session.getRequestInfo().data.driverInfo.onRoute = "0";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "0";
                                session.createRequest(requestInfo);
                            }

                        }

                    } else {
                        Toast.makeText(MainActivity.this, massage, Toast.LENGTH_SHORT).show();
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
    }

    private void init() {
        ly_new_task = findViewById(R.id.ly_new_task);
        ly_history = findViewById(R.id.ly_history);

        iv_manager_tab = findViewById(R.id.iv_manager_tab);
        ly_logout = findViewById(R.id.ly_logout);


        iv_newtask_tab = findViewById(R.id.iv_newtask_tab);
        iv_history_tab = findViewById(R.id.iv_history_tab);
        iv_setting_tab = findViewById(R.id.iv_setting_tab);

        app_session = new Session(this);

        ly_history.setOnClickListener(this);
        ly_new_task.setOnClickListener(this);
        iv_manager_tab.setOnClickListener(this);
        ly_logout.setOnClickListener(this);


        iv_history_tab.setImageResource(R.drawable.history);
        iv_newtask_tab.setImageResource(R.drawable.new_task);
        iv_setting_tab.setImageResource(R.drawable.settings);

        tv_newtask_tab = findViewById(R.id.tv_newtask_tab);
        tv_history_tab = findViewById(R.id.tv_history_tab);
        tv_setting_tab = findViewById(R.id.tv_setting_tab);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ly_new_task: {
                app_session = new Session(MainActivity.this);
                String screen = app_session.getScreen();

                switch (screen) {
                    case "NewTaskFragment": {
                        replaceFragment(NewTaskFragment.newInstance(current_lat, current_lng), false, R.id.fragment_place);
                        break;
                    }

                    case "LoadingDetailsFragment": {
                        replaceFragment(LoadingDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                        break;
                    }

                    case "DeliveryDetailsFragment": {
                        replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                        break;
                    }
                    case "": {
                        replaceFragment(NewTaskFragment.newInstance(current_lat, current_lng), false, R.id.fragment_place);
                        break;
                    }
                }

                // Setting active NewTask on bottom task layout
                iv_newtask_tab.setImageResource(R.drawable.active_new_task);
                iv_history_tab.setImageResource(R.drawable.history);
                iv_setting_tab.setImageResource(R.drawable.settings);

                ly_new_task.setEnabled(true);
                ly_history.setEnabled(true);
                iv_manager_tab.setEnabled(true);
                ly_logout.setEnabled(true);

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                break;
            }

            case R.id.ly_history: {
                fr = new HistoryFragment();
                replaceFragment(fr, false, R.id.fragment_place);

                // Setting active History on bottom task layout
                iv_history_tab.setImageResource(R.drawable.active_history);
                iv_newtask_tab.setImageResource(R.drawable.new_task);
                iv_setting_tab.setImageResource(R.drawable.settings);

                ly_new_task.setEnabled(true);
                ly_history.setEnabled(false);
                iv_manager_tab.setEnabled(true);
                ly_logout.setEnabled(true);

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_history_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                break;
            }

            case R.id.iv_manager_tab: {
                ly_new_task.setEnabled(true);
                ly_history.setEnabled(true);
                iv_manager_tab.setEnabled(false);
                ly_logout.setEnabled(true);
                getManagerPhoneNumber();

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.grey));

                break;
            }

            case R.id.ly_logout: {
                fr = new SettingsFragment();
                replaceFragment(fr, false, R.id.fragment_place);

                // Setting active Settings on bottom task layout
                iv_history_tab.setImageResource(R.drawable.history);
                iv_newtask_tab.setImageResource(R.drawable.new_task);
                iv_setting_tab.setImageResource(R.drawable.active_settings);

                ly_new_task.setEnabled(true);
                ly_history.setEnabled(true);
                iv_manager_tab.setEnabled(true);
                ly_logout.setEnabled(false);

                tv_newtask_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_history_tab.setTextColor(getResources().getColor(R.color.grey));
                tv_setting_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            }
        }
    }


    @Override
    public void onBackPressed() {
        Handler handler = new Handler();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

        } else if (!doubleBackToExitPressedOnce) {

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            // Util.showSnakbar(container, "Click again to exit", font);
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {/*super.onBackPressed();*/
            super.onBackPressed();
        }

    }

    private void yesToCallDialog(String msg, final String phone_number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    public void getPermissionMakeCall(String phone_number) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CALL_PHONE},
                        Constant.CALL_PHONE);
            } else {
                makeCall(phone_number);
            }
        } else makeCall(phone_number);
    }

    private void makeCall(String phone_number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;

        if (null != mCurrentLocation) {
            current_lat = String.valueOf(mCurrentLocation.getLatitude());
            current_lng = String.valueOf(mCurrentLocation.getLongitude());

            if (sendLocation != null) {
                sendLocation.sendData(MainActivity.current_lat, MainActivity.current_lng);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DNG.activityPaused();
        stopLocationUpdates();

    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        DNG.activityResumed();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("com.dng"));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel timer when activity is destroyed
        if (DNG.countDownTimer != null) {
            DNG.countDownTimer.cancel();
            DNG.countDownTimer = null;
        }
    }

    // Method defined to replace fragment
    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getSupportFragmentManager();
       /* int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }*/
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    // Listener to send location to New task Fragment
    public static void setListner(SendLocation listner) {
        sendLocation = listner;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void getManagerPhoneNumber() {
        if (AppHelper.isConnectingToInternet(MainActivity.this)) {
            WebService api = new WebService(MainActivity.this, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);
                        iv_manager_tab.setEnabled(true);
                        String status = js.getString("status");
                        String massage = js.getString("message");
                        String data = js.getString("message");
                        String contact = js.getString("contact");

                        if (status.equals("success")) {

                            yesToCallDialog(massage, contact);

                        } else{

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        iv_manager_tab.setEnabled(true);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    iv_manager_tab.setEnabled(true);
                }

            });
            api.callApi("user/managerInfo", Request.Method.GET, null);

        } else {
            iv_manager_tab.setEnabled(true);
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }
}
