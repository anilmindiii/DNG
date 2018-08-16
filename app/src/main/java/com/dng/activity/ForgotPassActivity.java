package com.dng.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.helper.AppHelper;
import com.dng.helper.Validation;
import com.dng.server_task.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassActivity extends AppCompatActivity {
    private EditText ed_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        ed_email = findViewById(R.id.ed_email);
        TextView tvSend = findViewById(R.id.tvSend);

        // Click action of button to call Forgot Password Api
        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppHelper.isConnectingToInternet(ForgotPassActivity.this)){
                    if(isValidData())
                        forgotPass(ed_email.getText().toString().trim());
                }else{
                    Toast.makeText(ForgotPassActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    // Api call for forgot password
    private void forgotPass(String Email){
        Map<String, String> map = new HashMap<>();
        map.put("email", Email);

        WebService api = new WebService(ForgotPassActivity.this, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);

                    String status =  js.getString("status");
                    String massage =  js.getString("message");

                    if(status.equals("success")){
                        ed_email.setText("");
                        Toast.makeText(ForgotPassActivity.this, massage, Toast.LENGTH_SHORT).show();

                    }else Toast.makeText(ForgotPassActivity.this, massage, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("error", e + "");
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        });
        api.callApi("forgotPassword", Request.Method.POST, map);
    }

    // Method to check entered email for forgot password is valid or not
    public boolean isValidData() {
        Validation v = new Validation();

        if (!v.isNullValue(ed_email)) {
            Toast.makeText(this, "please enter email address", Toast.LENGTH_SHORT).show();
            return false;

        }  else if (!v.isEmailValid(ed_email)) {
            Toast.makeText(this, "Please enter vaild email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}


