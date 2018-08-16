package com.dng.alarmServiceForOreo;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.activity.MainActivity;
import com.dng.model.RequestInfo;
import com.dng.server_task.WebService;
import com.dng.session.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class AlarmReceiver extends BroadcastReceiver  implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
  /*  private static final long INTERVAL = 1000 * 20;
    private static final long FASTEST_INTERVAL = 1000 * 10;*/

    private static final long INTERVAL = 10000 * 3;
    private static final long FASTEST_INTERVAL = 3000 * 3;


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;

    Context mContext;
    Session session;
    RequestInfo requestInfo;
    Location endPoint = null;
    Location endPoint2 = null;
    String screenName = "", openCustomerPopUp = "";

    double current_lat, current_long;
    double pit_lat, pit_long, delivery_lat, delivery_long;
    double customerValueNearLocation;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // For our recurring task, we'll just display a message
       // Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();


        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        com.google.android.gms.common.api.PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
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
        session = new Session(mContext);
        requestInfo = session.getRequestInfo();
        screenName = session.getScreen();

        mCurrentLocation = location;
        if (null != mCurrentLocation) {
            current_lat = mCurrentLocation.getLatitude();
            current_long = mCurrentLocation.getLongitude();
           // Toast.makeText(mContext, "" + current_lat + "\n" + current_long, Toast.LENGTH_SHORT).show();
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
          //  mLastLocation.set(location);

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

                if (session.getPopUpForCustomerArea().equals("Yes")) {
                    if (customerValueNearLocation <= 200.00) {
                        requestInfo.isInCustomerArea = "yesInCustomerArea";
                        session.createRequest(requestInfo);
                    }
                }

                if (screenName != null) {
                    if (screenName.equals("LoadingDetailsFragment") && session.getPopUpForCustomerArea().equals("Yes")) {
                        if (!session.getIsDialogOpen() && customerValueNearLocation <= 200.00) {
                            dialog("You are nearby customer area.");
                            session.setScreen("DeliveryDetailsFragment");
                            session.setPopUpForCustomerArea("No");
                            Log.e("Customer", "You are nearby customer area");
                        }
                    }
                }
            }
        }

    }

    private void dialog(String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle("DNG")
                .setMessage(msg)
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setButton("launch app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent dialogIntent = new Intent(mContext, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // dialogIntent.putExtra("type", "ok");
                mContext.startActivity(dialogIntent);

                alertDialog.dismiss();
            }
        });


        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        session.setIsDialogOpen(true);
        alertDialog.show();
    }

    private void startTrackLocationApi() {
        WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);
                    Log.e("TRACK LOCATION", response.toString());

                    String inSidePit = js.getString("inSidePit");

                    if (inSidePit.equals("yes")) {
                        session.setScreen("LoadingDetailsFragment");
                          //  Toast.makeText(mContext, "Alarm Service Inside Pit Yes", Toast.LENGTH_SHORT).show();
                    } else {
                         //  Toast.makeText(mContext, "Alarm Service  Inside Pit No", Toast.LENGTH_SHORT).show();
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
}