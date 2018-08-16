package com.dng.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.activity.MainActivity;
import com.dng.model.RequestInfo;
import com.dng.server_task.WebService;
import com.dng.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MyService extends Service {
    private static final String TAG = "SNS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 30000; //30sec
    private static final float LOCATION_DISTANCE = 50; //50m
    Session session;
    RequestInfo requestInfo;
    Location endPoint = null;
    Location endPoint2 = null;
    String screenName = "", openCustomerPopUp = "";

    double current_lat, current_long;
    double pit_lat, pit_long, delivery_lat, delivery_long;
    double customerValueNearLocation;


    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            //session = new Session(getApplicationContext());
            screenName = session.getScreen();

            if (session.getRequestInfo() != null) {
                requestInfo = session.getRequestInfo();
            }

            startTrackLocationApi();

            Location startPoint = new Location("locationA");
            startPoint.setLatitude(location.getLatitude());
            startPoint.setLongitude(location.getLongitude());

            current_lat = location.getLatitude();
            current_long = location.getLongitude();

            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            if (requestInfo != null) {
                String pitLat = requestInfo.data.pitInfo.pitLatitude;
                String pitLng = requestInfo.data.pitInfo.pitLongitude;

                if (pitLat != null && pitLng != null) {
                    pit_lat = Double.parseDouble(requestInfo.data.pitInfo.pitLatitude);
                    pit_long = Double.parseDouble(requestInfo.data.pitInfo.pitLongitude);

                    endPoint = new Location("locationA");
                    endPoint.setLatitude(Double.parseDouble(pitLat));
                    endPoint.setLongitude(Double.parseDouble(pitLng));

                    double distance = startPoint.distanceTo(endPoint);
                    double pitValueNearLocation = Double.parseDouble(new DecimalFormat("##.##").format(distance));

                }

                String cust_lat = requestInfo.data.orderInfo.deliveryLatitude;
                String cust_lng = requestInfo.data.orderInfo.deliveryLongitude;

                if (cust_lat != null && cust_lng != null) {
                    delivery_lat = Double.parseDouble(requestInfo.data.orderInfo.deliveryLatitude);
                    delivery_long = Double.parseDouble(requestInfo.data.orderInfo.deliveryLongitude);

                    endPoint2 = new Location("locationA");
                    endPoint2.setLatitude(Double.parseDouble(cust_lat));
                    endPoint2.setLongitude(Double.parseDouble(cust_lng));

                    double distance2 = startPoint.distanceTo(endPoint2);
                    customerValueNearLocation = Double.parseDouble(new DecimalFormat("##.##").format(distance2));

                }

                if (openCustomerPopUp.equals("Yes")) {
                    if (customerValueNearLocation <= 200.00) {
                        requestInfo.isInCustomerArea = "yesInCustomerArea";
                        session.createRequest(requestInfo);
                    }
                }

                if (screenName != null) {
                    if (screenName.equals("LoadingDetailsFragment") && openCustomerPopUp.equals("Yes")) {
                        if (!session.getIsDialogOpen() && customerValueNearLocation <= 200.00) {
                            dialog("You are nearby customer area.");
                            session.setScreen("DeliveryDetailsFragment");
                            Log.e("Customer", "You are nearby customer area");
                        }


                    }
                }
            }
        }

        private void dialog(String msg) {
            final AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle("DNG")
                    .setMessage(msg)
                    .create();
            alertDialog.setCancelable(false);
            alertDialog.setButton("launch app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent dialogIntent = new Intent(getApplicationContext(), MainActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // dialogIntent.putExtra("type", "ok");
                    startActivity(dialogIntent);

                    alertDialog.dismiss();
                }
            });

            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            session.setIsDialogOpen(true);
            alertDialog.show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

    }

    private void startTrackLocationApi() {
        WebService api = new WebService(getApplicationContext(), "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);
                    Log.e("TRACK LOCATION", response.toString());

                    String status = js.getString("status");
                    String massage = js.getString("message");
                    String inSidePit = js.getString("inSidePit");

                  /*  Toast.makeText(MyService.this, "Track Api Running", Toast.LENGTH_SHORT).show();*/

                    if (inSidePit.equals("yes")) {
                        session.setScreen("LoadingDetailsFragment");
                        //    Toast.makeText(MyService.this, "Inside Pit Yes", Toast.LENGTH_SHORT).show();
                    } else {
                        //   Toast.makeText(MyService.this, "Inside Pit No", Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("error", e + "");
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        });

        if (screenName.equals("NewTaskFragment")) {
            if (requestInfo != null) {
                if (requestInfo.data.pitInfo.pitId != null && requestInfo.data.requestStatus.equals("1")) {
                    api.callApi("user/trackLocation?pitId=" + requestInfo.data.pitInfo.pitId + "&latitude=" + current_lat + "&longitude=" + current_long, Request.Method.GET, null, true);
                } else {
                    api.callApi("user/trackLocation?latitude=" + current_lat + "&longitude=" + current_long, Request.Method.GET, null, true);
                }
            } else {
                api.callApi("user/trackLocation?latitude=" + current_lat + "&longitude=" + current_long, Request.Method.GET, null, true);
            }
        } else {
            api.callApi("user/trackLocation?latitude=" + current_lat + "&longitude=" + current_long, Request.Method.GET, null, true);
        }
    }


    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            if (intent.getStringExtra("PopUpForCustomerArea") != null) {
                openCustomerPopUp = intent.getStringExtra("PopUpForCustomerArea");
            }
        }

        requestInfo = session.getRequestInfo();
        screenName = session.getScreen();
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        session = new Session(getApplicationContext());
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}