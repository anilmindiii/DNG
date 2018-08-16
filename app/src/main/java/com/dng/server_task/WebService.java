package com.dng.server_task;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dng.R;
import com.dng.VolleyRequest.VolleyMultipartRequest;
import com.dng.VolleyRequest.VolleySingleton;

import com.dng.app.DNG;
import com.dng.helper.AppHelper;
import com.dng.service.MyService;
import com.dng.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebService {

    private Context mContext;
    private String TAG;

    private Session session;
    private ResponceListner mListener;
    private LoginRegistrationListener mLSListener;

/*    private ResponseApi.Listener mListener;
    private ResponseApi.LoginRegistrationListener mLSListener;*/


    public WebService(Context context, String TAG, ResponceListner listener) {
        super();
        mListener = listener;
        this.mContext = context;
        this.TAG = TAG;
        session = new Session(mContext);
        //progressDialog = new ProgressDialog(context);
    }


    /* Gatting artist profile from server */
    public void LoginTask(final Map<String, String> params) {
        final String loginUrl = API.BASE_URL + "userLogin";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        mLSListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLSListener.ErrorListener(error);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        DNG.getInstance().addToRequestQueue(stringRequest, TAG);
    }


    /* Gatting artist profile from server */
    public void signUpTask(final Map<String, String> params, final Bitmap bitmap) {
        final String signUpUrl = API.BASE_URL + "userRegistration";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, signUpUrl, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                mLSListener.onResponse(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;

            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                if (bitmap != null) {
                    params.put("profileImage", new DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                }
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
        //ImLink.getInstance().addToRequestQueue(multipartRequest, TAG);
    }


    public void callMultiPartApi(final String url, final Map<String, String> params) {
        callMultiPartApi(url, params, null);
    }

    // for image
    public void callMultiPartApi(final String url, final Map<String, String> params, final Map<String, Bitmap> bitmapList) {
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                API.BASE_URL + url, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                mListener.onResponse(resultResponse, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
                handleError(error);
                mListener.ErrorListener(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authToken", session.getAuthToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                for (Map.Entry<String, Bitmap> entry : bitmapList.entrySet()) {
                    String key = entry.getKey();
                    Bitmap bitmap = entry.getValue();
                    params.put(key, new DataPart(key.concat(".jpg"), AppHelper.getFileDataFromDrawable(bitmap), "image/png"));
                }

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
        //ImLink.getInstance().addToRequestQueue(multipartRequest, TAG);
    }

    public String getNetworkMessage(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = null;
                if (isJSONValid(result)) {
                    response = new JSONObject(result);
                }

                String status = "";
                String message = "";

                if (ServerResponseCode.getmeesageCode(networkResponse.statusCode).equals("Ok")) {
                    if (response.has("status")) status = response.getString("status");
                    if (response.has("message")) message = response.getString("message");
                    Log.e("Error Status", "" + status);
                    Log.e("Error Message", message);
                } else {
                    errorMessage = ServerResponseCode.getmeesageCode(networkResponse.statusCode);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        return errorMessage;
    }

    private static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
            // If JsonObject is ok then check for JSONArray
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        }
    }


    public void callApi(final String url, int Method, final Map<String, String> params) {
        callApi(url, Method, params, false);
    }


    public void callApi(final String url, int Method, final Map<String, String> params, final boolean isSelfErrorHandle) {
        //  Progress.DisplayLoader(mContext);
        StringRequest stringRequest = new StringRequest(Method, API.BASE_URL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        // Progress.HideLoader(mContext);
                        mListener.onResponse(response, url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Progress.HideLoader(mContext);
                        if (isSelfErrorHandle){
                            handleError(error);
                        }
                            //handleError(error);

                        mListener.ErrorListener(error);

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Map<String, String> params = new HashMap<>();
                //params.put("fireBaseId", id);
                if (params == null)
                    return super.getParams();
                else return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                if (session.isLoggedIn()) {
                    if (session.getAuthToken() != null) {
                        header.put("authToken", session.getAuthToken());
                    } else {
                        header.put("authToken", "");
                    }
                }
                return header;
            }
        };
        DNG.getInstance().addToRequestQueue(stringRequest, TAG);
    }

    public interface ResponceListner {
        void onResponse(String responce, String url);

        void ErrorListener(VolleyError error);
    }

    public void setListner(ResponceListner listner) {
        mListener = listner;
    }


    public interface LoginRegistrationListener {
        /**
         * Called when a response is received.
         */
        void onResponse(String response);

        void ErrorListener(VolleyError error);
    }

    public void setLoginRegisterListner(LoginRegistrationListener LoginRegistrationListener) {
        mLSListener = LoginRegistrationListener;
    }


    private void handleError(VolleyError error) {
        handleError(mContext, error);
    }


    private void handleError(Context context, VolleyError error) {
        if (error != null && error.networkResponse != null) {

            try {
                if (error.networkResponse.statusCode == 400) {
                    // Dialog dialog = new Dialog(context);
                    //Toast.makeText(mContext, "Session expired", Toast.LENGTH_SHORT).show();
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        AppHelper.stopAlarmService(mContext);
                    }else {
                        Intent intent  = new Intent(mContext, MyService.class);
                        mContext.stopService(intent);
                    }

//                   showSessionError("session expired", "Your current session has been expired.", "Ok");
                } /* }else {
                    MySnackBar.show(WebServiceAPI.getNetworkMessage(error));
                }*/
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

        }
    }

    public void showSessionError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("session expired");
        builder.setMessage("Your current session has been expired.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Session session = new Session(mContext);
                session.sessionExpireLogout();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
