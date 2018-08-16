package com.dng.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.Validation;
import com.dng.model.FaceBookInfo;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText ed_email, ed_password;
    private ProgressDialog progressDialog;
    private CallbackManager callbackManager;
    private RelativeLayout fb_login;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LinearLayout activity_login = findViewById(R.id.activity_login);
        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Please wait...");
        session = new Session(this);


        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        loginFb();

        activity_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.hideSoftKeyboard(LoginActivity.this);
            }
        });

        findViewById(R.id.tv_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_forgot_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.fb_login).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (AppHelper.isConnectingToInternet(LoginActivity.this)) {
                    LoginManager.getInstance().logInWithReadPermissions(
                            LoginActivity.this, Arrays.asList("public_profile", "email"));
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.hideSoftKeyboard(LoginActivity.this);
                String Email = ed_email.getText().toString();
                String Password = ed_password.getText().toString();

                if (isValidData()) {
                    if (AppHelper.isConnectingToInternet(LoginActivity.this)) {
                        if (isValidData())
                            loginMentod(Email, Password);
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    }
                }


            }

            // Api call for login
            private void loginMentod(final String Email, final String Password) {
                progressDialog.show();
                String token = FirebaseInstanceId.getInstance().getToken();
                Map<String, String> map = new HashMap<>();
                map.put("email", Email);
                map.put("password", Password);
                map.put("deviceType", "1");
                map.put("deviceToken", token);

                WebService api = new WebService(LoginActivity.this, "fds", new WebService.ResponceListner() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        progressDialog.dismiss();
                        try {
                            JSONObject js = new JSONObject(response);
                            Log.e("LOGIN RESPONSE", response.toString());

                            String status = js.getString("status");
                            String massage = js.getString("message");

                            if (status.equals("success")) {
                                if (massage.equals("invalid login credential.")) {

                                    Toast.makeText(LoginActivity.this, massage, Toast.LENGTH_SHORT).show();
                                } else {
                                    Gson gson = new Gson();
                                    User user = gson.fromJson(String.valueOf(js), User.class);

                                    // If new user login then clear session for old user
                                    if (session.getUser() != null) {
                                        if (!(session.getUser().data.userName.equals(Email)) || !(session.getUser().data.email.equals(Email))) {

                                            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                                AppHelper.stopAlarmService(LoginActivity.this);
                                            }else {
                                                stopService(new Intent(LoginActivity.this, MyService.class));

                                            }

                                            session.clearSessionNewUser();

                                        }
                                    }

                                    // Set login password in session for change password
                                    session.setUserPassword(Password);
                                    session.createSession(user);

                                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                        AppHelper.startAlarmService(LoginActivity.this);
                                    }else {
                                        Intent trackIntent = new Intent(LoginActivity.this, MyService.class);
                                        startService(trackIntent);
                                    }

                                    // User is made on duty when login
                                    if (user.data.onDuty == 0) {
                                        onDutyMethod();
                                    } else {
                                        if (user.data.isVerify.equals("1")) {
                                            if (user.data.approval.equals("0")) {
                                                Intent intent = new Intent(LoginActivity.this, TemplateActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else if (user.data.approval.equals("1")) {
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }
                                }


                            } else {
                                Toast.makeText(LoginActivity.this, massage, Toast.LENGTH_SHORT).show();
                            }


                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
                api.callApi("userLogin", Request.Method.POST, map);
            }


        });
    }

    // Api for checking driver is on duty or off duty
    private void onDutyMethod() {
        if (AppHelper.isConnectingToInternet(LoginActivity.this)) {
            WebService api = new WebService(LoginActivity.this, "fds", new WebService.ResponceListner() {
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

                            if (user.data.isVerify.equals("1")) {
                                if (user.data.approval.equals("0")) {
                                    Intent intent = new Intent(LoginActivity.this, TemplateActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (user.data.approval.equals("1")) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }


                        } else
                            Toast.makeText(LoginActivity.this, massage, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }

            });
            api.callApi("user/duty", Request.Method.GET, null);

        } else {
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Validation method to check login credential is valid or not
    public boolean isValidData() {
        Validation v = new Validation();

        if (!v.isNullValue(ed_email)) {
            Toast.makeText(this, getString(R.string.login_email_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isNullValue(ed_password)) {
            Toast.makeText(this, getString(R.string.login_password_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (!v.isPasswordValid(ed_password)) {
            Toast.makeText(this, getString(R.string.login_password_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Login with facebook
    private void loginFb() {
        final FaceBookInfo faceBookInfo = new FaceBookInfo();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                progressDialog.show();
                String accessToken = loginResult.getAccessToken().getToken();
                final String sSocialId = loginResult.getAccessToken().getUserId();
                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try {
                            JSONObject jsonObject = new JSONObject(String.valueOf(object));
                            String email = "";
                            if (object.has("email")) {
                                email = object.getString("email");
                            }

                            final String socialId = object.getString("id");
                            final String firstname = object.getString("first_name");
                            final String lastname = object.getString("last_name");
                            final String fullname = firstname + " " + lastname;
                            final String profileImage = "https://graph.facebook.com/" + sSocialId + "/picture?type=large";
                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();


                            faceBookInfo.email = email;
                            faceBookInfo.fullname = fullname;
                            faceBookInfo.socialId = socialId;
                            faceBookInfo.profileImage = profileImage;


                            checkfbUserExist(socialId, faceBookInfo);
                            progressDialog.dismiss();
                            // api.signUpTask(params, null);

                        } catch (JSONException e) {
                            e.printStackTrace();

                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }

                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, picture");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onError(FacebookException exception) {
                if (exception instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
                exception.printStackTrace();
            }
        });
    }

    // Api to check whether user exists with facebook login or not
    void checkfbUserExist(String socialId, final FaceBookInfo faceBookInfo) {
        progressDialog.show();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, String> map = new HashMap<>();
        map.put("socialId", socialId);

        WebService api = new WebService(LoginActivity.this, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {
                progressDialog.dismiss();
                try {
                    JSONObject js = new JSONObject(response);

                    String status = js.getString("status");
                    String massage = js.getString("message");


                    if (status.equals("success")) {
                        String otherUser = js.getString("otherUser");
                        if (otherUser.equals("customer")) {
                            userAtOtherPlatformDialog();
                        } else {
                            Gson gson = new Gson();
                            User user = gson.fromJson(String.valueOf(js), User.class);


                            if (user.data.email != null) {
                                session.createSession(user);

                                // On login with facebook set driver on ON duty
                                if (user.data.onDuty == 0) {
                                    onDutyMethod();
                                }else {
                                    if (user.data.isVerify.equals("1")) {
                                        if (user.data.approval.equals("0")) {
                                            Intent intent = new Intent(LoginActivity.this, TemplateActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (user.data.approval.equals("1")) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }


                            } else {
                                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                intent.putExtra("facebookInfo", faceBookInfo);
                                startActivity(intent);
                            }
                        }

                    } else {
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        intent.putExtra("facebookInfo", faceBookInfo);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        });
        api.callApi("checkSocial", Request.Method.POST, map);
    }

    // Dialog to show user is registered with another platform
    public void userAtOtherPlatformDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DNG");
        builder.setMessage("User Register with other platform");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
