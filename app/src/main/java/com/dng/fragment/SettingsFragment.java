package com.dng.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;

import com.dng.helper.AppHelper;
import com.dng.helper.TimerAlertBroadcastReceiver;
import com.dng.model.RequestInfo;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.ALARM_SERVICE;

public class SettingsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RelativeLayout ly_logout, ly_default_map, ly_profile;
    private Session app_session;

    private String mParam1;
    private String mParam2;
    Context mContext;

    public SettingsFragment() {
    }


    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        app_session = new Session(mContext);

        ly_profile = view.findViewById(R.id.ly_profile);
        ly_logout = view.findViewById(R.id.ly_logout);
        ly_default_map = view.findViewById(R.id.ly_default_map);

        ly_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        ly_default_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseMapType();
            }
        });

        ly_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new ProfileTerminalFragment(), false, R.id.fragment_place);
            }
        });

        return view;
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
        this.mContext = context;
    }

    private void chooseMapType() {
        int checkItem;

        if (app_session.getDefaultMap() == 0) {
            checkItem = 0;
        } else {
            checkItem = 1;
        }

        final CharSequence[] items = {"Google Map", "Waze"};

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("Select Default Map")
                .setSingleChoiceItems(items, checkItem, null)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        app_session.setDefaultMap(selectedPosition);

                    }
                })
                .show();

    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Do you want to logout")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        logout();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Logout...");
        alert.show();

    }

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

                            if (app_session != null && app_session.getRequestInfo().data.driverInfo != null) {
                                if (app_session.getRequestInfo().data.driverInfo.onRoute != null && !app_session.getRequestInfo().data.driverInfo.onRoute.equals("")) {
                                    if (app_session.getRequestInfo().data.driverInfo.onRoute.equals("1")) {
                                        startStopRoute();
                                    }
                                }
                            }

                            app_session.logout();

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
                        app_session.logout();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    app_session.logout();
                }

            });
            api.callApi("user/logout", Request.Method.GET, null);

        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

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
                                if (app_session.getRequestInfo() != null) {
                                    app_session.getRequestInfo().data.driverInfo.onRoute = "1";
                                    RequestInfo requestInfo = app_session.getRequestInfo();
                                    requestInfo.data.driverInfo.onRoute = "1";
                                    app_session.createRequest(requestInfo);
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
                                if (app_session.getRequestInfo() != null) {
                                    app_session.getRequestInfo().data.driverInfo.onRoute = "0";
                                    RequestInfo requestInfo = app_session.getRequestInfo();
                                    requestInfo.data.driverInfo.onRoute = "0";
                                    app_session.createRequest(requestInfo);
                                }
                            }

                        } else {
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
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
            api.callApi("user/route", Request.Method.GET, null, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }

    }
}
